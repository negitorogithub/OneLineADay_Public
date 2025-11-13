# 📘 One Line A Day - 一行日記アプリ

🖊️ 毎日1行だけ、気軽に書き残すためのシンプルな日記アプリ  
Jetpack Compose + Kotlin で構築しました。

このリポジトリは、開発用リポジトリから機密情報を抜いたパブリック版となっています。

PlayStoreで公開中です。

[https://play.google.com/store/apps/details?id=net.unifar.mydiary](https://play.google.com/store/apps/details?id=net.unifar.mydiary)

## ✨ 特徴

- 📅 **1日1行記録するだけ**
- 🎨 **Compose Material3 によるモダンなUI**
- ✍️ **IME連動・自動スクロールなど、書きやすさにこだわったUI**
- ☁️ **AdMobバナー広告（Adaptive Banner）実装済み**
- 🚀 **アプリ内レビュー・強制アップデート対応**
- 🌐 **海外展開を見据えたローカライズ対応（英語版あり）**
- 🪙 **Billing Libraryでサブスク機能実装済み**
- 🔷 **レイヤードアーキテクチャを採用**
- ✅ **単体テスト導入済み**
- ✅ **GitHub ActionsによるCI導入済み**

## 🧱 技術構成

| 分類   | 使用技術                                                                    |
|:-----|:------------------------------------------------------------------------|
| 言語   | Kotlin                                                                  |
| UI   | Jetpack Compose (Material3)                                             |
| 状態管理 | ViewModel + StateFlow + Hilt                                            |
| DB   | Room                                                                    |
| 広告   | Google AdMob（Adaptive Banner / Interstitial）                            |
| その他  | Firebase Crashlytics, In-App Review, In-App Updates API, Github Actions |

## 📸 スクリーンショット

| 書く画面                                                                                                              | 一覧画面                                                                                                              | 設定画面                                                                                                              |プラン選択画面|
|-------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------|----------------|
|<img width="300" alt="Screenshot_20251114_005726" src="https://github.com/user-attachments/assets/34c121fb-5dab-4717-a8cc-519dc0e8d7a1" />|<img width="300"  alt="Screenshot_20251113_231746" src="https://github.com/user-attachments/assets/49767e66-9c33-462b-b94d-c05937f9854c" />|<img width="300" alt="Screenshot_20251113_231806" src="https://github.com/user-attachments/assets/e40a29e5-5ec5-4f71-bc55-b9b4f749e2bb" />|<img width="300" alt="Screenshot_20251113_231757" src="https://github.com/user-attachments/assets/6ebb96f8-c419-4b88-a9b2-5e884f685c30" />|
|<img width="300" alt="Screenshot_20251114_005908" src="https://github.com/user-attachments/assets/dc52a975-1f2a-42d0-9e9b-a4c3b1d6cdf2" />|<img width="300" alt="Screenshot_20251113_232113" src="https://github.com/user-attachments/assets/2dda5444-9088-428a-bc7a-224a1bd17e40" />|<img width="300" alt="Screenshot_20251113_232124" src="https://github.com/user-attachments/assets/96683762-28da-497d-ad36-ae659a6ae54e" />|<img width="300" alt="Screenshot_20251113_232118" src="https://github.com/user-attachments/assets/f4888674-bc26-44e4-812d-6030a02b417e" />|

## 🔧 セットアップ方法

1. このリポジトリをクローン：

```bash
git clone https://github.com/your-username/one-line-diary.git
```

2. google-services.jsonを配置

app/src/debugとapp/src/release両方に配置する必要があります！
