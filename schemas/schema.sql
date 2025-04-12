-- テーブル作成
CREATE TABLE IF NOT EXISTS server_permissions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    guild_id TEXT NOT NULL,
    command_name TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS user_permissions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id TEXT NOT NULL,
    command_name TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS blacklist (
    user_id TEXT PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS settings (
    key TEXT PRIMARY KEY,
    value TEXT NOT NULL,
    updated_at TEXT NOT NULL
);