# 📘 One Line A Day - 一行日記アプリ

🖊️ 毎日1行だけ、気軽に書き残すためのシンプルな日記アプリ  
Jetpack Compose + Kotlin で構築しました。

近日中にPlayStoreに公開予定です。


## ✨ 特徴

- 📅 **1日1行記録するだけ**
- 🎨 **Compose Material3 によるモダンなUI**
- ✍️ **IME連動・自動スクロールなど、書きやすさにこだわったUI**
- ☁️ **AdMobバナー広告（Adaptive Banner）実装済み**
- 🚀 **アプリ内レビュー・強制アップデート対応**
- 🌐 **海外展開を見据えたローカライズ対応（英語版あり）**

## 🧱 技術構成

| 分類 | 使用技術 |
|:--|:--|
| 言語 | Kotlin |
| UI | Jetpack Compose (Material3) |
| 状態管理 | ViewModel + StateFlow + Hilt |
| DB | Room |
| 広告 | Google AdMob（Adaptive Banner / Interstitial） |
| その他 | Firebase Crashlytics, In-App Review, In-App Updates API, Github Actions |


## 📸 スクリーンショット

| 書く画面 | 一覧画面 | 設定画面 |
|---|---|---|
|![Screenshot_20250520_200153](https://github.com/user-attachments/assets/a68d25f4-e75e-4837-9afd-472e8220c620) | ![Screenshot_20250520_200011](https://github.com/user-attachments/assets/0fb19ca4-3069-427e-92c3-b695a3700699)|![Screenshot_20250522_110533](https://github.com/user-attachments/assets/d895610c-58a9-4c7a-b5a6-1693af97d873) |
|![Screenshot_20250520_230720_en](https://github.com/user-attachments/assets/f9da0121-3236-4446-b630-d4b0ff1b93ff)|![Screenshot_20250520_230645_en](https://github.com/user-attachments/assets/79de9dc1-47cb-474d-bb47-9633b5792ff0)|![Screenshot_20250522_110453_en](https://github.com/user-attachments/assets/39270908-cb28-4813-b846-b70f41fb053e)|

## 🔧 セットアップ方法

1. このリポジトリをクローン：

```bash
git clone https://github.com/your-username/one-line-diary.git
```

2. google-services.jsonを追加
(追加しないとビルドできません)
