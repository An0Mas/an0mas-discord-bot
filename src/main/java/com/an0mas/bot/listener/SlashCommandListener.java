package com.an0mas.bot.listener;

import org.jetbrains.annotations.NotNull;

import com.an0mas.bot.command.BaseCommand;
import com.an0mas.bot.command.CommandRegistry;
import com.an0mas.bot.database.DatabaseHelper;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
    

public class SlashCommandListener extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(SlashCommandListener.class);

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        
        String commandName = event.getName();
        String guildId = event.getGuild() != null ? event.getGuild().getId() : null;
        String userId = event.getUser().getId();

        BaseCommand command = CommandRegistry.getByName(commandName);

        if (DatabaseHelper.isMaintenanceMode()) {
            logger.warn("⛔ メンテナンスモード中のため、ユーザー {} はコマンドを実行できません: {}", userId, commandName);
            event.reply("⚠️ 現在メンテナンスモード中のため、このコマンドは実行できません。").setEphemeral(true).queue();
            return;
        }

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
