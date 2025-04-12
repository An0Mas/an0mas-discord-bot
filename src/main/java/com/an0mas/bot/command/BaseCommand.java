package com.an0mas.bot.command;

import com.an0mas.bot.util.AccessControlUtil;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

/**
 * 🌟 すべてのスラッシュコマンドが継承する「共通のベースクラス」
 * 
 * 各コマンドはこのクラスを継承して、以下の機能を実装します：
 * - コマンド名・説明の定義（name, description）
 * - 実行処理の定義（execute）
 * - オプションでスラッシュコマンド定義（getSlashCommandData）
 * - 任意でアクセス制限の指定（isRestricted）
 * 
 * 主なユースケース：
 * - このクラスを継承して、各種スラッシュコマンドを簡単に実装できます。
 * - 例: HelpCommand, PingCommand など
 */
public abstract class BaseCommand {

	/** 🔛 コマンドの名前（例：help, ping） */
	private final String name;

	/** 📝 コマンドの説明文（例：「Botが応答するか確認します」） */
	private final String description;

	/**
	 * 🏗️ コンストラクター（名前と説明を受け取って保持）
	 * 
	 * @param name コマンド名（スラッシュなし。例："help"）
	 * @param description コマンドの説明文（/help に表示される）
	 */
	public BaseCommand(String name, String description) {
		this.name = name;
		this.description = description;
	}

	/**
	 * 🔛 コマンドの名前を取得（例："help"）
	 * 
	 * @return コマンド名（スラッシュなし）
	 */
	public String getName() {
		return name;
	}

	/**
	 * 📝 コマンドの説明文を取得
	 * 
	 * @return 説明テキスト（/help に表示される概要）
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * 🚀 コマンドが実行されたときの本体処理
	 * 
	 * 各サブクラスで具体的な処理を実装してください。
	 * 
	 * @param event コマンド実行時のイベント情報
	 */
	public abstract void execute(SlashCommandInteractionEvent event);

	/**
	 * 🚡 このコマンドにアクセス制限があるかどうかを返す
	 * 
	 * - デフォルトでは false（誰でも使える）
	 * - 管理者向けなどで制限したい場合は true にする
	 * 
	 * @return true = 使用制限あり / false = 誰でも使える
	 */
	public boolean isRestricted() {
		return false;
	}

	/**
	 * 📦 このコマンドをスラッシュコマンドとして登録するための情報
	 * 
	 * - 通常はオーバーライドして、SlashCommandData を返す
	 * - 登録不要な場合は null を返せば OK
	 * 
	 * @return スラッシュコマンド定義（または null）
	 */
	public SlashCommandData getSlashCommandData() {
		return null;
	}

	/**
	 * ⛔ 実行前に共通のアクセス制限チェックを行う
	 * 
	 * ブラックリストに登録されている場合は実行をブロックします。
	 * すべてのコマンドで自動的に適用されるよう、SlashCommandListener 側で呼び出してください。
	 *
	 * @param event コマンド実行イベント
	 */
	public void executeWithCheck(SlashCommandInteractionEvent event) {
        if (AccessControlUtil.isBlocked(event.getUser().getId())) {
            System.out.println("ブロックされたユーザーがコマンドを試行: " + event.getUser().getId());
            event.reply("\u26d4 あなたはこのBotの利用を制限されています。").setEphemeral(true).queue();
            return;
        }
        this.execute(event);
    }
}
