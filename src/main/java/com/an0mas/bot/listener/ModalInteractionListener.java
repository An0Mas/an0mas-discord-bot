package com.an0mas.bot.listener;

import java.time.OffsetDateTime;

import org.jetbrains.annotations.NotNull;

import com.an0mas.bot.database.FeedbackDatabaseHelper;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * 📥 ModalInteractionListener：モーダル（ポップアップ）の入力を受け取るリスナー
 * 
 * 複数のモーダルを処理できるよう、モーダルIDで分岐して処理を振り分けます。
 */
public class ModalInteractionListener extends ListenerAdapter {

	@Override
	public void onModalInteraction(@NotNull ModalInteractionEvent event) {
		// 🎯 モーダルIDで処理を分岐（今回はフィードバック専用モーダル）
		if (event.getModalId().equals("feedback_modal")) {
			handleFeedbackModal(event);
		}
	}

	/**
	 * 📮 フィードバックモーダルの処理
	 * 入力されたタイトル・本文をフィードバックDBに保存します。
	 */
	private void handleFeedbackModal(ModalInteractionEvent event) {
		// ✍️ 入力内容を取得
		String title = event.getValue("title").getAsString();
		String content = event.getValue("content").getAsString();

		// 👤 ユーザー情報と送信日時を取得
		String userId = event.getUser().getId();
		String userName = event.getMember() != null
				? event.getMember().getEffectiveName() // サーバー内ニックネーム
				: event.getUser().getName(); // DMなどのフォールバック
		String timestamp = OffsetDateTime.now().toString();

		// 💾 フィードバックを保存
		FeedbackDatabaseHelper.insertFeedback(userId, userName, title, content, timestamp);

		// ✅ 成功メッセージを送信
		event.reply("✅ フィードバックを受け付けました！ありがとう〜！")
				.setEphemeral(true)
				.queue();
	}
}