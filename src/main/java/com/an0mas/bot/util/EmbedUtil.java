package com.an0mas.bot.util;

import com.an0mas.bot.model.FeedbackEntry;

/**
 * 🧩 フィードバック表示用のEmbed整形ユーティリティ
 */
public class EmbedUtil {

	/**
	 * 📝 フィードバックを見やすいテキスト形式に整形して返す
	 * 
	 * @param entry 表示対象のフィードバック
	 * @return 整形済みのテキスト
	 */
	public static String formatFeedbackEntry(FeedbackEntry entry) {
		// 匿名対応：ユーザー名がnullや空文字の場合は「匿名」
		String user = (entry.userName == null || entry.userName.isBlank()) ? "匿名" : entry.userName;

		// 日付の整形：2025-04-05T23:38:30.627806+09:00 → 2025/04/05 23:38
		String timestamp = entry.timestamp
				.replace("T", " ")
				.replaceAll("\\..*?\\+", "") // 小数点秒〜タイムゾーンを削除
				.replace("-", "/")
				.substring(0, 16); // yyyy/MM/dd HH:mm 形式

		return "📝 **件名：" + entry.title + "**\n"
				+ "🆔 ID：" + entry.id + "\n"
				+ "👤 " + user + "\n"
				+ "📅 " + timestamp;
	}
}
