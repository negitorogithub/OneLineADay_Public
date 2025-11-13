package net.unifar.mydiary.util

/**
 * Firebase Analyticsのイベント名とパラメータを定数で管理
 */
object AnalyticsEvents {
    const val SETTING_ACTION = "setting_action"
    const val PREMIUM_DIALOG_ACTION = "premium_dialog_action"
    const val RESTORE_DIALOG_ACTION = "restore_dialog_action"
    const val DIARY_CREATED = "diary_created"
    const val DIARY_EDIT_STARTED = "diary_edit_started"
    const val DIARY_EDIT_ENDED = "diary_edit_ended"
    const val PURCHASE_INITIATED = "purchase_initiated"
}

/**
 * 設定画面のアクション
 */
sealed class SettingAction(val value: String) {
    data object InquiryEmail : SettingAction("inquiry_email")
    data object CopyEmail : SettingAction("copy_email")
    data object Backup : SettingAction("backup")
    data object Restore : SettingAction("restore")
    data object Export : SettingAction("export")
    data object CopyBackupCode : SettingAction("copy_backup_code")
    data object RestorePurchase : SettingAction("restore_purchase")
}

/**
 * タブ名
 */
sealed class TabName(val value: String) {
    data object Home : TabName("home")
    data object Premium : TabName("premium")
    data object Settings : TabName("settings")
}

/**
 * ダイアログのボタン
 */
sealed class DialogButton(val value: String) {
    data object Ok : DialogButton("ok")
    data object Cancel : DialogButton("cancel")
}

/**
 * プレミアムプランのタイプ
 */
sealed class PlanType(val value: String) {
    data object Monthly : PlanType("monthly")
    data object Annual : PlanType("annual")
}

