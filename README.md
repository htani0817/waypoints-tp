# WaypointTp（PaperMC 1.21+）

**座標の保存・GUI でのテレポート・2段階削除**ができる Paper プラグイン（Kotlin）。  
オプションで、参加中/参加時の**全プレイヤーのホットバー左端（スロット0）**に**メニュー起動アイテム（オープナー）**を自動配布し、**右クリックで GUI** を開けます。

---

## 主な機能

- **インベントリ GUI** から一覧/テレポート
- **現在地の保存**（コマンド `/wp set` または GUI の「追加」）
- **金床リネーム（ゴースト入力）**：GUI の「📍 現在地を登録」（スロット53）を押すと**金床の入力画面**が開き、**任意の名前**で保存できます。  
  この金床は **入力専用（ゴースト）** で、置いた紙は**手元に残りません**（クリック/ドラッグ/シフト・数字キーの移動を禁止し、閉じる際に上段インベントリをクリア）。 :contentReference[oaicite:0]{index=0}
- **2段階削除**（右クリックで保留 → 10秒以内に再右クリックで確定）
- **（任意）ホットバー0番に起動アイテム**を自動配布（右クリックで GUI を開く）
- **メッセージの日本語化**：`messages.yml` によるローカライズ（MiniMessage で装飾可能）
- **作成者表示**：GUI の各ウェイポイントアイテムの *lore* に「作成者」を表示
- **共有ストレージ**：ウェイポイントはサーバー側に保存され、全員で共有
- **`/wp give`**：なくした／置いてきた時に**コマンドでオープナーを再配布**（自分／他人）

---

## 動作要件

- **Paper 1.21.x**
- **Java 21 以上**（Paper 1.21 は Java 21 以上が必須） 。 :contentReference[oaicite:1]{index=1}

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
    - 金床は **MenuType API（`MenuType.ANVIL`）** を使用して開き、**`AnvilView#getRenameText()`** から入力文字列を取得します。 :contentReference[oaicite:2]{index=2}
    - この画面の紙は**手元に残りません**（入力専用のゴースト）。
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

- **GUI 識別**：`InventoryHolder` を実装した独自ホルダーで自作メニューのみを判定
- **識別情報（ID）**：アイテム／エントリに **PDC（Persistent Data Container）** で UUID を保持（`ItemMeta` の `PersistentDataContainer`）。 :contentReference[oaicite:4]{index=4}
- **右クリック検知**：`PlayerInteractEvent` は左右の手で発火するため、**`EquipmentSlot.HAND`（メイン手）**のみ処理
- **オープナー配布**：インベントリの空きは `Inventory#firstEmpty()` で判定。空きがなければ足元にドロップ
- **金床入力（ゴースト化）**：
    - **MenuType API** で金床を開き、**`AnvilView#getRenameText()`** から入力を取得。 :contentReference[oaicite:5]{index=5}
    - **クリック抑止**：金床上段の **入力スロット(0,1)** へのクリックをキャンセル。**シフトクリック / 数字キー（`ClickType.NUMBER_KEY`）**による移動もキャンセル。 :contentReference[oaicite:6]{index=6}
    - **ドラッグ抑止**：`InventoryDragEvent` の **raw slot 0..2** を含むドラッグをキャンセル。 :contentReference[oaicite:7]{index=7}
    - **クローズ時**：上段インベントリを **`clear()`** して残留/返却/ドロップを防止（＝紙は手元に残らない）。
- **テレポート**：未ロードチャンク対策として **`teleportAsync`** を使用。 :contentReference[oaicite:8]{index=8}

---

## ビルド

このプロジェクトは **Gradle（Kotlin DSL）** を使用します。

```bash
# Windows
.\gradlew.bat clean build

# macOS/Linux
./gradlew clean build
