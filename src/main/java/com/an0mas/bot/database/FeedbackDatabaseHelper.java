package com.an0mas.bot.database;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.an0mas.bot.model.FeedbackEntry;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * 💬 FeedbackDatabaseHelper:
 * ユーザーから送られたフィードバックをSQLiteで管理するユーティリティ。
 */
public class FeedbackDatabaseHelper {
	private static final Logger logger = LoggerFactory.getLogger(FeedbackDatabaseHelper.class);

	private static final HikariDataSource dataSource;

	private static final Dotenv dotenv = Dotenv.load();
	private static final String SCHEMA_FILE = "schemas/feedback_schema.sql";

	static {
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl("jdbc:sqlite:" + dotenv.get("FEEDBACK_DB_PATH", "data/feedbacks.db"));
		config.setMaximumPoolSize(10); // 最大プールサイズ
		config.setMinimumIdle(5);     // 最小アイドルコネクション数
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
	 * 📦 フィードバック用DBの初期化
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

			logger.info("✅ フィードバックDB初期化完了！");

		} catch (Exception e) {
			logger.error("❌ フィードバックDB初期化中にエラーが発生しました: {}", e.getMessage(), e);
		}
	}

	// ========== フィードバック操作 ==========

	/**
	 * 💾 フィードバックを保存（新規追加）
	 */
	public static void insertFeedback(String userId, String userName, String title, String content, String timestamp) {
		String sql = "INSERT INTO feedback (user_id, user_name, title, content, timestamp) VALUES (?, ?, ?, ?, ?)";
		executeUpdate(sql, userId, userName, title, content, timestamp);
		logger.info("📨 フィードバックを保存: {}（送信者: {}）", title, userName);
	}

	/**
	 * 📥 全フィードバックの取得（新しい順）
	 */
	public static List<FeedbackEntry> getAllFeedbacks() {
		String sql = "SELECT * FROM feedback ORDER BY id DESC";
		return queryList(sql, rs -> new FeedbackEntry(
				rs.getInt("id"),
				rs.getString("user_id"),
				rs.getString("user_name"),
				rs.getString("title"),
				rs.getString("content"),
				rs.getString("timestamp")));
	}

	/**
	 * 🔍 IDから特定のフィードバックを取得
	 */
	public static FeedbackEntry getFeedbackById(int id) {
		String sql = "SELECT * FROM feedback WHERE id = ?";
		return querySingleResult(sql, rs -> new FeedbackEntry(
				rs.getInt("id"),
				rs.getString("user_id"),
				rs.getString("user_name"),
				rs.getString("title"),
				rs.getString("content"),
				rs.getString("timestamp")), null, id);
	}

	/**
	 * 🗑️ 指定されたIDのフィードバックを削除
	 */
	public static void deleteFeedbackById(int id) {
		String sql = "DELETE FROM feedback WHERE id = ?";
		int affected = executeUpdate(sql, id);
		if (affected > 0) {
			logger.info("🗑️ フィードバック削除: ID = {}", id);
		} else {
			logger.warn("⚠️ 該当するフィードバックが見つかりませんでした（ID: {}）", id);
		}
	}

	/**
	 * 📃 ページごとのフィードバックを取得
	 */
	public static List<FeedbackEntry> getFeedbacksPaged(int offset, int limit) {
		String sql = "SELECT * FROM feedback ORDER BY id DESC LIMIT ? OFFSET ?";
		return queryList(sql, rs -> new FeedbackEntry(
				rs.getInt("id"),
				rs.getString("user_id"),
				rs.getString("user_name"),
				rs.getString("title"),
				rs.getString("content"),
				rs.getString("timestamp")), limit, offset);
	}

	/**
	 * 📊 フィードバックの総件数を取得
	 */
	public static int getFeedbackCount() {
		String sql = "SELECT COUNT(*) FROM feedback";
		return querySingleResult(sql, rs -> rs.getInt(1), 0);
	}

	// ========== 共通ユーティリティ ==========

	private static int executeUpdate(String sql, Object... params) {
		try (Connection conn = getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql)) {

			for (int i = 0; i < params.length; i++) {
				pstmt.setObject(i + 1, params[i]);
			}
			return pstmt.executeUpdate();
		} catch (Exception e) {
			logger.error("❌ クエリ実行中にエラーが発生しました: {}", e.getMessage(), e);
			return 0;
		}
	}

	private static <T> T querySingleResult(String sql, ResultSetMapper<T> mapper, T defaultValue, Object... params) {
		try (Connection conn = getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql)) {

			for (int i = 0; i < params.length; i++) {
				pstmt.setObject(i + 1, params[i]);
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

	private static <T> List<T> queryList(String sql, ResultSetMapper<T> mapper, Object... params) {
		List<T> results = new ArrayList<>();
		try (Connection conn = getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql)) {

			for (int i = 0; i < params.length; i++) {
				pstmt.setObject(i + 1, params[i]);
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