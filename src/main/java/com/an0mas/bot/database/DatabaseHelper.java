package com.an0mas.bot.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * 📂 DatabaseHelper:
 * SQLiteを使って、コマンド権限・ブラックリスト・設定（メンテナンス状態）などを管理するユーティリティクラス。
 */
public class DatabaseHelper {

	private static final Dotenv dotenv = Dotenv.load();
	private static final String DB_URL = "jdbc:sqlite:" + dotenv.get("DB_PATH", "data/command_permissions.db");

	// ========== 初期化 ==========

	/**
	 * 📦 DB初期化処理（各種テーブルの作成）
	 */
	public static void initializeDatabase() {
		try (Connection conn = DriverManager.getConnection(DB_URL);
				Statement stmt = conn.createStatement()) {

			String createServerTable = """
						CREATE TABLE IF NOT EXISTS server_permissions (
						    id INTEGER PRIMARY KEY AUTOINCREMENT,
						    guild_id TEXT NOT NULL,
						    command_name TEXT NOT NULL
						);
					""";

			String createUserTable = """
						CREATE TABLE IF NOT EXISTS user_permissions (
						    id INTEGER PRIMARY KEY AUTOINCREMENT,
						    user_id TEXT NOT NULL,
						    command_name TEXT NOT NULL
						);
					""";

			String createBlacklistTable = """
						CREATE TABLE IF NOT EXISTS blacklist (
						    user_id TEXT PRIMARY KEY
						);
					""";

			String createSettingsTable = """
						CREATE TABLE IF NOT EXISTS settings (
						    key TEXT PRIMARY KEY,
						    value TEXT NOT NULL,
						    updated_at TEXT NOT NULL
						);
					""";

			stmt.execute(createServerTable);
			stmt.execute(createUserTable);
			stmt.execute(createBlacklistTable);
			stmt.execute(createSettingsTable);

			// 🔧 メンテナンスモードが未設定なら false に初期化
			ResultSet rs = stmt.executeQuery("SELECT 1 FROM settings WHERE key = 'maintenance_mode'");
			if (!rs.next()) {
				stmt.execute(
						"INSERT INTO settings (key, value, updated_at) VALUES ('maintenance_mode', 'false', datetime('now'))");
				System.out.println("🛠️ メンテナンスモード初期化: false");
			}

			System.out.println("✅ データベース初期化完了！");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// ========== メンテナンスモード ==========

	/** 🛠️ 現在のメンテナンスモード状態を取得（true or false） */
	public static boolean isMaintenanceMode() {
		String sql = "SELECT value FROM settings WHERE key = 'maintenance_mode'";
		try (Connection conn = DriverManager.getConnection(DB_URL);
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {

			if (rs.next()) {
				return Boolean.parseBoolean(rs.getString("value"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/** 🛠️ メンテナンスモードを変更（ON/OFF） */
	public static void setMaintenanceMode(boolean enabled) {
		String sql = "UPDATE settings SET value = ?, updated_at = datetime('now') WHERE key = 'maintenance_mode'";
		executeUpdate(sql, String.valueOf(enabled));
		System.out.println("🛠️ メンテナンスモード更新: " + enabled);
	}

	// ========== コマンド権限（Guild/User） ==========

	public static void addGuildPermission(String guildId, String commandName) {
		executeUpdate("INSERT INTO server_permissions (guild_id, command_name) VALUES (?, ?)", guildId, commandName);
		System.out.println("✅ サーバー許可追加: " + guildId + " → " + commandName);
	}

	public static void addUserPermission(String userId, String commandName) {
		executeUpdate("INSERT INTO user_permissions (user_id, command_name) VALUES (?, ?)", userId, commandName);
		System.out.println("✅ ユーザー許可追加: " + userId + " → " + commandName);
	}

	public static void removeGuildPermission(String guildId, String commandName) {
		int affected = executeUpdate("DELETE FROM server_permissions WHERE guild_id = ? AND command_name = ?", guildId,
				commandName);
		System.out.println(affected > 0 ? "🗑️ サーバーの許可を削除: " + guildId : "⚠️ 指定されたサーバー許可が見つかりません");
	}

	public static void removeUserPermission(String userId, String commandName) {
		int affected = executeUpdate("DELETE FROM user_permissions WHERE user_id = ? AND command_name = ?", userId,
				commandName);
		System.out.println(affected > 0 ? "🗑️ ユーザーの許可を削除: " + userId : "⚠️ 指定されたユーザー許可が見つかりません");
	}

	public static boolean isGuildCommandAllowed(String guildId, String commandName) {
		return exists("SELECT 1 FROM server_permissions WHERE guild_id = ? AND command_name = ?", guildId, commandName);
	}

	public static boolean isUserCommandAllowed(String userId, String commandName) {
		return exists("SELECT 1 FROM user_permissions WHERE user_id = ? AND command_name = ?", userId, commandName);
	}

	// ========== ブラックリスト管理 ==========

	public static boolean isUserBlacklisted(String userId) {
		return exists("SELECT 1 FROM blacklist WHERE user_id = ?", userId);
	}

	public static void addUserToBlacklist(String userId) {
		executeUpdate("INSERT OR IGNORE INTO blacklist (user_id) VALUES (?)", userId);
		System.out.println("⛔ ブラックリスト追加: " + userId);
	}

	public static void removeUserFromBlacklist(String userId) {
		int affected = executeUpdate("DELETE FROM blacklist WHERE user_id = ?", userId);
		System.out.println(affected > 0 ? "✅ ブラックリスト解除: " + userId : "⚠️ 指定されたユーザーはブロックされていません");
	}

	public static List<String> getAllBlacklistedUsers() {
		List<String> userIds = new ArrayList<>();
		String sql = "SELECT user_id FROM blacklist";

		try (Connection conn = DriverManager.getConnection(DB_URL);
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {

			while (rs.next()) {
				userIds.add(rs.getString("user_id"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return userIds;
	}

	// ========== 一覧取得（Guild/User） ==========

	public static List<String> getGuildsAllowedForCommand(String commandName) {
		List<String> guildIds = new ArrayList<>();
		String sql = "SELECT DISTINCT guild_id FROM server_permissions WHERE command_name = ?";

		try (Connection conn = DriverManager.getConnection(DB_URL);
				PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, commandName);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				guildIds.add(rs.getString("guild_id"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return guildIds;
	}

	public static List<String> getCommandsAllowedForGuild(String guildId) {
		List<String> commands = new ArrayList<>();
		String sql = "SELECT DISTINCT command_name FROM server_permissions WHERE guild_id = ?";

		try (Connection conn = DriverManager.getConnection(DB_URL);
				PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, guildId);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				commands.add(rs.getString("command_name"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return commands;
	}

	public static List<String> getCommandsAllowedForUser(String userId) {
		List<String> commands = new ArrayList<>();
		String sql = "SELECT DISTINCT command_name FROM user_permissions WHERE user_id = ?";

		try (Connection conn = DriverManager.getConnection(DB_URL);
				PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, userId);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				commands.add(rs.getString("command_name"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return commands;
	}

	public static Map<String, List<String>> getAllGuildPermissions() {
		Map<String, List<String>> map = new HashMap<>();
		String sql = "SELECT guild_id, command_name FROM server_permissions";

		try (Connection conn = DriverManager.getConnection(DB_URL);
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {

			while (rs.next()) {
				String guildId = rs.getString("guild_id");
				String command = rs.getString("command_name");
				map.computeIfAbsent(guildId, k -> new ArrayList<>()).add(command);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}

	public static Map<String, List<String>> getAllUserPermissions() {
		Map<String, List<String>> map = new HashMap<>();
		String sql = "SELECT user_id, command_name FROM user_permissions";

		try (Connection conn = DriverManager.getConnection(DB_URL);
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {

			while (rs.next()) {
				String userId = rs.getString("user_id");
				String command = rs.getString("command_name");
				map.computeIfAbsent(userId, k -> new ArrayList<>()).add(command);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}

	// ========== 共通ユーティリティ ==========

	private static boolean exists(String sql, String... params) {
		try (Connection conn = DriverManager.getConnection(DB_URL);
				PreparedStatement pstmt = conn.prepareStatement(sql)) {

			for (int i = 0; i < params.length; i++) {
				pstmt.setString(i + 1, params[i]);
			}
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private static int executeUpdate(String sql, String... params) {
		try (Connection conn = DriverManager.getConnection(DB_URL);
				PreparedStatement pstmt = conn.prepareStatement(sql)) {

			for (int i = 0; i < params.length; i++) {
				pstmt.setString(i + 1, params[i]);
			}
			return pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
}
