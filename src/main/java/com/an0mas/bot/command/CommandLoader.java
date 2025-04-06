package com.an0mas.bot.command;

import java.util.List;

/**
 * 📦 すべてのコマンドをまとめて登録するためのローダークラス
 */
public class CommandLoader {

    /**
     * 📋 登録したいコマンド一覧を返す
     */
    public static List<BaseCommand> getAllCommands() {
        return List.of(
            new HelpCommand(),
            new CmdAccessCommand(),
            new FeedbackCommand(),
            new FeedbackListCommand()
            // 今後ここに新しいコマンドを追加していくだけ！
        );
    }
}
