package com.an0mas.bot.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.an0mas.bot.database.DatabaseHelper;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

/**
 * 🛠️ スラッシュコマンドを登録する初期化クラス。
 *
 * ✅ 制限なしコマンドはグローバル登録
 * 🔒 制限付きコマンドは許可されたGuildにのみ登録
 * 🛡️ 特定の制限付きコマンド（cmdaccess）は例外的にグローバル登録
 */
public class CommandsInitializer {

	public static void registerSlashCommands(JDA jda) {
		List<SlashCommandData> globalCommands = new ArrayList<>();
		Map<String, List<SlashCommandData>> guildCommandMap = new HashMap<>();
		Set<String> globallyRegistered = new HashSet<>();

		for (BaseCommand cmd : CommandRegistry.getCommands()) {
			SlashCommandData data = cmd.getSlashCommandData();
			if (data == null)
				continue;

			String name = cmd.getName();

			if (!cmd.isRestricted()) {
				// ✅ 制限なし → グローバル登録
				globalCommands.add(data);
				globallyRegistered.add(name);
				System.out.println("🌐 グローバル登録 → /" + name);

			} else if (name.equals("cmdaccess")) {
				// 🛡️ 特例としてグローバル登録
				globalCommands.add(data);
				globallyRegistered.add(name);
				System.out.println("🛡️ 特例でグローバル登録 → /" + name);

			} else {
				// 🔒 その他の制限付きコマンド → Guildごとに登録
				List<String> allowedGuilds = DatabaseHelper.getGuildsAllowedForCommand(name);
				System.out.println("🔎 /" + name + " の許可Guild数: " + allowedGuilds.size());

				for (String guildId : allowedGuilds) {
					guildCommandMap.computeIfAbsent(guildId, k -> new ArrayList<>()).add(data);
					System.out.println("📌 /" + name + " を Guild " + guildId + " に登録予定");
				}
			}
		}

		// 🌍 グローバルコマンド登録（最大1時間で反映）
		if (!globalCommands.isEmpty()) {
			jda.updateCommands().addCommands(globalCommands).queue();
			System.out.println("✅ グローバルコマンドを登録しました（" + globalCommands.size() + " 件）");
		}

		// 🏠 Guildごとのコマンド登録（即時反映）
		for (Map.Entry<String, List<SlashCommandData>> entry : guildCommandMap.entrySet()) {
			String guildId = entry.getKey();
			List<SlashCommandData> commands = entry.getValue();
			Guild guild = jda.getGuildById(guildId);

			// 🔍 登録しようとしているコマンド一覧をログに表示
			System.out.println("📋 Guild登録対象コマンド（" + guildId + "）: ");
			for (SlashCommandData cmd : commands) {
				System.out.println("  ┗ /" + cmd.getName());
			}

			if (guild != null) {
				guild.updateCommands().addCommands(commands).queue(
						success -> System.out.println("✅ 登録完了 → Guild: " + guild.getName() + " (" + guildId + ") に "
								+ commands.size() + " 件登録しました"),
						error -> System.out
								.println("❌ 登録失敗 → Guild: " + guildId + " に登録中にエラー発生: " + error.getMessage()));
			} else {
				System.out.println("⚠️ Guildが見つかりません（Botが参加していない？）: " + guildId);
			}
		}

	}
}
