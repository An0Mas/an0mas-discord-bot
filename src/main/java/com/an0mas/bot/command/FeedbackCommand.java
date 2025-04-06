package com.an0mas.bot.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

/**
 * 📨 /feedback コマンド：Botに関する意見や要望を送るモーダルを表示します。
 */
public class FeedbackCommand extends BaseCommand {

	public FeedbackCommand() {
		super("feedback", "Botに関する意見や要望を送信します！");
	}

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		// 📝 件名入力欄（1行）
		TextInput titleInput = TextInput.create("title", "件名", TextInputStyle.SHORT)
				.setPlaceholder("例：新しいコマンドが欲しい！")
				.setRequired(true)
				.setMaxLength(100)
				.build();

		// 📝 本文入力欄（複数行）
		TextInput contentInput = TextInput.create("content", "内容", TextInputStyle.PARAGRAPH)
				.setPlaceholder("Botに関するフィードバックのみを記入してください")
				.setRequired(true)
				.setMaxLength(1000)
				.build();

		// 📮 モーダルの構築（説明文はタイトルに含めてわかりやすく）
		Modal modal = Modal.create("feedback_modal", "📮 Botへのフィードバックを送信")
				.addActionRow(titleInput)
				.addActionRow(contentInput)
				.build();

		// ✅ モーダルを表示（その場でポップアップ）
		event.replyModal(modal).queue();
	}

	@Override
	public SlashCommandData getSlashCommandData() {
		return Commands.slash(getName(), getDescription());
	}

	@Override
	public boolean isRestricted() {
		return false; // ✅ 誰でも使える！
	}
}
