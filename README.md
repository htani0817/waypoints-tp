# WaypointTp (Paper 1.21+)

ウェイポイント登録・GUI テレポート・日本語メッセージ対応の Paper プラグイン。  
**v1.1.0** から、次の 2 点を追加／変更しました。

1. **/wp reload**  
   * `config.yml` と `messages.yml` だけを再読み込み  
   * **waypoints.yml（座標データ）は読み直さず** キャッシュを保持  
   * 実装は `WaypointTp#reloadAll()` に集約
2. **ウェイポイント削除権限の厳格化**  
   * **作成者本人のみ** 2 段階クリックで削除可能  
   * 他人が削除しようとすると `delete_not_owner` メッセージを表示  
   * 旧ノード `waypoints.delete.any` は廃止

---

## 更新点の詳細

| 変更箇所 | 内容 |
|----------|------|
| **WaypointTp.kt** | `reloadAll()` を追加<br> 1) `reloadConfig()`<br> 2) `messages.load()`<br> **`repo` は触らない** |
| **WpCommand.kt** | `/wp reload` で `plugin.reloadAll()` を呼び出すよう修正 |
| **MenuListener.kt** | `deleteWith2Step()` 内の権限判定を `wp.creator == p.uniqueId` のみへ簡素化 |
| **plugin.yml** | 権限ノード `waypoints.delete.any` を削除 |
| **messages.yml** | 追加キー<br>`reloaded: "<green>設定を再読み込みしました。"`<br>`delete_not_owner: "<red>このウェイポイントはあなたが作成していません！"` |
| **config.yml** (新規) | 例: `page-size`, `give-opener-on-join` など今後の拡張設定を記述 |

---

## ビルド & 導入

```bash
./gradlew clean build
