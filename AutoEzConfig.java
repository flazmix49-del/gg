package com.autoez;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class AutoEzConfig {
	public static boolean enabled = true;
	public static String message = "ez";
	public static int delayTicks = 10;      // 20 тиков = 1 секунда
	public static long killWindowMs = 5000; // сколько после последнего удара считается убийством

	private static Path file() {
		return FabricLoader.getInstance().getConfigDir().resolve("autoez.properties");
	}

	public static void load() {
		Path f = file();
		if (!Files.exists(f)) {
			save();
			return;
		}
		try (Reader r = Files.newBufferedReader(f)) {
			Properties p = new Properties();
			p.load(r);
			enabled = Boolean.parseBoolean(p.getProperty("enabled", "true"));
			message = p.getProperty("message", "ez");
			delayTicks = Integer.parseInt(p.getProperty("delayTicks", "10"));
			killWindowMs = Long.parseLong(p.getProperty("killWindowMs", "5000"));
		} catch (IOException | NumberFormatException e) {
			e.printStackTrace();
		}
	}

	public static void save() {
		Properties p = new Properties();
		p.setProperty("enabled", Boolean.toString(enabled));
		p.setProperty("message", message);
		p.setProperty("delayTicks", Integer.toString(delayTicks));
		p.setProperty("killWindowMs", Long.toString(killWindowMs));
		try (Writer w = Files.newBufferedWriter(file())) {
			p.store(w, "Auto EZ config");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
