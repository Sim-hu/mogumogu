# もぐもぐmod (Mogumogu Mod)

## 概要 (Overview)
Minecraft 1.19.4 (Fabric) 用のクライアントサイド自動食事MODです。満腹度が設定値以下になると、ホットバー内の食べ物を自動で食べます。ブラックリストや細かな設定も可能です。

A client-side auto-eat mod for Minecraft 1.19.4 (Fabric). When your hunger drops below a threshold, it automatically eats food from your hotbar. Blacklist and detailed settings are available.

---

## 主な機能 (Features)
- 満腹度がしきい値以下で自動的に食事
- ホットバー内の食べ物のみ自動で食べる
- 食べたくない食料をブラックリストで設定可能（アイコンで直感的に操作）
- Mod Menu/Cloth Config対応でGUIから設定可能
- 他アクション中の自動食事許可設定
- 機能のオン/オフ切り替え
- 日本語・英語対応

- Automatically eats food when hunger is at or below a threshold
- Only eats food from the hotbar (not from inventory)
- Blacklist foods you don't want to eat (easy icon-based UI)
- Mod Menu/Cloth Config support for GUI settings
- Option to allow auto-eating during other actions (e.g., sprinting)
- Enable/disable the mod
- Japanese and English support

---

## 導入方法 (Installation)
1. [Fabric API](https://fabricmc.net/) および [Fabric Loader](https://fabricmc.net/use/) を導入してください。
2. このmodのjarファイルを `mods` フォルダに入れてください。

1. Install [Fabric API](https://fabricmc.net/) and [Fabric Loader](https://fabricmc.net/use/).
2. Put the jar file of this mod into your `mods` folder.

---

## 使い方・設定 (Usage & Settings)
- ゲーム内で`Mod Menu`から「もぐもぐmod」設定画面を開けます。
- 満腹度のしきい値やブラックリスト、機能のオンオフなどをGUIで調整できます。
- ブラックリスト設定画面では、食べ物アイコンをクリックして自動食事の可否を切り替えられます。
- 他アクション中の自動食事許可も設定可能です。

- Open the "Mogumogu Mod" settings from `Mod Menu` in-game.
- Adjust hunger threshold, blacklist, enable/disable, etc. via GUI.
- In the blacklist screen, click food icons to allow/deny auto-eating for each food.
- You can also allow auto-eating during other actions (like sprinting).

---

## 注意事項 (Notes)
- サバイバル/アドベンチャーのみ有効。クリエイティブ/スペクテイターでは動作しません。
- ブラックリストに登録した食べ物は手動でも食べられません。
- 自動食事はホットバー内の食べ物のみ対象です。
- 満腹度監視→食事実行→完了確認の3段階で安定動作します。

- Only works in Survival/Adventure. Not active in Creative/Spectator.
- Blacklisted foods cannot be eaten manually.
- Only foods in the hotbar are auto-eaten.
- The mod uses a 3-step state: monitoring hunger → eating → confirming completion for stable operation.

---

## ライセンス (License)
This mod is released under the MIT License. 
