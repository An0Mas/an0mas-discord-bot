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
		// 📝 件名
		TextInput titleInput = TextInput.create("title", "件名", TextInputStyle.SHORT)
				.setPlaceholder("例：新しいコマンドが欲しい！")
				.setRequired(true)
				.setMaxLength(100)
				.build();

		// 📝 内容
		TextInput contentInput = TextInput.create("content", "内容", TextInputStyle.PARAGRAPH)
				.setPlaceholder("Botに関するフィードバックを入力してください")
				.setRequired(true)
				.setMaxLength(1000)
				.build();

		// 🕵️ 匿名送信
		TextInput anonymousInput = TextInput.create("anonymous", "匿名で送る？", TextInputStyle.SHORT)
				.setPlaceholder("yes と入力すると匿名になります")
				.setRequired(false)
				.setMaxLength(10)
				.build();

		// 📮 モーダル
		Modal modal = Modal.create("feedback_modal", "📮 Botへのフィードバックを送信")
				.addActionRow(titleInput)
				.addActionRow(contentInput)
				.addActionRow(anonymousInput)
				.build();

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
