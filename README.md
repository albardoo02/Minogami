# Minogami (美濃紙)

Minogami（美濃紙）は [Paper](https://github.com/PaperMC/Paper) をベースに改良されたChiyogamiに更に改良を重ねた、次世代マルチスレッドMinecraftサーバーソフトです。

従来のマルチスレッドサーバーではプラグインの同期問題やデッドロックが頻発し、大量のプラグインを改修する必要がありました。Minogami ではサーバーコア側に対障害性・並行性強化レイヤーを設けることで、既存プラグイン資産を維持したまま高負荷サーバーを安定して並行稼働させることを目的としています。

---

## Chiyogami との主な違い

| 機能 / 特徴              | Chiyogami (従来)                           | Minogami (本作)                                                                      |
|:---------------------|:-----------------------------------------|:-----------------------------------------------------------------------------------|
| **ベースバージョン**         | Minecraft / Paper 26.2                   | **Minecraft / Paper 26.3 (最新追従)**                                                  |
| **ビルドシステム**          | 旧構成                                      | **Paperweight 2.0.0-beta.24 / Gradle 9.8 互換**                                      |
| **イベント排他制御 (Mutex)** | 全イベントディスパッチ時に単一Mutexで完全排他ロック（スレッド詰まりの原因） | **ホワイトリスト指定によるイベント排他ロックバイパス機構**<br>（高速なクラス・文字列キャッシュ判定によるナノ秒早期リターン）                 |
| **例外耐障害性 (セーフガード)**  | プラグインが例外（CME等）を投げるとワールドTick全体がクラッシュ      | **ワールドTick局所捕捉・例外握り潰しセーフガード**<br>（エンティティ/タイル更新ループやTick実行境界を保護し、5秒単位のレートリミット付きログ出力） |
| **クロスワールドテレポート**     | スレッド間同期の不整合によるエンティティ追跡破損リスク              | **遅延タスク委譲（ハンドオフ機構）による安全な移送**                                                       |
| **設定ファイル**           | `chiyogami.yml`                          | **`minogami.yml`** (ロックバイパス等の追加設定に対応)                                              |
| **API / ブランディング**    | `world.chiyogami`                        | **`world.minogami`** / Paper Branded Jar 対応                                        |

---

## 主な機能詳細

### 1. イベント排他制御（Mutex）のバイパス機構
高頻度で発火するイベント（`PlayerMoveEvent` 等）や、スレッドセーフティに自前で対処している/またはロック競合を避けたいプラグイン（`CrackShot`, `CoreProtect` 等）に対し、排他ロックの取得をスキップして直接リスナーを実行させます。判定処理にはキャッシュを活用し、ナノ秒単位で早期リターンします。

`minogami.yml` で柔軟にホワイトリストを設定できます：
```yaml
thread-safety:
  event-lock-bypass:
    # ロックをバイパスする完全修飾イベントクラス名
    events:
      - "org.bukkit.event.player.PlayerMoveEvent"
    # ロックをバイパスするプラグイン名（前方一致 / 完全一致）
    plugins:
      - "CrackShot"
      - "CrackShotPlus"
      - "CoreProtect"
      - "Azi"
      - "Life"
```

### 2. ワールドTick内の局所例外ハンドラ（共倒れ防止セーフガード）
マルチスレッド環境下で非対応プラグインが `ConcurrentModificationException` や `IllegalStateException` 等をスローしても、ワールドTick全体やサーバーが巻き込まれてクラッシュするのを防ぎます。
- エンティティ更新ループ（`entityTickList`）、タイルエンティティ更新ループ（`blockEntityTickers`）、ワールドTick実行境界を局所的に `try-catch` で保護。
- スタックトレースは同一例外のログスパムを防止するレートリミット機構（5秒間隔の集約ロガー）を介して出力され、後続処理およびワールドTickを強制継続します。

### 3. クロスワールドテレポートの遅延委譲
異なるワールドTickスレッド間でプレイヤーが移動する際、移動先ワールドのタスクキューへ移送処理をエンキューし、移動先スレッドの次のTick冒頭で安全にスポーン処理を実行するハンドオフ機構を備えています。

---

## ビルド方法

ビルドを実行するには、`git` および **Java 25 (JDK 25)** が必要です。

```bash
# パッチの適用
./gradlew applyAllPatches

# サーバー Bundler Jar の生成
./gradlew createBundlerJar
```

ビルドが完了すると、`minogami-server/build/libs/minogami-bundler-26.3...jar` に実行可能サーバーjarが生成されます。

---

## ライセンス
GNU General Public License v3.0 (GPLv3)
Base: PaperMC / Chiyogami (bea4dev)
