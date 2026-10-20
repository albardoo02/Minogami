# Minogami (美濃紙)

Minogami（美濃紙）は [Paper](https://github.com/PaperMC/Paper) をベースにした次世代マルチスレッドMinecraftサーバーソフトです（Chiyogamiフォーク）。
Spigot/Paperプラグイン資産をそのまま活用しつつ、ワールドごとの並行実行、イベント排他ロックバイパス、Tick例外局所捕捉セーフガードを提供します。

> [Download](https://github.com/bea4dev/Chiyogami/releases)

Versions
------
* [26.3 (Current)](https://github.com/bea4dev/Chiyogami/tree/ver/26.3)
* [26.2](https://github.com/bea4dev/Chiyogami/tree/ver/26.2)
* [26.1.2](https://github.com/bea4dev/Chiyogami/tree/ver/26.1.2)
* [1.21.11](https://github.com/bea4dev/Chiyogami/tree/ver/1.21.11)
* [1.21.8](https://github.com/bea4dev/Chiyogami/tree/ver/1.21.8)

Features
------
- **マルチスレッドワールド実行:** ワールドごとに独立スレッドを割り当て、大規模サーバーでのTPS低下を防ぎます。
- **イベント排他ロックバイパス:** `minogami.yml` のホワイトリスト指定により、`PlayerMoveEvent` や特定プラグイン（CrackShot, CoreProtect等）のイベントロックをバイパスして並行突っ切り動作を可能にします。
- **Tick例外セーフガード:** プラグインの非同期例外（`ConcurrentModificationException`等）を局所捕捉し、レートリミットログを出力しながらワールドTickの巻き添えクラッシュを防止します。
- **クロスワールドテレポート遅延委譲:** 異ワールド間のプレイヤー移動を移動先スレッドのTick冒頭へ安全にハンドオフします。

How to build
------
ビルドを実行するには、git, JDK 25 が必要です。

```bash
./gradlew applyAllPatches
./gradlew createBundlerJar
```

`chiyogami-server/build/libs` 内に `minogami-bundler-26.3...jar` が生成されます。