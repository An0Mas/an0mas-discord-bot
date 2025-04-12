package com.an0mas.bot;

import java.util.List;
import java.util.Objects;

import com.an0mas.bot.command.BaseCommand;
import com.an0mas.bot.command.CommandLoader;
import com.an0mas.bot.command.CommandRegistry;
import com.an0mas.bot.command.CommandsInitializer;
import com.an0mas.bot.config.ConfigLoader;
import com.an0mas.bot.database.DatabaseHelper;
import com.an0mas.bot.database.FeedbackDatabaseHelper;
import com.an0mas.bot.listener.BotJoinListener;
import com.an0mas.bot.listener.ButtonInteractionListener;
import com.an0mas.bot.listener.ModalInteractionListener;
import com.an0mas.bot.listener.ReadyListener;
import com.an0mas.bot.listener.SlashCommandListener;

import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class Main {
	public static void main(String[] args) throws Exception {
		// 💾 データベース初期化
		try {
			DatabaseHelper.initializeDatabase();
			FeedbackDatabaseHelper.initializeDatabase();
		} catch (Exception e) {
			System.err.println("❌ データベースの初期化に失敗しました: " + e.getMessage());
			e.printStackTrace();
			return; // プログラムを終了する
		}

		// 🔐 .envファイルからトークンを読み込む
		Dotenv dotenv = Dotenv.load();
		String token = ConfigLoader.get("DISCORD_TOKEN");
		if (token == null || token.isBlank()) {
			throw new IllegalArgumentException("❌ DISCORD_TOKEN が見つかりません！");
		}

		// 全コマンドを一括登録
		for (BaseCommand command : CommandLoader.getAllCommands()) {
			CommandRegistry.register(command);
		}

		// 🤖 JDAの設定・初期化
		JDABuilder builder = JDABuilder.createDefault(token)
				.setActivity(Activity.playing("An0MasBot 開発中..."))
				.addEventListeners(
						new SlashCommandListener(),
						new ReadyListener(),
						new ModalInteractionListener(),
						new ButtonInteractionListener(),
						new BotJoinListener());

		// 🚀 Botを起動（非同期でログイン開始）
		JDA jda = builder.build();

		// 🕓 起動完了を待機 → コマンド登録
		jda.awaitReady(); // Botがログイン完了するまで待つ
		CommandsInitializer.registerSlashCommands(jda);

		// ⏱️ テストGuildに即時反映（ここ！）
		String testGuildId = ConfigLoader.get("TEST_GUILD_ID");
		if (testGuildId == null || testGuildId.isBlank()) {
			System.err.println("❌ TEST_GUILD_ID が見つかりません！");
		} else {
			Guild testGuild = jda.getGuildById(testGuildId);
			if (testGuild != null) {
				List<SlashCommandData> testCommands = CommandRegistry.getCommands().stream()
						.map(BaseCommand::getSlashCommandData)
						.filter(Objects::nonNull)
						.toList();
				testGuild.updateCommands().addCommands(testCommands).queue();
				System.out.println("🚀 テストサーバーに即時登録しました！");
			}
		}

		// 🛑 シャットダウン時の処理（きれいに終了）
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			System.out.println("🛑 Botを停止します...");
			System.out.flush();
			try {
				Thread.sleep(500);
			} catch (InterruptedException ignored) {
			}
			jda.shutdown(); // Discordとの接続を終了
		}));
	}
}
