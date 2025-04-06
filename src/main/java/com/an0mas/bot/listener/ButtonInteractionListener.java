package com.an0mas.bot.listener;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.an0mas.bot.database.FeedbackDatabaseHelper;
import com.an0mas.bot.model.FeedbackEntry;
import com.an0mas.bot.util.EmbedUtil;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

/**
 * ⏮️⏭️ フィードバック一覧のページ切り替えや詳細表示を処理するリスナー
 */
public class ButtonInteractionListener extends ListenerAdapter {

	private static final int PAGE_SIZE = 5;

	@Override
	public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
		String buttonId = event.getComponentId();

		if (buttonId.startsWith("feedback_page_")) {
			handleFeedbackListPagination(event);
		} else if (buttonId.startsWith("feedback_detail_")) {
			handleFeedbackDetail(event);
		}
	}

	/**
	 * 📋 フィードバック一覧ページの切り替えを処理する
	 */
	private void handleFeedbackListPagination(ButtonInteractionEvent event) {
		try {
			event.deferEdit().queue();

			String[] parts = event.getComponentId().split("_");
			if (parts.length != 5)
				return;

			int currentPage = Integer.parseInt(parts[2]);
			int totalPages = Integer.parseInt(parts[3]);
			String direction = parts[4];

			int newPage = switch (direction) {
			case "next" -> currentPage + 1;
			case "prev" -> currentPage - 1;
			default -> currentPage;
			};

			if (newPage < 1 || newPage > totalPages)
				return;

			List<FeedbackEntry> allFeedbacks = FeedbackDatabaseHelper.getAllFeedbacks();
			int start = (newPage - 1) * PAGE_SIZE;
			int end = Math.min(start + PAGE_SIZE, allFeedbacks.size());
			List<FeedbackEntry> pageItems = allFeedbacks.subList(start, end);

			EmbedBuilder embed = new EmbedBuilder()
					.setTitle("📬 フィードバック一覧 ページ " + newPage + " / " + totalPages)
					.setColor(Color.CYAN);

			for (FeedbackEntry entry : pageItems) {
				embed.addField("⠀", EmbedUtil.formatFeedbackEntry(entry), false);
			}

			boolean hasPrev = newPage > 1;
			boolean hasNext = newPage < totalPages;

			List<Button> navButtons = List.of(
					Button.primary("feedback_page_" + newPage + "_" + totalPages + "_prev", "◀ 前へ")
							.withDisabled(!hasPrev),
					Button.secondary("page_number_display", newPage + " / " + totalPages).asDisabled(),
					Button.primary("feedback_page_" + newPage + "_" + totalPages + "_next", "次へ ▶")
							.withDisabled(!hasNext));

			List<Button> detailButtons = new ArrayList<>();
			for (FeedbackEntry entry : pageItems) {
				detailButtons.add(Button.secondary("feedback_detail_" + entry.id, "🔍 詳細 (" + entry.id + ")"));
			}

			event.getHook().editOriginalEmbeds(embed.build())
					.setComponents(ActionRow.of(detailButtons), ActionRow.of(navButtons))
					.queue();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 📝 指定されたIDのフィードバックをEmbedで詳細表示
	 */
	private void handleFeedbackDetail(ButtonInteractionEvent event) {
		try {
			String buttonId = event.getComponentId(); // 例: feedback_detail_7
			int id = Integer.parseInt(buttonId.replace("feedback_detail_", ""));

			FeedbackEntry entry = FeedbackDatabaseHelper.getFeedbackById(id);
			if (entry == null) {
				event.reply("⚠️ フィードバックが見つかりませんでした。").setEphemeral(true).queue();
				return;
			}

			EmbedBuilder embed = new EmbedBuilder()
					.setTitle("📄 フィードバック詳細 [ID: " + entry.id + "]")
					.addField("👤 ユーザー", entry.userName, false)
					.addField("📝 件名", entry.title, false)
					.addField("📝 内容", entry.content, false)
					.addField("📅 送信日時", entry.timestamp, false)
					.setColor(Color.ORANGE);

			event.replyEmbeds(embed.build()).setEphemeral(true).queue();

		} catch (Exception e) {
			e.printStackTrace();
			event.reply("⚠️ 詳細表示中にエラーが発生しました。").setEphemeral(true).queue();
		}
	}
}
