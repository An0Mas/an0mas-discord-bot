package com.an0mas.bot.database;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * 📂 DatabaseHelper:
 * SQLiteを使って、コマンド権限・ブラックリスト・設定（メンテナンス状態）などを管理するユーティリティクラス。
 */
public class DatabaseHelper {
	private static final Logger logger = LoggerFactory.getLogger(DatabaseHelper.class);

	private static final HikariDataSource dataSource;

	private static final Dotenv dotenv = Dotenv.load();
	private static final String SCHEMA_FILE = "schemas/schema.sql";

	static {
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl("jdbc:sqlite:" + dotenv.get("DB_PATH", "data/command_permissions.db"));
		config.setMaximumPoolSize(10);
		config.setIdleTimeout(30000); // 30秒間アイドル状態ならコネクションを閉じる
		config.setConnectionTimeout(10000); // 10秒でタイムアウト
		config.setLeakDetectionThreshold(2000); // コネクションリーク検出（2秒）
		dataSource = new HikariDataSource(config);
	}

	private static Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

	// ========== 初期化 ==========

	/**
	 * 📦 DB初期化処理（各種テーブルの作成）
	 */
	public static void initializeDatabase() {
		Path schemaPath = Path.of(SCHEMA_FILE);

		if (!Files.exists(schemaPath)) {
			logger.error("❌ スキーマファイルが見つかりません: {}", SCHEMA_FILE);
			return;
		}

		try (Connection conn = getConnection();
				Statement stmt = conn.createStatement()) {

			String schema = Files.readString(schemaPath);
			stmt.execute(schema);

			logger.info("✅ データベース初期化完了！");

			initializeMaintenanceMode();

		} catch (Exception e) {
			logger.error("❌ データベース初期化中にエラーが発生しました: {}", e.getMessage(), e);
		}
	}

	private static void initializeMaintenanceMode() {
		String checkSql = "SELECT 1 FROM settings WHERE key = 'maintenance_mode'";
		String insertSql = "INSERT INTO settings (key, value, updated_at) VALUES ('maintenance_mode', 'false', datetime('now'))";

		if (!exists(checkSql)) {
			executeUpdate(insertSql);
			logger.info("🛠️ メンテナンスモード初期化: false");
		}
	}

	// ========== メンテナンスモード ==========

	/** 🛠️ 現在のメンテナンスモード状態を取得（true or false） */
	public static boolean isMaintenanceMode() {
		String sql = "SELECT value FROM settings WHERE key = 'maintenance_mode'";
		return querySingleResult(sql, rs -> Boolean.parseBoolean(rs.getString("value")), false);
	}

	/** 🛠️ メンテナンスモードを変更（ON/OFF） */
	public static void setMaintenanceMode(boolean enabled) {
		String sql = "UPDATE settings SET value = ?, updated_at = datetime('now') WHERE key = 'maintenance_mode'";
		executeUpdate(sql, String.valueOf(enabled));
		logger.info("🛠️ メンテナンスモード更新: {}", enabled);
	}

	// ========== コマンド権限 ==========

	public static void addGuildPermission(String guildId, String commandName) {
		executeUpdate("INSERT INTO server_permissions (guild_id, command_name) VALUES (?, ?)", guildId, commandName);
		logger.info("✅ サーバー許可追加: {} → {}", guildId, commandName);
	}

	public static void addUserPermission(String userId, String commandName) {
		executeUpdate("INSERT INTO user_permissions (user_id, command_name) VALUES (?, ?)", userId, commandName);
		logger.info("✅ ユーザー許可追加: {} → {}", userId, commandName);
	}

	public static void removeGuildPermission(String guildId, String commandName) {
		executeUpdate("DELETE FROM server_permissions WHERE guild_id = ? AND command_name = ?", guildId, commandName);
		logger.info("🗑️ サーバー許可を削除: {} → {}", guildId, commandName);
	}

	public static void removeUserPermission(String userId, String commandName) {
		executeUpdate("DELETE FROM user_permissions WHERE user_id = ? AND command_name = ?", userId, commandName);
		logger.info("🗑️ ユーザー許可を削除: {} → {}", userId, commandName);
	}
	
	// ========== 一覧取得（Guild/User） ==========

	public static boolean isGuildCommandAllowed(String guildId, String commandName) {
		return exists("SELECT 1 FROM server_permissions WHERE guild_id = ? AND command_name = ?", guildId, commandName);
	}

	public static boolean isUserCommandAllowed(String userId, String commandName) {
		return exists("SELECT 1 FROM user_permissions WHERE user_id = ? AND command_name = ?", userId, commandName);
	}

	public static List<String> getGuildsAllowedForCommand(String commandName) {
		return queryList("SELECT DISTINCT guild_id FROM server_permissions WHERE command_name = ?",
				rs -> rs.getString("guild_id"), commandName);
	}

	public static List<String> getCommandsAllowedForGuild(String guildId) {
		return queryList("SELECT DISTINCT command_name FROM server_permissions WHERE guild_id = ?",
				rs -> rs.getString("command_name"), guildId);
	}

	public static List<String> getCommandsAllowedForUser(String userId) {
		return queryList("SELECT DISTINCT command_name FROM user_permissions WHERE user_id = ?",
				rs -> rs.getString("command_name"), userId);
	}

	public static Map<String, List<String>> getAllGuildPermissions() {
		String sql = "SELECT guild_id, command_name FROM server_permissions";
		Map<String, List<String>> permissions = new HashMap<>();

		queryList(sql, rs -> {
			String guildId = rs.getString("guild_id");
			String commandName = rs.getString("command_name");
			permissions.computeIfAbsent(guildId, k -> new ArrayList<>()).add(commandName);
			return null; // Return null, as the map is already updated.
		});

		return permissions;
	}

	public static Map<String, List<String>> getAllUserPermissions() {
		String sql = "SELECT user_id, command_name FROM user_permissions";
		Map<String, List<String>> permissions = new HashMap<>();

		queryList(sql, rs -> {
			String userId = rs.getString("user_id");
			String commandName = rs.getString("command_name");
			permissions.computeIfAbsent(userId, k -> new ArrayList<>()).add(commandName);
			return null; // Return null, as the map is already updated.
		});

		return permissions;
	}

	// ========== ブラックリスト管理 ==========

	public static boolean isUserBlacklisted(String userId) {
		return exists("SELECT 1 FROM blacklist WHERE user_id = ?", userId);
	}

	public static void addUserToBlacklist(String userId) {
		executeUpdate("INSERT OR IGNORE INTO blacklist (user_id) VALUES (?)", userId);
		logger.info("⛔ ブラックリスト追加: {}", userId);
	}

	public static void removeUserFromBlacklist(String userId) {
		executeUpdate("DELETE FROM blacklist WHERE user_id = ?", userId);
		logger.info("✅ ブラックリスト解除: {}", userId);
	}

	public static List<String> getAllBlacklistedUsers() {
		return queryList("SELECT user_id FROM blacklist", rs -> rs.getString("user_id"));
	}

	// ========== 共通ユーティリティ ==========

	private static boolean exists(String sql, String... params) {
		return querySingleResult(sql, ResultSet::next, false, params);
	}

	private static int executeUpdate(String sql, String... params) {
		try (Connection conn = getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql)) {

			for (int i = 0; i < params.length; i++) {
				pstmt.setString(i + 1, params[i]);
			}
			return pstmt.executeUpdate();
		} catch (Exception e) {
			logger.error("❌ クエリ実行中にエラーが発生しました: {}", e.getMessage(), e);
			return 0;
		}
	}

	private static <T> T querySingleResult(String sql, ResultSetMapper<T> mapper, T defaultValue, String... params) {
		try (Connection conn = getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql)) {

			for (int i = 0; i < params.length; i++) {
				pstmt.setString(i + 1, params[i]);
			}

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return mapper.map(rs);
				}
			}
		} catch (Exception e) {
			logger.error("❌ クエリ実行中にエラーが発生しました: {}", e.getMessage(), e);
		}
		return defaultValue;
	}

	private static <T> List<T> queryList(String sql, ResultSetMapper<T> mapper, String... params) {
		List<T> results = new ArrayList<>();
		try (Connection conn = getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql)) {

			for (int i = 0; i < params.length; i++) {
				pstmt.setString(i + 1, params[i]);
			}

			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					results.add(mapper.map(rs));
				}
			}
		} catch (Exception e) {
			logger.error("❌ クエリ実行中にエラーが発生しました: {}", e.getMessage(), e);
		}
		return results;
	}

	@FunctionalInterface
	private interface ResultSetMapper<T> {
		T map(ResultSet rs) throws SQLException;
	}
}