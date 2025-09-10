# 🏗️ ARCHITECTURE — an0mas-discord-bot
_最終更新: 2025-09-10 JST_

この文書は **内部構造と拡張ポイント** を素早く把握するためのガイドです。実装が変わった場合は本書を更新してください（**実装が常に正**）。

---

## 1. システム概要
- **Discord Bot**: JDA を用いた Slash Command/Interaction 処理。  
- **Web UI**: Spring Boot + Thymeleaf による管理画面（共通レイアウト: `templates/base.html`）。  
- **DB**: SQLite（将来 PostgreSQL 移行視野）。DBアクセスは `DatabaseHelper` に集約。

> 詳細仕様は `docs/SPEC.md` を参照。

---

## 2. ディレクトリ構成（代表例）
> 実装に合わせて随時更新。無いパッケージは将来追加の想定。

```
src/main/java/com/an0mas/bot/
 ├─ command/                 # 各Slashコマンド実装（〜Command）
 │   ├─ BaseCommand.java     # 共通前処理（ブロック/権限などの集約点）
 │   ├─ FeedbackCommand.java
 │   ├─ FeedbackListCommand.java
 │   ├─ HelpCommand.java
 │   ├─ CmdAccessCommand.java
 │   └─ BlockUserCommand.java
 ├─ listener/
 │   ├─ SlashCommandListener.java  # Slashイベントの受口・ディスパッチ
 │   └─ ButtonInteractionListener.java
 ├─ db/
 │   └─ DatabaseHelper.java
 ├─ config/
 │   └─ BotConstants.java     # 開発者ID/通知チャンネル/運用フラグ 等
 └─ web/（あれば）
     ├─ controller/           # Spring MVC コントローラ
     └─ ...

src/main/resources/
 └─ templates/
     └─ base.html            # WebUI 共通レイアウト
```

---

## 3. ランタイム・フロー
### 3.1 Slash Command
1. ユーザー操作 → Discord API → **JDA** が `SlashCommandInteractionEvent` を発火  
2. **SlashCommandListener** がイベントを受け、コマンド名で対応する `*Command` を解決  
3. **BaseCommand**（親）で共通前処理：  
   - ブロック判定（ブラックリスト）  
   - （将来）開発者特権 / GuildOnly / AllowLists  
4. 個別 `*Command.execute(event)` が本処理を実行  
5. 必要に応じて **DatabaseHelper** 経由で永続化／取得  
6. 返信（エフェメラル/公開、Embed/Modal など）

### 3.2 Button / Component Interaction
- `ButtonInteractionListener` が ID 形式 `domain:action:arg1:...` を解析し、対象処理へ委譲。  
- ID には **発行者ID** や **nonce** を含め、なりすまし/期限切れを防止。

---

## 4. 権限モデル（実装ポイント）
- 実装は `BaseCommand` に集約する想定。評価順序（SPEC準拠）：  
  **Developer特権 → GuildOnly → AllowLists → defaultOpen**  
- AllowLists のデータは `server_permissions` / `user_permissions` を参照。  
- 固定メッセージは SPEC の推奨文言に寄せて統一予定。

> 既に各コマンド内で分散チェックがある場合も、**BaseCommand に寄せる**のが推奨。

---

## 5. データアクセス
- すべて **`DatabaseHelper`** を経由。  
- SQLite 方言に依存しない SQL を優先（PG 移行を見据え、`INTEGER epoch` / `TEXT ID` を基本に）。  
- トランザクションが必要な範囲は `DatabaseHelper` 側で提供。

---

## 6. Web UI（あれば）
- 共通レイアウト `templates/base.html`。  
- 画面は「ダッシュボード」「フィードバック一覧」から開始。  
- Thymeleaf でサーバーサイドレンダリング。フロントの状態は最小限。

---

## 7. 拡張ポイント
### 7.1 新しいコマンドの追加
1. `command/` に `XxxCommand.java` を作成（`BaseCommand` を継承）。  
2. `getSlashCommandData()` に `Commands.slash(getName(), getDescription())` を返す実装。  
3. `execute(event)` に本処理。DB が必要なら `DatabaseHelper` を利用。  
4. `SlashCommandListener` の解決テーブルに登録（または既存の自動登録機構に従う）。  
5. SPEC の「コマンド一覧」を更新。

**雛形：**
```java
public class SampleCommand extends BaseCommand {
    public SampleCommand() { super("sample", "説明文"); }

    @Override public void execute(SlashCommandInteractionEvent event) {
        event.reply("OK").setEphemeral(true).queue();
    }

    @Override public SlashCommandData getSlashCommandData() {
        return Commands.slash(getName(), getDescription());
    }
}
```

### 7.2 ボタン操作の追加
- ID は `sample:do:12345` のように **domain:action:args** を厳守。  
- Listener 側で **発行者一致** と **期限切れ** をチェック。

---

## 8. ロギングとエラー
- 例外はログ（WARN/ERROR）へ。ユーザー向けは **日本語・短文・エフェメラル**。  
- DB 一時失敗は **≤100ms / 1回** 再試行。

---

## 9. ビルド・起動
- ビルド: `mvn -q -DskipTests package`（`pom.xml` の設定に従う）  
- 実行: `java -jar target/<artifact>.jar`（実アーティファクト名はビルド結果に従う）  
- JDK のバージョンは `pom.xml` を正とする。

---

## 10. 将来の見直し（RFC候補）
- 権限チェックの完全集約（短TTLキャッシュを含む）  
- メッセージ固定文言の統一（多言語対応は将来案）  
- DB 移行（PG）と SQL 方言の更なる最小化
