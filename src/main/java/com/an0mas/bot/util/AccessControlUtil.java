package com.an0mas.bot.util;

import com.an0mas.bot.database.DatabaseHelper;

/**
 * ⛔ AccessControlUtil:
 * ブラックリストによるユーザーの利用制限をチェックするユーティリティクラス。
 */
public class AccessControlUtil {

    /**
     * ⛔ 指定されたユーザーがブロックされているかをチェック
     * 
     * @param userId ユーザーID
     * @return ブロックされていれば true
     */
    public static boolean isBlocked(String userId) {
        return DatabaseHelper.isUserBlacklisted(userId);
    }

    /**
     * ⛔ 指定されたユーザーをブロックリストに追加
     */
    public static void blockUser(String userId) {
        DatabaseHelper.addUserToBlacklist(userId);
    }

    /**
     * ⛔ 指定されたユーザーをブロックリストから解除
     */
    public static void unblockUser(String userId) {
        DatabaseHelper.removeUserFromBlacklist(userId);
    }
}
