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
				new HelpCommand(), // 🆘 /help：Botの使い方ガイドを表示
				new CmdAccessCommand(), // 🛡️ /cmdaccess：使用許可の追加・削除・確認
				new FeedbackCommand(), // 📨 /feedback：モーダルで意見や要望を送信
				new FeedbackListCommand(), // 📋 /feedbacklist：受け取ったフィードバックを一覧表示
				new BlockUserCommand() // ⛔ /blockuser：ユーザーをブロック／解除／リスト表示（開発者専用）
		// 🧩 今後ここに新しいコマンドを追加していくだけ！
		);
	}
	
	//💡 コマンドが増えてきたら下のReflectionsコードに切り替えてOK！
//	Reflections reflections = new Reflections("com.an0mas.bot.command");
//
//	return reflections.getSubTypesOf(BaseCommand.class).stream()
//	    .filter(clazz -> !Modifier.isAbstract(clazz.getModifiers()))
//	    .map(clazz -> {
//	        try {
//	            return clazz.getDeclaredConstructor().newInstance();
//	        } catch (Exception e) {
//	            System.err.println("⚠ コマンドの生成に失敗: " + clazz.getName());
//	            e.printStackTrace();
//	            return null;
//	        }
//	    })
//	    .filter(Objects::nonNull)
//	    .collect(Collectors.toList());
}
