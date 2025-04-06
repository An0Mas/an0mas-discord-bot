package com.an0mas.bot.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.an0mas.bot.model.FeedbackEntry;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * 💬 FeedbackDatabaseHelper:
 * ユーザーから送られたフィードバックをSQLiteで管理するユーティリティ。
 */
public class FeedbackDatabaseHelper {

	private static final Dotenv dotenv = Dotenv.load();
	private static final String DB_URL = "jdbc:sqlite:" + dotenv.get("FEEDBACK_DB_PATH", "data/feedbacks.db");

	/**
	 * 📦 テーブル初期化処理
	 */
	public static void initializeDatabase() {
		try (Connection conn = DriverManager.getConnection(DB_URL);
				Statement stmt = conn.createStatement()) {

			String createTable = """
					CREATE TABLE IF NOT EXISTS feedback (
					    id INTEGER PRIMARY KEY AUTOINCREMENT,
					    user_id TEXT,
					    user_name TEXT,
					    title TEXT NOT NULL,
					    content TEXT NOT NULL,
					    timestamp TEXT NOT NULL
					);
					""";

			stmt.execute(createTable);
			System.out.println("✅ フィードバックDB初期化完了！");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 💾 フィードバックを保存（新規追加）
	 */
	public static void insertFeedback(String userId, String userName, String title, String content, String timestamp) {
		String sql = "INSERT INTO feedback (user_id, user_name, title, content, timestamp) VALUES (?, ?, ?, ?, ?)";
		try (Connection conn = DriverManager.getConnection(DB_URL);
				PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, userId);
			pstmt.setString(2, userName);
			pstmt.setString(3, title);
			pstmt.setString(4, content);
			pstmt.setString(5, timestamp);
			pstmt.executeUpdate();
			System.out.println("📨 フィードバックを保存: " + title + "（送信者: " + userName + "）");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 📥 全フィードバックの取得（新しい順）
	 */
	public static List<FeedbackEntry> getAllFeedbacks() {
		List<FeedbackEntry> list = new ArrayList<>();
		String sql = "SELECT * FROM feedback ORDER BY id DESC";

		try (Connection conn = DriverManager.getConnection(DB_URL);
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {

			while (rs.next()) {
				FeedbackEntry entry = new FeedbackEntry(
						rs.getInt("id"),
						rs.getString("user_id"),
						rs.getString("user_name"),
						rs.getString("title"),
						rs.getString("content"),
						rs.getString("timestamp"));
				list.add(entry);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * 🔍 IDから特定のフィードバックを取得
	 */
	public static FeedbackEntry getFeedbackById(int id) {
		String sql = "SELECT * FROM feedback WHERE id = ?";
		try (Connection conn = DriverManager.getConnection(DB_URL);
				PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setInt(1, id);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return new FeedbackEntry(
							rs.getInt("id"),
							rs.getString("user_id"),
							rs.getString("user_name"),
							rs.getString("title"),
							rs.getString("content"),
							rs.getString("timestamp"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 🗑️ 指定されたIDのフィードバックを削除
	 */
	public static void deleteFeedbackById(int id) {
		String sql = "DELETE FROM feedback WHERE id = ?";

		try (Connection conn = DriverManager.getConnection(DB_URL);
				PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setInt(1, id);
			int affected = stmt.executeUpdate();

			if (affected > 0) {
				System.out.println("🗑️ フィードバック削除: ID = " + id);
			} else {
				System.out.println("⚠️ 該当するフィードバックが見つかりませんでした（ID: " + id + "）");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 📃 ページごとのフィードバックを取得
	 */
	public static List<FeedbackEntry> getFeedbacksPaged(int offset, int limit) {
		List<FeedbackEntry> list = new ArrayList<>();
		String sql = "SELECT * FROM feedback ORDER BY id DESC LIMIT ? OFFSET ?";

		try (Connection conn = DriverManager.getConnection(DB_URL);
				PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setInt(1, limit);
			stmt.setInt(2, offset);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				FeedbackEntry entry = new FeedbackEntry(
						rs.getInt("id"),
						rs.getString("user_id"),
						rs.getString("user_name"),
						rs.getString("title"),
						rs.getString("content"),
						rs.getString("timestamp"));
				list.add(entry);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * 📊 フィードバックの総件数を取得
	 */
	public static int getFeedbackCount() {
		String sql = "SELECT COUNT(*) FROM feedback";
		try (Connection conn = DriverManager.getConnection(DB_URL);
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {

			if (rs.next()) {
				return rs.getInt(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * 🔢 フィードバックの総件数を取得
	 */
	public static int getTotalFeedbackCount() {
		String sql = "SELECT COUNT(*) FROM feedback";

		try (Connection conn = DriverManager.getConnection(DB_URL);
			 PreparedStatement stmt = conn.prepareStatement(sql);
			 ResultSet rs = stmt.executeQuery()) {
			if (rs.next()) {
				return rs.getInt(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

}
