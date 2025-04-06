package com.an0mas.bot.config;

import io.github.cdimascio.dotenv.Dotenv;

public class ConfigLoader {
	private static final Dotenv dotenv = Dotenv.load(); // .envを読み込む

	public static String get(String key) {
		return dotenv.get(key);
	}
}