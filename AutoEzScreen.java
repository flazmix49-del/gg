package com.autoez;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class AutoEzScreen extends Screen {
	private final Screen parent;
	private TextFieldWidget messageField;
	private ButtonWidget toggleButton;
	private ButtonWidget delayButton;

	public AutoEzScreen(Screen parent) {
		super(Text.literal("Auto EZ"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		int cx = this.width / 2;
		int y = this.height / 2 - 55;

		toggleButton = ButtonWidget.builder(toggleText(), b -> {
			AutoEzConfig.enabled = !AutoEzConfig.enabled;
			b.setMessage(toggleText());
		}).dimensions(cx - 100, y, 200, 20).build();
		addDrawableChild(toggleButton);

		messageField = new TextFieldWidget(this.textRenderer, cx - 100, y + 40, 200, 20, Text.literal("Сообщение"));
		messageField.setMaxLength(100);
		messageField.setText(AutoEzConfig.message);
		addDrawableChild(messageField);
		setInitialFocus(messageField);

		delayButton = ButtonWidget.builder(delayText(), b -> {
			// 0, 5, 10, 20, 40 тиков по кругу
			int[] opts = {0, 5, 10, 20, 40};
			int next = opts[0];
			for (int i = 0; i < opts.length; i++) {
				if (opts[i] == AutoEzConfig.delayTicks) {
					next = opts[(i + 1) % opts.length];
					break;
				}
			}
			AutoEzConfig.delayTicks = next;
			b.setMessage(delayText());
		}).dimensions(cx - 100, y + 70, 200, 20).build();
		addDrawableChild(delayButton);

		addDrawableChild(ButtonWidget.builder(Text.literal("Готово"), b -> close())
				.dimensions(cx - 100, y + 105, 200, 20).build());
	}

	private Text toggleText() {
		return Text.literal("Auto EZ: " + (AutoEzConfig.enabled ? "ВКЛ" : "ВЫКЛ"));
	}

	private Text delayText() {
		return Text.literal("Задержка: " + AutoEzConfig.delayTicks + " тиков");
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, this.height / 2 - 80, 0xFFFFFF);
		context.drawTextWithShadow(this.textRenderer, Text.literal("Сообщение после убийства:"),
				this.width / 2 - 100, this.height / 2 - 26, 0xA0A0A0);
	}

	@Override
	public void close() {
		AutoEzConfig.message = messageField.getText();
		AutoEzConfig.save();
		if (this.client != null) {
			this.client.setScreen(parent);
		}
	}
}
