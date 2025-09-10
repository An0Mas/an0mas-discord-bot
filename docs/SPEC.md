# 🧭 an0mas-discord-bot 仕様サマリ（SPEC）
_最終更新: 2025-09-10 JST_

> 配置場所: **`/docs/SPEC.md`**（リポジトリ直下に `docs` フォルダを作成し、その中に保存）

---

## 📂 リポジトリ
- 現行: https://github.com/An0Mas/an0mas-discord-bot
- 旧: DiscordBot4An0Mas2.zip（参考用。通常は未参照。必要時に最新を再アップしてから利用）

---

## 🎯 目的・非目的
- **目的**: JDA ベースの Discord Bot と Spring Boot + Thymeleaf の WebUI で  
  **①フィードバック収集 ②メンテナンス/権限運用 ③（将来）未読管理** を提供する。
- **非目的**: 生成AIの組み込み、複雑なWeb管理画面（現時点では対象外／段階的に拡張）。

---

## 🛠 技術スタック
- 言語: Java（JDK 要件は `pom.xml` を正）
- Discord: JDA（Slash Command 中心、イベントは Listener 集約）
- Web: Spring Boot + Thymeleaf（共通レイアウト `templates/base.html`）
- DB: SQLite（将来 PostgreSQL 移行視野）
- 定数: `BotConstants`（開発者ID、通知チャンネルID、運用フラグ 等）
- DBアクセス: `DatabaseHelper` に集約（SQL 方言は最小化）

---

## 🧩 主要コンポーネント（クラス）
- `BotConstants` … 重要ID/固定値の集約  
- `DatabaseHelper` … DB接続・CRUD（`feedbacks` / `server_permissions` / `user_permissions` 等）  
- `SlashCommandListener` … Slashイベントの受口・振り分け  
- `ButtonInteractionListener` … ボタン/ページング等のハンドラ  
- **コマンド実装（現行クラス名）**
  - `FeedbackCommand`
  - `FeedbackListCommand`
  - `HelpCommand`
  - `CmdAccessCommand`
  - `BlockUserCommand`
- **共通基底**
  - `BaseCommand` … 共通前処理（ブロック判定、権限/共通ヘルパの導入点）

---

## 🏷️ コマンド命名規約（現行＋ルール）
- 原則: **`<ClassName>` から末尾の `Command` を除き、小文字化 → `/<name>`**  
  例: `FeedbackCommand` → `/feedback`、`FeedbackListCommand` → `/feedbacklist`
- 例外が必要なら各クラスに **定数 `COMMAND`** を設け、そこで上書き（導入時は SPEC に追記）。

**コマンド一覧（現行の推定マッピング）**
- FeedbackCommand → `/feedback`
- FeedbackListCommand → `/feedbacklist`
- HelpCommand → `/help`
- CmdAccessCommand → `/cmdaccess`
- BlockUserCommand → `/blockuser`

---

## 🔐 権限モデル（仕様）
- **評価順序**  
  1) **Developer 特権**（`BotConstants.DEVELOPER_IDS` は常に通す）  
  2) **GuildOnly 判定**（DMは拒否）  
  3) **AllowLists**（`server_permissions` / `user_permissions`）  
  4) **defaultOpen**（許可が何も設定されていない時の既定動作）
- **AllowLists の意味**  
  - いずれかのリストが1件以上ある場合、**列挙された対象のみ許可**（ユーザーID一致 or ギルドID一致でOK）。
- **既定**  
  - コマンドは原則 **defaultOpen = true**（制限が無ければ誰でも使用可）。  
  - `FeedbackListCommand` は **GuildOnly** とする（DMは不可）。
- **ユーザー向けメッセージ（推奨固定文言）**  
  - 権限なし：**このコマンドを使用する権限がありません。**  
  - Guild限定でDM：**このコマンドはサーバー内でのみ使用できます。**  
  - 予期せぬ失敗：**処理中にエラーが発生しました。時間をおいてお試しください。**

> **現状差分（実装観測）**  
> - ブロック時: `⛔ あなたはこのBotの利用を制限されています。`（`BaseCommand`）  
> - 開発者専用: `⚠️ このコマンドは開発者専用です。`（`BlockUserCommand` / `CmdAccessCommand`）  
> - → 固定文言を上記“推奨”に寄せる場合は小修正で統一可。

---

## 📨 フィードバック機能
### `/feedback`（`FeedbackCommand`）
- **モーダル**: `feedback_modal`  
  - 入力1: `title`（件名、必須、max=100）  
  - 入力2: `content`（内容、必須、max=1000）  
  - 入力3: `anonymous`（任意、`yes` で匿名扱い）  
- **保存**: `feedbacks` に記録（匿名風だが開発者は投稿者特定可能）  
- **返信**: モーダル表示→送信後に受領通知（エフェメラル想定）

### `/feedbacklist`（`FeedbackListCommand`）
- **GuildOnly**: ✅（DM不可）  
- **表示**: Embed＋ボタンでページング（新しい順）  
- **ヘッダ**: `全 {total} 件中 {from}〜{to} 件`

---

## 🧱 Button / Interaction 規約
- **ID 形式**: `domain:action:arg1:arg2`（半角英数、`:` 区切り）  
- **検証**: 発行者一致（`userId`）と有効期限（必要なら `nonce`）の最低限チェック  
- **期限切れ**: エフェメラル短文で通知（例: `操作の有効期限が切れました。`）

---

## 📄 ページング契約
- **デフォルトページサイズ**: 10  
- **並び順**: `created_at DESC`  
- **ヘッダ表示**: `全 {total} 件中 {from}〜{to} 件`  
- **境界**: 先頭/末尾ページのボタン状態を無効化

---

## 🗄️ データモデル（採用スキーマ）
```sql
-- フィードバック
CREATE TABLE IF NOT EXISTS feedbacks (
  id          INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id     TEXT    NOT NULL,
  guild_id    TEXT,
  content     TEXT    NOT NULL,
  created_at  INTEGER NOT NULL,   -- epoch millis (UTC)
  is_read     INTEGER NOT NULL DEFAULT 0,
  is_new      INTEGER NOT NULL DEFAULT 1
);
CREATE INDEX IF NOT EXISTS idx_feedbacks_created ON feedbacks(created_at DESC);

-- 権限（AllowLists）
CREATE TABLE IF NOT EXISTS server_permissions (
  command_name TEXT NOT NULL,
  guild_id     TEXT NOT NULL,
  PRIMARY KEY (command_name, guild_id)
);
CREATE TABLE IF NOT EXISTS user_permissions (
  command_name TEXT NOT NULL,
  user_id      TEXT NOT NULL,
  PRIMARY KEY (command_name, user_id)
);
```

> 実装と差異があれば**実装に合わせて本SPECを更新**する（SPECが常に正）。

---

## 🧰 例外・エラーハンドリング
- ユーザー向けは**日本語・短文・エフェメラル**、内部はログ（INFO/WARN/ERROR）  
- DB一時失敗は**1回だけ再試行（≤100ms）**  
- メンテONのときは一般コマンドを抑止（開発者は通す）

---

## ⚙️ 運用・デプロイ
- Slash登録: グローバルと Guild 専用を使い分け（開発中はテストGuild優先）  
- バックアップ: SQLite を**日次スナップショット（保持7世代）**  
- タイムゾーン: 保存は **UTC epoch**、表示は Discord ロケール

---

## 🧪 開発ルール（運用モード）
- **最小差分**で実装、**既存命名/構成は原則維持**  
- DBアクセスは **`DatabaseHelper` 経由**  
- 例外時は固定短文（上記メッセージ）  
- **会話モード**：  
  - **セーフ**… 局所修正/追記。大胆変更は「提案:」に分離（コードは出さない）  
  - **RFC**… 根本見直し・ライブラリ追加の**提案のみ**（移行/影響/ロールバック/参考URL）

---

## 🔭 ロードマップ（短/中期）
1. **固定メッセの統一**（権限なし/DM禁止/一般エラーを共通化）  
2. **権限チェックの単一入口化**（`BaseCommand` 前処理で集約。短TTLキャッシュ導入は任意）  
3. **未読/新着フラグの活用**（/feedbacklist に強調、既読操作）  
4. **ギルド設定ページ**（WebUI）＆設定タイルUI  
5. **PG移行の前提整理**（SQL 方言の最小化、移行スクリプト雛形）

---

## 🧾 変更履歴
- 2025-09-10: 初版（現行コード観測に合わせてクラス名・文言方針を反映）
