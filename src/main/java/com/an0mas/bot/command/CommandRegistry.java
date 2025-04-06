package com.an0mas.bot.command;

import java.util.ArrayList;
import java.util.List;

public class CommandRegistry {

    private static final List<BaseCommand> commands = new ArrayList<>();

    // コマンドを登録する
    public static void register(BaseCommand command) {
        commands.add(command);
    }

    // 登録されたコマンド一覧を取得
    public static List<BaseCommand> getCommands() {
        return commands;
    }

    // コマンド名から取得
    public static BaseCommand getByName(String name) {
        return commands.stream()
                .filter(cmd -> cmd.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}
