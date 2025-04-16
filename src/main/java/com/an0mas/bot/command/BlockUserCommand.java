package com.an0mas.bot.command;

import java.util.List;

import com.an0mas.bot.config.ConfigLoader;
import com.an0mas.bot.database.DatabaseHelper;
import com.an0mas.bot.util.AccessControlUtil;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

/**
 * ⛔ BlockUserCommand:
 * ユーザーをブロック／ブロック解除／リスト表示する開発者専用コマンド
 */
public class BlockUserCommand extends BaseCommand {

	public BlockUserCommand() {
		super("blockuser", "ユーザーのBot利用を制限・解除・確認します（開発者専用）");
	}

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		// ✅ 開発者のみ使用可
		String ownerId = ConfigLoader.get("OWNER_ID");
		if (ownerId == null || !event.getUser().getId().equals(ownerId)) {
			event.reply("⚠️ このコマンドは開発者専用です。\n（あなたのID: `%s`）"
					.formatted(event.getUser().getId()))
					.setEphemeral(true)
					.queue();
			return;
		}

		String subcommand = event.getSubcommandName();

		if (subcommand.equals("list")) {
			List<String> blocked = DatabaseHelper.getAllBlacklistedUsers();
			if (blocked.isEmpty()) {
				event.reply("✅ 現在ブロックされているユーザーはいません。")
						.setEphemeral(true).queue();
			} else {
				StringBuilder sb = new StringBuilder("⛔ ブロック中のユーザー一覧 (`%d` 件)：\n".formatted(blocked.size()));
				for (String id : blocked) {
					sb.append("- `").append(id).append("`\n");
				}
				event.reply(sb.toString()).setEphemeral(true).queue();
			}
			return;
		}

		String userId = event.getOption("user_id") != null ? event.getOption("user_id").getAsString() : null;

		if (userId == null) {
			event.reply("⚠️ user_id が指定されていません。").setEphemeral(true).queue();
			return;
		}

		if (subcommand.equals("add")) {
			AccessControlUtil.blockUser(userId);
			event.reply("⛔ ユーザー `%s` をブロックリストに追加しました。".formatted(userId))
					.setEphemeral(true).queue();
		} else if (subcommand.equals("remove")) {
			AccessControlUtil.unblockUser(userId);
			event.reply("✅ ユーザー `%s` をブロックリストから解除しました。".formatted(userId))
					.setEphemeral(true).queue();
		} else {
			event.reply("⚠️ 不明なサブコマンドです。").setEphemeral(true).queue();
		}
	}

	@Override
	public SlashCommandData getSlashCommandData() {
		return Commands.slash(getName(), getDescription())
				.addSubcommands(
						new SubcommandData("add", "指定したユーザーをブロック")
								.addOption(OptionType.STRING, "user_id", "ブロックするユーザーのID", true),
						new SubcommandData("remove", "指定したユーザーのブロックを解除")
								.addOption(OptionType.STRING, "user_id", "解除するユーザーのID", true),
						new SubcommandData("list", "現在ブロック中のユーザー一覧を表示"));
	}

	@Override
	public boolean isRestricted() {
		return true; // 🛡️ 開発者のみ実行可能（OWNER_ID）
	}
}
