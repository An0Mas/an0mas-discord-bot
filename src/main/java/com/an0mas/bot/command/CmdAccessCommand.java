package com.an0mas.bot.command;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.an0mas.bot.config.ConfigLoader;
import com.an0mas.bot.database.DatabaseHelper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class CmdAccessCommand extends BaseCommand {

	public CmdAccessCommand() {
		super("cmdaccess", "コマンドの使用許可を管理します");
	}

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		String ownerId = ConfigLoader.get("BOT_OWNER_ID");
		if (ownerId == null || !event.getUser().getId().equals(ownerId)) {
			event.reply("⛔ このコマンドは開発者専用です。").setEphemeral(true).queue();
			return;
		}
		String subcommand = event.getSubcommandName();
		String type = getOptionValue(event, "type");
		String targetId = getOptionValue(event, "target");
		String commandName = getOptionValue(event, "command");

		if (subcommand == null || (subcommand.equals("add") || subcommand.equals("remove"))
				&& (type == null || targetId == null || commandName == null)) {
			event.reply("⚠️ 入力に不備があります。全てのオプションが必要です。")
					.setEphemeral(true).queue();
			return;
		}

		switch (subcommand) {
		case "add" -> handleAdd(event, type, targetId, commandName);
		case "remove" -> handleRemove(event, type, targetId, commandName);
		case "list" -> handleList(event, type, targetId);
		case "listall" -> handleListAll(event);
		default -> event.reply("⚠️ 不明なサブコマンドです。").setEphemeral(true).queue();
		}
	}

	private void handleAdd(SlashCommandInteractionEvent event, String type, String targetId, String commandName) {
		boolean success = false;
		if (type.equalsIgnoreCase("server")) {
			DatabaseHelper.addGuildPermission(targetId, commandName);
			success = true;

			Guild guild = event.getJDA().getGuildById(targetId);
			if (guild != null) {
				BaseCommand cmd = CommandRegistry.getByName(commandName);
				if (cmd != null && cmd.getSlashCommandData() != null) {
					guild.upsertCommand(cmd.getSlashCommandData()).queue();
				}
			}
		} else if (type.equalsIgnoreCase("user")) {
			DatabaseHelper.addUserPermission(targetId, commandName);
			success = true;
		}

		if (success) {
			event.reply("✅ 許可を追加しました！").setEphemeral(true).queue();
		} else {
			event.reply("⚠️ 許可の追加に失敗しました。入力を確認してください。")
					.setEphemeral(true).queue();
		}
	}

	private void handleRemove(SlashCommandInteractionEvent event, String type, String targetId, String commandName) {
		boolean success = false;
		if (type.equalsIgnoreCase("server")) {
			DatabaseHelper.removeGuildPermission(targetId, commandName);
			success = true;
		} else if (type.equalsIgnoreCase("user")) {
			DatabaseHelper.removeUserPermission(targetId, commandName);
			success = true;
		}

		if (success) {
			event.reply("✅ 許可を削除しました！").setEphemeral(true).queue();
		} else {
			event.reply("⚠️ 許可の削除に失敗しました。入力を確認してください。")
					.setEphemeral(true).queue();
		}
	}

	private void handleList(SlashCommandInteractionEvent event, String type, String targetId) {
		List<String> commands;
		String title;

		if (type.equalsIgnoreCase("server")) {
			commands = DatabaseHelper.getCommandsAllowedForGuild(targetId);
			Guild guild = event.getJDA().getGuildById(targetId);
			String name = (guild != null) ? guild.getName() : "Unknown Guild (" + targetId + ")";
			title = "📋 サーバーの許可コマンド一覧: " + name;
		} else if (type.equalsIgnoreCase("user")) {
			commands = DatabaseHelper.getCommandsAllowedForUser(targetId);
			User user = event.getJDA().getUserById(targetId);
			String name = (user != null) ? user.getAsTag() : "Unknown User (" + targetId + ")";
			title = "📋 ユーザーの許可コマンド一覧: " + name;
		} else {
			event.reply("⚠️ typeは 'server' または 'user' を指定してください。")
					.setEphemeral(true).queue();
			return;
		}

		if (commands.isEmpty()) {
			event.reply("📭 許可されているコマンドはありません。").setEphemeral(true).queue();
		} else {
			EmbedBuilder embed = new EmbedBuilder()
					.setTitle(title)
					.setDescription(commands.stream().map(cmd -> "• /" + cmd).collect(Collectors.joining("\n")))
					.setColor(Color.GREEN);
			event.replyEmbeds(embed.build()).setEphemeral(true).queue();
		}
	}

	private void handleListAll(SlashCommandInteractionEvent event) {
		Map<String, List<String>> guildMap = DatabaseHelper.getAllGuildPermissions();
		Map<String, List<String>> userMap = DatabaseHelper.getAllUserPermissions();

		EmbedBuilder embed = new EmbedBuilder().setTitle("📋 全体の使用許可リスト").setColor(Color.BLUE);

		if (!guildMap.isEmpty()) {
			embed.addField("🏠 サーバーごとの許可", guildMap.entrySet().stream()
					.map(entry -> {
						Guild guild = event.getJDA().getGuildById(entry.getKey());
						String name = (guild != null) ? guild.getName() : "Unknown Guild (" + entry.getKey() + ")";
						String cmds = String.join(", ", entry.getValue());
						return "**" + name + "**: " + cmds;
					})
					.collect(Collectors.joining("\n")), false);
		}

		if (!userMap.isEmpty()) {
			embed.addField("👤 ユーザーごとの許可", userMap.entrySet().stream()
					.map(entry -> {
						User user = event.getJDA().getUserById(entry.getKey());
						String name = (user != null) ? user.getAsTag() : "Unknown User (" + entry.getKey() + ")";
						String cmds = String.join(", ", entry.getValue());
						return "**" + name + "**: " + cmds;
					})
					.collect(Collectors.joining("\n")), false);
		}

		event.replyEmbeds(embed.build()).setEphemeral(true).queue();
	}

	private String getOptionValue(SlashCommandInteractionEvent event, String name) {
		var option = event.getOption(name);
		return option != null ? option.getAsString() : null;
	}

	@Override
	public SlashCommandData getSlashCommandData() {
		return Commands.slash(getName(), getDescription())
				.addSubcommands(
						new SubcommandData("add", "使用許可を追加")
								.addOption(OptionType.STRING, "type", "server か user を指定", true)
								.addOption(OptionType.STRING, "target", "サーバーIDまたはユーザーID", true)
								.addOption(OptionType.STRING, "command", "許可するコマンド名", true),
						new SubcommandData("remove", "使用許可を削除")
								.addOption(OptionType.STRING, "type", "server か user を指定", true)
								.addOption(OptionType.STRING, "target", "サーバーIDまたはユーザーID", true)
								.addOption(OptionType.STRING, "command", "削除するコマンド名", true),
						new SubcommandData("list", "対象に許可されているコマンドを表示")
								.addOption(OptionType.STRING, "type", "server か user を指定", true)
								.addOption(OptionType.STRING, "target", "対象ID (サーバーまたはユーザー)", true),
						new SubcommandData("listall", "全ての許可情報を表示（管理者用）"));
	}

	@Override
	public boolean isRestricted() {
		return true;
	}
}
