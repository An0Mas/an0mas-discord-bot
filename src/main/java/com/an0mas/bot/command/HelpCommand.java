package com.an0mas.bot.command;

import java.util.List;

import com.an0mas.bot.database.DatabaseHelper;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

/**
 * 🆘 /help コマンド：使用可能なコマンド一覧・制限・アップデート情報を表示
 */
public class HelpCommand extends BaseCommand {

	public HelpCommand() {
        super("help", "コマンドの一覧と説明を表示します。");
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String guildId = event.getGuild() != null ? event.getGuild().getId() : null;
        String userId = event.getUser().getId();

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("📘 An0MasBot ヘルプ");
        embed.setDescription("以下は現在このサーバーで使用できるコマンドです：");
        embed.setColor(0x1E90FF); // 青系カラー（カスタム可能）

        for (BaseCommand cmd : CommandRegistry.getCommands()) {
            // 🔒 制限付きの場合、使用可能かチェック
            if (cmd.isRestricted()) {
                boolean guildAllowed = guildId != null && DatabaseHelper.isGuildCommandAllowed(guildId, cmd.getName());
                boolean userAllowed = DatabaseHelper.isUserCommandAllowed(userId, cmd.getName());

                // 両方とも許可されてない → 表示しない
                if (!guildAllowed && !userAllowed) continue;

                // ギルドはOKだけどユーザーはNG → 🔒付きで表示
                if (!userAllowed) {
                    embed.addField("🔒 /" + cmd.getName(), cmd.getDescription() + "（使用制限あり）", false);
                    continue;
                }
            }

            // ✅ 制限なし or 許可済み → 通常表示
            embed.addField("✅ /" + cmd.getName(), cmd.getDescription(), false);
        }

        // 📌 お知らせやアップデート予定のまとめ
        StringBuilder notes = new StringBuilder();

        // 🛠️ 今後のアップデート予定
        List<String> upcoming = HelpUpdateInfo.getUpcomingFeatures();
        if (!upcoming.isEmpty()) {
            notes.append("🛠️ **今後のアップデート予定：**\n");
            for (String item : upcoming) {
                notes.append("- ").append(item).append("\n");
            }
            notes.append("\n");
        }

        // 💡 注意書き
        notes.append(HelpUpdateInfo.getNotice());

        // 📦 お知らせセクションとして追加
        embed.addField("📌 お知らせ", notes.toString(), false);
        embed.setFooter("🔒 はあなたが使用できない制限付きコマンドです");

        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
    }

    @Override
    public SlashCommandData getSlashCommandData() {
        return Commands.slash(getName(), getDescription());
    }
}
