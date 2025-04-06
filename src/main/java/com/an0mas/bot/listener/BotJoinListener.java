package com.an0mas.bot.listener;

import java.util.ArrayList;
import java.util.List;

import com.an0mas.bot.command.BaseCommand;
import com.an0mas.bot.command.CommandRegistry;
import com.an0mas.bot.database.DatabaseHelper;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

/**
 * 🏠 Botが新しいGuildに追加された時の処理
 */
public class BotJoinListener extends ListenerAdapter {

	@Override
	public void onGuildJoin(GuildJoinEvent event) {
		Guild guild = event.getGuild();
		String guildId = guild.getId();

		List<SlashCommandData> commandsToRegister = new ArrayList<>();

		for (BaseCommand cmd : CommandRegistry.getCommands()) {
			if (cmd.isRestricted()) {
				String name = cmd.getName();

				if (DatabaseHelper.isGuildCommandAllowed(name, guildId)) {
					commandsToRegister.add(cmd.getSlashCommandData());
					System.out.println("✅ " + name + " を Guild " + guildId + " に登録予定");
				}
			}
		}

		if (!commandsToRegister.isEmpty()) {
			guild.updateCommands().addCommands(commandsToRegister).queue(
					success -> System.out.println("✅ Guild登録完了：" + guild.getName() + "（" + guildId + "）"),
					error -> System.out.println("❌ Guild登録失敗：" + error.getMessage()));
		} else {
			System.out.println("ℹ️ 登録するコマンドはありません：" + guild.getName());
		}
	}
}
