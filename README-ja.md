
---

## `README-ja.md`

```markdown
# WaypointTp（PaperMC 1.21+）

**座標の保存・GUI でのテレポート・2段階削除**ができる Paper プラグイン（Kotlin）。  
オプションで、**参加中/参加時の全プレイヤーのホットバー左端（スロット0）**に**メニュー起動アイテム**を自動配布し、**右クリックで GUI** を開けます。

> Paper 1.21.x（Java 21 必須）向け。アイテム/エントリの識別に **PDC**、未ロードチャンクへの TP には **`teleportAsync`** を使用します。 :contentReference[oaicite:22]{index=22}

---

## 主な機能

- **インベントリ GUI** から一覧/テレポート
- **現在地の保存**（コマンド or GUI の「追加」）
- **2段階削除**（右クリックで保留 → 10秒以内に再右クリックで確定）
- **（任意）ホットバー0番に起動アイテム**を自動配布（右クリックで GUI）
- **日本語メッセージ**：`messages.yml` を **MiniMessage** で装飾可能 :contentReference[oaicite:23]{index=23}

---

## 動作要件

- **Paper 1.21.x**
- **Java 21**（Paper の要件） :contentReference[oaicite:24]{index=24}

Java の導入は Paper 公式のガイドが参考になります。 :contentReference[oaicite:25]{index=25}

---

## 使い方（導入）

1. 本プラグインの JAR を取得（下記「ビルド」参照）。  
2. サーバーの `plugins/` に配置。  
3. サーバーを起動（Paper については公式の「はじめに」を参照）。 :contentReference[oaicite:26]{index=26}

---

## コマンド

| コマンド | 説明 |
|---|---|
| `/wp ui` | GUI を開く |
| `/wp set <name>` | 現在地を `<name>` で保存 |
| `/wp tp <name>` | 保存済み `<name>` にテレポート（`teleportAsync` を使用） :contentReference[oaicite:27]{index=27} |
| `/wp tpp <player>` | 指定プレイヤーの位置へテレポート |
| `/wp reload` | `messages.yml` などを再読み込み |

> 削除はコマンドではなく GUI 上で行います（2段階確認）。

---

## 権限

`plugin.yml` に権限ノードを定義しています。**`default: true`** にすると **OP 以外も含め全員が使用可** になります。指定可能な既定値は `true` / `false` / `op` / `not op` です。 :contentReference[oaicite:28]{index=28}

| ノード | 既定 | 役割 |
|---|---:|---|
| `waypoints.use` | `true` | `/wp` の基本利用 |
| `waypoints.create` | `true` | `/wp set` と GUI 追加 |
| `waypoints.delete` | `true` | GUI での削除 |
| `waypoints.tpp` | `true` | `/wp tpp` |
| `waypoints.reload` | `op` | `/wp reload` |

---

## 設定

- `messages.yml`：表示メッセージを管理。**MiniMessage** のタグで色/装飾やクリック動作も指定できます。 :contentReference[oaicite:29]{index=29}  
- 反映は `/wp reload` で可能。

---

## 実装メモ（仕組み）

- **GUI 識別**：`InventoryHolder` を自作して自前メニューのみを確実に判定（タイトル比較より安全）。プロジェクト構成の基本は Paper のセットアップガイドが参考。 :contentReference[oaicite:30]{index=30}
- **ID 管理**：アイテム/エントリに **PDC（Persistent Data Container）** で UUID を埋め込み、クリック時に取り出し。 :contentReference[oaicite:31]{index=31}
- **テレポート**：未ロードチャンクの可能性があるため **`teleportAsync`** を使用。同期チャンクロードを避け、メインスレッドへの負担を減らします。 :contentReference[oaicite:32]{index=32}
- **起動アイテム配布**：`PlayerJoinEvent` と `Bukkit.getOnlinePlayers()` を用いて、参加時と起動直後に一括適用。 :contentReference[oaicite:33]{index=33}

---

## ビルド（ソースから）

本プロジェクトは **Gradle（Kotlin DSL）** と **Shadow プラグイン**で配布用 JAR（依存込み）を生成します。  
Shadow のプラグイン ID は `com.gradleup.shadow`。 :contentReference[oaicite:34]{index=34}

```bash
# Windows
.\gradlew.bat clean build

# macOS/Linux
./gradlew clean build
