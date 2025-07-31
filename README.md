# WaypointTp（PaperMC 1.21+）

**座標の保存・GUI でのテレポート・2段階削除**ができる Paper プラグイン（Kotlin）。  
オプションで、参加中/参加時の**全プレイヤーのホットバー左端（スロット0）**に**メニュー起動アイテム（オープナー）**を自動配布し、**右クリックで GUI** を開けます。

---

## 主な機能

- **インベントリ GUI** から一覧/テレポート
- **現在地の保存**（コマンド `/wp set` または GUI の「追加」）
- **金床リネーム対応**：GUI の「📍 現在地を登録」（スロット53）を押すと**金床の入力画面**が開き、**任意の名前**で保存できます
- **2段階削除**（右クリックで保留 → 10秒以内に再右クリックで確定）
- **（任意）ホットバー0番に起動アイテム**を自動配布（右クリックで GUI を開く）
- **メッセージの日本語化**：`messages.yml` によるローカライズ（MiniMessage で装飾可能）
- **作成者表示**：GUI の各ウェイポイントアイテムの *lore* に「作成者」を表示
- **共有ストレージ**：ウェイポイントはサーバー側に保存され、全員で共有
- **`/wp give`**：なくした／置いてきた時に**コマンドでオープナーを再配布**（自分／他人）

---

## 動作要件

- **Paper 1.21.x**
- **Java 21 以上**（Paper 1.21 は Java 21 以上が必須）

---

## インストール

1. JAR を用意（下記「ビルド」参照）。
2. サーバーの `plugins/` に配置。
3. サーバーを起動（Paper が自動でロードします）。

---

## 使い方（GUIでの登録）

1. `/wp ui` またはオープナー（コンパス）を右クリックして **GUI** を開く
2. **右下の「📍 現在地を登録」**（スロット 53）をクリック
3. **金床の入力画面**が開くので、保存したい**名前**を入力して**右側の結果スロット**をクリック
4. 入力した名前で**現在地が保存**され、GUI に戻ります（キャンセルした場合は保存されません）

---

## コマンド

| コマンド | 説明 |
|---|---|
| `/wp ui` | GUI を開く |
| `/wp set <name> [x y z] [yaw pitch] [world]` | ウェイポイントを保存。省略した値は **現在の** x/y/z・yaw/pitch・ワールドが使われます。例：`/wp set home 100 64 -30`、`/wp set base 0 80 0 180 0 world_nether` |
| `/wp tp <name>` | 保存済み `<name>` にテレポート（`teleportAsync` を使用） |
| `/wp tpp <player>` | 指定プレイヤーの位置へテレポート |
| `/wp give [player]` | **GUI オープナー（コンパス）を配布**：引数なしで自分、指定すると他人へ |
| `/wp reload` | `messages.yml` などを再読み込み |

> **テレポートについて**  
> 未ロードチャンクへ移動する可能性がある場合は、**`teleportAsync` を使用**すると**同期チャンク読み込みを避けられ**、メインスレッド負荷を軽減できます。

---

## `[yaw pitch]` について（角度の意味）

- **yaw** … 水平方向の角度（例：0=南、90=西、180=北、-90/270=東）
- **pitch** … 上下方向の角度（0=水平、+90=真下、-90=真上）

> 角度を省略した場合は、コマンド実行時の **プレイヤーの向き**が保存されます。

---

## 権限

`plugin.yml` に定義。既定値は次の通り（`reload` と「他人への give」以外は全員使用可）。

| ノード | 既定 | 役割 |
|---|---:|---|
| `waypoints.use` | `true` | `/wp` の基本利用 |
| `waypoints.create` | `true` | `/wp set` と GUI 追加 |
| `waypoints.delete` | `true` | GUI での削除 |
| `waypoints.tpp` | `true` | `/wp tpp` |
| `waypoints.opener` | `true` | `/wp give`（自分へ配布） |
| `waypoints.opener.others` | `op` | `/wp give <player>`（他人へ配布） |
| `waypoints.reload` | `op` | `/wp reload` |

---

## 実装メモ（技術）

- **GUI 識別**：`InventoryHolder` を実装した独自ホルダーで自作メニューのみを判定
- **識別情報（ID）**：アイテム／エントリに **PDC（Persistent Data Container）** で UUID を保持（`ItemMeta` の `PersistentDataContainer`）
- **右クリック検知**：`PlayerInteractEvent` は左右の手で発火するため、**`EquipmentSlot.HAND`（メイン手）**のみ処理
- **オープナー配布**：インベントリの空きは `Inventory#firstEmpty()` で判定。空きがなければ足元にドロップ
- **金床入力**：Paper 1.21 の **MenuType API**（`MenuType.ANVIL`）で金床 UI を開き、**`AnvilView#getRenameText()`** から入力文字を取得
- **テレポート**：未ロードチャンク対策として **`teleportAsync`** を使用

---

## ビルド

このプロジェクトは **Gradle（Kotlin DSL）** を使用します。

```bash
# Windows
.\gradlew.bat clean build

# macOS/Linux
./gradlew clean build
