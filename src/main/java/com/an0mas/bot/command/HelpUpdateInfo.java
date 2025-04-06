package com.an0mas.bot.command;

import java.util.List;

/**
 * 🛠️ /help コマンド内で表示する「アップデート予定」や「お知らせ」などの定義クラス
 */
public class HelpUpdateInfo {

	/**
	 * 今後追加予定の機能リスト
	 */
	public static List<String> getUpcomingFeatures() {
		return List.of(
				"`/config` によるBot設定の管理",
				"`/userinfo` コマンドでユーザー情報を表示");
	}

	/**
	 * 注意書きや補足文など
	 */
	public static String getNotice() {
		return """
				💡 このBotでは、管理者が特定のコマンドの使用を制限している場合があります。
				コマンドが見えない場合は、サーバー管理者に確認してください。

				🎉 ご意見・不具合の報告は `/feedback` コマンドからどうぞ！
				""";
	}
}
