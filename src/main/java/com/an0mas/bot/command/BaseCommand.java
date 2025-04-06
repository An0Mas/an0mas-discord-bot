package com.an0mas.bot.command;

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
 */
public abstract class BaseCommand {

    /** 📛 コマンドの名前（例：help, ping） */
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
     * 📛 コマンドの名前を取得（例："help"）
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
     * 🛡️ このコマンドにアクセス制限があるかどうかを返す
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
}
