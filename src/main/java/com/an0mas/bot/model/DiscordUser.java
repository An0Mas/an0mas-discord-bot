package com.an0mas.bot.model;

public class DiscordUser {
	private final String username;
	private final String avatarUrl;

	public DiscordUser(String username, String avatarUrl) {
		this.username = username;
		this.avatarUrl = avatarUrl;
	}

	public String getUsername() {
		return username;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}
}
