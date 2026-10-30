package com.autoez;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import org.lwjgl.glfw.GLFW;

public class AutoEzMod implements ClientModInitializer {
	// Клавиша меню по умолчанию: \ (backslash). Меняется в Настройки -> Управление -> Auto EZ
	public static KeyBinding openMenuKey;

	private static PlayerEntity lastTarget;
	private static long lastHitTime;
	private static int pendingTicks = -1;

	@Override
	public void onInitializeClient() {
		AutoEzConfig.load();

		openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.autoez.open_menu",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_BACKSLASH,
				"category.autoez"
		));

		// Запоминаем последнего атакованного игрока
		AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if (world.isClient() && entity instanceof PlayerEntity target && target != player) {
				lastTarget = target;
				lastHitTime = System.currentTimeMillis();
			}
			return ActionResult.PASS;
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			// Открытие меню
			while (openMenuKey.wasPressed()) {
				client.setScreen(new AutoEzScreen(client.currentScreen));
			}

			// Отложенная отправка сообщения
			if (pendingTicks >= 0) {
				if (pendingTicks == 0) {
					sendEz(client);
				}
				pendingTicks--;
			}

			if (!AutoEzConfig.enabled || lastTarget == null || client.player == null) {
				return;
			}

			// Прошло слишком много времени после удара — забываем цель
			if (System.currentTimeMillis() - lastHitTime > AutoEzConfig.killWindowMs) {
				lastTarget = null;
				return;
			}

			LivingEntity t = lastTarget;
			if (t.isDead() || t.getHealth() <= 0.0F || t.isRemoved()) {
				lastTarget = null;
				pendingTicks = Math.max(0, AutoEzConfig.delayTicks);
			}
		});
	}

	private static void sendEz(MinecraftClient client) {
		if (client.player == null || client.getNetworkHandler() == null) return;
		String msg = AutoEzConfig.message;
		if (msg == null || msg.isBlank()) msg = "ez";
		client.getNetworkHandler().sendChatMessage(msg);
	}
}
