package com.an0mas.bot.listener;

import org.jetbrains.annotations.NotNull;

import com.an0mas.bot.command.BaseCommand;
import com.an0mas.bot.command.CommandRegistry;
import com.an0mas.bot.database.DatabaseHelper;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class SlashCommandListener extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        String guildId = event.getGuild() != null ? event.getGuild().getId() : null;
        String userId = event.getUser().getId();

        BaseCommand command = CommandRegistry.getByName(commandName);

        if (command == null) {
            event.reply("⚠️ 未知のコマンドです。").setEphemeral(true).queue();
            return;
        }

        // 🛡️ 制限付きコマンドは許可チェック！
        if (command.isRestricted()) {
            boolean guildAllowed = guildId != null && DatabaseHelper.isGuildCommandAllowed(guildId, commandName);
            boolean userAllowed = DatabaseHelper.isUserCommandAllowed(userId, commandName);

            if (!guildAllowed && !userAllowed) {
                event.reply("⚠️ このコマンドは使用できません。").setEphemeral(true).queue();
                return;
            }
        }

        // 🚀 実行！
        command.execute(event);
    }
}
