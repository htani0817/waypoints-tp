
---

## `README-ja.md`（日本語・置き換え）

```markdown
# WaypointTp（PaperMC 1.21+）

**座標の保存・GUI でのテレポート・2段階削除**ができる Paper プラグイン（Kotlin）。  
オプションで、**参加中/参加時の全プレイヤーのホットバー左端（スロット0）**に**メニュー起動アイテム**を自動配布し、**右クリックで GUI** を開けます。

---

## 主な機能

- **インベントリ GUI** から一覧/テレポート
- **現在地の保存**（コマンド or GUI の「追加」）
- **2段階削除**（右クリックで保留 → 10秒以内に再右クリックで確定）
- **（任意）ホットバー0番に起動アイテム**を自動配布（右クリックで GUI）
- **日本語メッセージ**：`messages.yml` を **MiniMessage** で装飾可能
- **作成者表示**：GUI の各ウェイポイントアイテムの **lore に「作成者」** を表示
- **共有ストレージ**：ウェイポイントはサーバー側に保存され、全員で共有

---

## 動作要件

- **Paper 1.21.x**
- **Java 21 以上**

---

## 導入手順

1. JAR を用意（下記「ビルド」参照）。  
2. サーバーの `plugins/` に配置。  
3. サーバーを起動（Paper が自動でロードします）。

---

## コマンド

| コマンド | 説明 |
|---|---|
| `/wp ui` | GUI を開く |
| `/wp set <name> [x y z] [yaw pitch] [world]` | ウェイポイントを保存。省略した値は **現在の** x/y/z・yaw/pitch・ワールドが使われます。例：`/wp set home 100 64 -30`、`/wp set base 0 80 0 180 0 world_nether` |
| `/wp tp <name>` | 保存済み `<name>` にテレポート（`teleportAsync` を使用） |
| `/wp tpp <player>` | 指定プレイヤーの位置へテレポート |
| `/wp reload` | `messages.yml` などを再読み込み |

> 削除はコマンドではなく GUI 上で行います（2段階確認）。

**`[yaw pitch]` について**  
- `yaw` … 水平方向の角度（0=南、90=西、180=北、-90/270=東）  
- `pitch` … 上下の角度（0=水平、+90=真下、-90=真上）

---

## 権限

`plugin.yml` に定義。既定値は次の通りです（`reload` 以外は全員使用可）。

| ノード | 既定 | 役割 |
|---|---:|---|
| `waypoints.use` | `true` | `/wp` の基本利用 |
| `waypoints.create` | `true` | `/wp set` と GUI 追加 |
| `waypoints.delete` | `true` | GUI での削除 |
| `waypoints.tpp` | `true` | `/wp tpp` |
| `waypoints.reload` | `op` | `/wp reload` |

---

## 設定

- `messages.yml`：表示メッセージ（MiniMessage のタグに対応）  
  `/wp reload` で即時反映。

---

## 実装メモ

- **GUI 識別**：`InventoryHolder` を使って自作メニューのみ確実に判定
- **ID 管理**：アイテム/エントリに **PDC**（`ItemMeta` の PDC）で UUID を埋め込み
- **テレポート**：未ロードチャンクに備え **`teleportAsync`** を使用
- **配布**：`PlayerJoinEvent` と `Bukkit.getOnlinePlayers()` で参加時/起動直後に一括適用

---

## ビルド

```bash
# Windows
.\gradlew.bat clean build

# macOS/Linux
./gradlew clean build
