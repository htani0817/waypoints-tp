# WaypointTp（PaperMC 1.21+）

**座標の保存・GUI でのテレポート・2段階削除**ができる Paper プラグイン（Kotlin）。  
オプションで、**参加中/参加時の全プレイヤーのホットバー左端（スロット0）**に**メニュー起動アイテム**（オープナー）を自動配布し、**右クリックで GUI** を開けます。ホットバーのインデックスは 0–8 がホットバーです。 :contentReference[oaicite:0]{index=0}

---

## 主な機能

- **インベントリ GUI** から一覧/テレポート  
- **現在地の保存**（コマンド `/wp set` または GUI の「追加」）  
- **2段階削除**（右クリックで保留 → 10秒以内に再右クリックで確定）  
- **（任意）ホットバー0番に起動アイテム**を自動配布（右クリックで GUI を開く）  
- **メッセージの日本語化**：`messages.yml` によるローカライズ（**MiniMessage** で装飾可能） 。 :contentReference[oaicite:1]{index=1}  
- **作成者表示**：GUI の各ウェイポイントアイテムの **lore に「作成者」** を表示  
- **共有ストレージ**：ウェイポイントはサーバー側に保存され、全員で共有  
- **`/wp give`**：なくした／置いてきた時に **コマンドでオープナーを再配布**（自分／他人）

---

## 動作要件

- **Paper 1.21.x**  
- **Java 21 以上**（Paper は Java 21 以上が必須） 。 :contentReference[oaicite:2]{index=2}

---

## インストール

1. JAR を用意（下記「ビルド」参照）。  
2. サーバーの `plugins/` に配置。  
3. サーバーを起動（Paper が自動でロードします）。

---

## コマンド

| コマンド | 説明 |
|---|---|
| `/wp ui` | GUI を開く |
| `/wp set <name> [x y z] [yaw pitch] [world]` | ウェイポイントを保存。省略した値は **現在の** x/y/z・yaw/pitch・ワールドが使われます。例：`/wp set home 100 64 -30`、`/wp set base 0 80 0 180 0 world_nether` |
| `/wp tp <name>` | 保存済み `<name>` にテレポート（**`teleportAsync`** を使用） |
| `/wp tpp <player>` | 指定プレイヤーの位置へテレポート |
| `/wp give [player]` | **GUI オープナー（コンパス）を配布**：引数なしで自分、指定すると他人へ |
| `/wp reload` | `messages.yml` などを再読み込み |

> **テレポートについて**  
> 未ロードチャンクへ移動する可能性がある場合は、**`teleportAsync` を使用**すると**同期チャンク読み込みを避けられ**、メインスレッド負荷を軽減できます。 :contentReference[oaicite:3]{index=3}

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

- **GUI 識別**：`InventoryHolder` を実装した独自ホルダーで自作メニューのみを判定（タイトル比較は不採用）  
- **識別情報（ID）**：アイテム／エントリに **PDC（Persistent Data Container）** で UUID を保持（`ItemMeta` の `PersistentDataContainer` を使用） 。 :contentReference[oaicite:4]{index=4}  
- **右クリック検知**：`PlayerInteractEvent` は **左右の手で発火**するため、**`EquipmentSlot.HAND`（メイン手）だけ**処理。アクションは右クリック（AIR/BLOCK）のみ受け付け。 :contentReference[oaicite:5]{index=5}  
- **オープナー配布**：インベントリの空きは **`Inventory#firstEmpty()`** で判定。空きがなければ足元にドロップ。オンライン全員への配布には **`Bukkit#getOnlinePlayers()`** を利用。 :contentReference[oaicite:6]{index=6}  
- **タブ補完**：`TabCompleter#onTabComplete` を使用（`/wp set` の座標などは現在値を候補に）。 :contentReference[oaicite:7]{index=7}

---

## よくある質問（FAQ）

**Q. オープナー（コンパス）をなくしました。**  
A. `/wp give` で自分に再配布できます。管理者は `/wp give <player>` で他人へ配布可能です。インベントリが満杯の場合は**足元にドロップ**されます。 :contentReference[oaicite:8]{index=8}

**Q. オープナーが反応しません。**  
A. **メイン手で右クリック**しているか、他プラグインでイベントがキャンセルされていないかを確認してください。`PlayerInteractEvent` は両手で発火するため、メイン手以外は無視しています。 :contentReference[oaicite:9]{index=9}

---

## ビルド

このプロジェクトは **Gradle（Kotlin DSL）** を使用します。

```bash
# Windows
.\gradlew.bat clean build

# macOS/Linux
./gradlew clean build
