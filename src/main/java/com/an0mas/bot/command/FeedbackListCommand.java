package com.an0mas.bot.command;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.an0mas.bot.database.FeedbackDatabaseHelper;
import com.an0mas.bot.model.FeedbackEntry;
import com.an0mas.bot.util.BotConstants;
import com.an0mas.bot.util.EmbedUtil;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

/**
 * 📋 /feedbacklist コマンド：Botへのフィードバック一覧を表示（ページ切り替え対応）
 */
public class FeedbackListCommand extends BaseCommand {

	public FeedbackListCommand() {
		super("feedbacklist", "送られたフィードバックを一覧表示します（管理者用）");
	}

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		if (!event.getUser().getId().equals(BotConstants.OWNER_ID)) {
			event.reply("⚠️ このコマンドは開発者専用です。").setEphemeral(true).queue();
			return;
		}

		int page = 1;
		int pageSize = 5;
		List<FeedbackEntry> allFeedbacks = FeedbackDatabaseHelper.getAllFeedbacks();

		if (allFeedbacks.isEmpty()) {
			event.reply("📭 現在フィードバックはありません。").setEphemeral(true).queue();
			return;
		}

		int totalPages = (int) Math.ceil(allFeedbacks.size() / (double) pageSize);
		List<FeedbackEntry> pageEntries = allFeedbacks.stream()
				.skip((page - 1) * pageSize)
				.limit(pageSize)
				.toList();

		// 📋 Embed1つにまとめて表示（見やすく整形）
		EmbedBuilder embed = new EmbedBuilder()
				.setTitle("📬 フィードバック一覧 ページ " + page + " / " + totalPages)
				.setColor(Color.CYAN);

		for (FeedbackEntry entry : pageEntries) {
			embed.addField("⠀", EmbedUtil.formatFeedbackEntry(entry), false);
		}

		// 🔍 詳細表示ボタン
		List<Button> detailButtons = new ArrayList<>();
		for (FeedbackEntry entry : pageEntries) {
			detailButtons.add(Button.secondary("feedback_detail_" + entry.id, "🔍 詳細 (" + entry.id + ")"));
		}

		// 🔘 ページ操作ボタン
		Button prev = Button.primary("feedback_page_1_" + totalPages + "_prev", "◀ 前へ").withDisabled(true);
		Button next = Button.primary("feedback_page_1_" + totalPages + "_next", "次へ ▶")
				.withDisabled(totalPages <= 1);
		Button pageInfo = Button.secondary("page_number_display", "1 / " + totalPages).asDisabled();

		event.replyEmbeds(embed.build())
				.addActionRow(detailButtons)
				.addActionRow(prev, pageInfo, next)
				.setEphemeral(true)
				.queue();
	}

	@Override
	public SlashCommandData getSlashCommandData() {
		return Commands.slash(getName(), getDescription());
	}

	@Override
	public boolean isRestricted() {
		return true;
	}
}
