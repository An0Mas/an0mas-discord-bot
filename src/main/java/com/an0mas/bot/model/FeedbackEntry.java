package com.an0mas.bot.model;

/**
 * 🗂️ フィードバック1件分の情報を保持するクラス
 */
public class FeedbackEntry {
	public int id;
	public String userId;
	public String userName;
	public String title;
	public String content;
	public String timestamp;

	public FeedbackEntry(int id, String userId, String userName, String title, String content, String timestamp) {
		this.id = id;
		this.userId = userId;
		this.userName = userName;
		this.title = title;
		this.content = content;
		this.timestamp = timestamp;
	}
}
