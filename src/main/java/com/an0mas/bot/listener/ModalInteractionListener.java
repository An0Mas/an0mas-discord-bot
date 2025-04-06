package com.an0mas.bot.listener;

import java.awt.Color;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.jetbrains.annotations.NotNull;

import com.an0mas.bot.database.FeedbackDatabaseHelper;
import com.an0mas.bot.util.BotConstants;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * 📅 ModalInteractionListener：モーダル（ポップアップ）の入力を受け取るリスナー
 * 
 * 複数のモーダルを処理できるよう、モーダルIDで分岐して処理を振り分けます。
 */
public class ModalInteractionListener extends ListenerAdapter {

	@Override
	public void onModalInteraction(@NotNull ModalInteractionEvent event) {
		// 🎯 モーダルIDで処理を分岐（今回はフィードバック専用モーダル）
		if ("feedback_modal".equals(event.getModalId())) {
			handleFeedbackModal(event);
		}
	}

	/**
	 * 📮 フィードバックモーダルの処理
	 * 入力されたタイトル・本文をフィードバックDBに保存し、通知も送信します。
	 */
	private void handleFeedbackModal(ModalInteractionEvent event) {
		// ✍️ 入力内容を取得
		String title = event.getValue("title").getAsString();
		String content = event.getValue("content").getAsString();

		// 🕵️‍♂️ 匿名入力欄からフラグを判断（大文字・小文字関係なし）
		String anonInput = event.getValue("anonymous") != null
				? event.getValue("anonymous").getAsString().trim().toLowerCase()
				: "";

		boolean anonymous = anonInput.equals("yes");

		// 👤 ユーザー情報と送信日時を取得
		String userId = event.getUser().getId();
		String userName = event.getMember() != null
				? event.getMember().getEffectiveName()
				: event.getUser().getName();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd (E) HH:mm", Locale.JAPANESE);
		String timestamp = OffsetDateTime.now().format(formatter);

		// 💾 フィードバックを保存（匿名でも実ユーザー情報は記録）
		FeedbackDatabaseHelper.insertFeedback(userId, userName, title, content, timestamp);

		// ✅ 成功メッセージを送信（匿名対応）
		String replyMessage = anonymous
				? "✅ 匿名フィードバックを受け付けました！ありがとう〜！"
				: "✅ フィードバックを受け付けました！ありがとう〜！";

		event.reply(replyMessage)
				.setEphemeral(true)
				.queue();

		// 📢 通知を送信（BotConstantsで指定したチャンネルへ）
		TextChannel notifyChannel = event.getJDA().getTextChannelById(BotConstants.FEEDBACK_NOTIFY_CHANNEL_ID);
		if (notifyChannel != null) {
			String displayName = anonymous ? "匿名" : userName;

			EmbedBuilder embed = new EmbedBuilder()
					.setTitle("📬 新しいフィードバックが届きました！")
					.addField("👤 ユーザー", displayName, false)
					.addField("📝 タイトル", title, false)
					.addField("📅 送信日時", timestamp, false)
					.setColor(Color.CYAN);

			notifyChannel.sendMessageEmbeds(embed.build()).queue();
		}
	}
}