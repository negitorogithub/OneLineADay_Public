package net.unifar.mydiary.presentation.viewmodel

import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.unifar.mydiary.data.local.entity.Diary
import net.unifar.mydiary.data.local.dao.DiaryDao
import net.unifar.mydiary.data.repository.BackUpCodeRepository
import net.unifar.mydiary.data.repository.BillingRepository
import net.unifar.mydiary.data.repository.DiaryRepository
import net.unifar.mydiary.domain.model.DiaryUiModel
import net.unifar.mydiary.domain.model.toUiModel
import net.unifar.mydiary.domain.usecase.BackupUseCase
import net.unifar.mydiary.util.AnalyticsEvents
import net.unifar.mydiary.util.TabName
import net.unifar.mydiary.util.generateSortableId
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import javax.inject.Inject


sealed class DiaryListUiState {
    object Loading : DiaryListUiState()
    object Empty : DiaryListUiState()
    object HasContent : DiaryListUiState()
}

@HiltViewModel
class DiariesViewModel @Inject constructor(
    private val diaryRepository: DiaryRepository,
    backUpCodeRepository: BackUpCodeRepository,
    private val backUpUseCase: BackupUseCase,
    val diaryDao: DiaryDao,
    billingRepository: BillingRepository,
    private val analytics: FirebaseAnalytics,
) :
    ViewModel() {

    val isPremium = billingRepository.isPremiumFlow
    val backUpCodeFlow = backUpCodeRepository.backUpCodeFlow

    val editingId = MutableStateFlow<String?>(null)
    val diariesUiModel: StateFlow<List<DiaryUiModel>> = combine(
        diaryRepository.allDiaries, // Flow<List<Diary>>
        editingId // Flow<String?>
    ) { diaries, editingId ->
        diaries.sortedByDescending { it.date.time }
            .map { it.toUiModel(editingId == it.id) }
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        initialValue = emptyList()
    )

//    init {
//        if (BuildConfig.DEBUG) {
//            viewModelScope.launch {
//                diaryRepository.deleteAllDiaries()
//                if (diaryRepository.diariesCount() == 0) {
//                    diaryRepository.insertAll(demoDiaries)
//                }
//            }
//        }
//    }

    private val _diaryListUiState = MutableStateFlow<DiaryListUiState>(DiaryListUiState.Loading)
    val diaryListUiState: StateFlow<DiaryListUiState> = _diaryListUiState

    init {
        viewModelScope.launch {
            diaryRepository.allDiaries.collect { grouped ->
                _diaryListUiState.value = if (grouped.isEmpty()) {
                    DiaryListUiState.Empty
                } else {
                    DiaryListUiState.HasContent
                }
            }
        }
    }


    fun getYearMonthPattern(locale: Locale): String {
        val language = locale.language
        val country = locale.country

        return when {
            // 年→月の文化圏
            language in listOf("ja", "ko", "zh") -> "yyyy/MM"

            // ドイツ語圏などの特殊系（ドット区切り）
            language == "de" || country in listOf("PL", "CZ", "HU", "SK", "AT") -> "MM.yyyy"

            // 年→月が一般的なアラビア語圏
            language == "ar" -> "yyyy/MM"

            // その他は月→年
            else -> "MM/yyyy"
        }
    }

    val diaryGroupedByDate: StateFlow<Map<String, List<DiaryUiModel>>> = diariesUiModel
        .map { list ->
            list.groupBy {

                val pattern = getYearMonthPattern(Locale.getDefault())
                it.date.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate().format(DateTimeFormatter.ofPattern(pattern))
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())

    fun isEditing(): Boolean {
        return editingId.value != null && diariesUiModel.value.any { it.id == editingId.value }
    }

    suspend fun addNewDiary(): String {
        val diary = Diary(generateSortableId(), "", Date())
        withContext(Dispatchers.IO) {
            diaryRepository.insert(diary)
        }
        viewModelScope.launch(Dispatchers.IO) {
            backUpUseCase.executeBackup(diaryDao)
        }
        
        // 現在の日記総数（新規作成含む）
        val totalCount = diariesUiModel.value.size + 1
        
        // User Propertyを更新
        updateUserProperties(totalCount)
        
        // Analyticsイベントを送信
        analytics.logEvent(
            AnalyticsEvents.DIARY_CREATED,
            bundleOf(
                "diary_id" to diary.id,
                "timestamp" to diary.date.time,
                "total_count" to totalCount
            )
        )
        
        return diary.id
    }

    suspend fun updateDiaryContent(id: String, content: String) {
        withContext(Dispatchers.IO) {
            diaryRepository.updateContent(id, content)
        }
        viewModelScope.launch(Dispatchers.IO) {
            backUpUseCase.executeBackup(diaryDao)
        }
    }

    suspend fun updateDiaryContentWithoutSaving(id: String, content: String) {
        withContext(Dispatchers.IO) {
            diaryRepository.updateContent(id, content)
        }
    }

    suspend fun updateDiaryDateToNext(id: String) {
        withContext(Dispatchers.IO) {
            diaryRepository.updateDiaryDateToNext(id)
        }
        viewModelScope.launch(Dispatchers.IO) {
            backUpUseCase.executeBackup(diaryDao)
        }
    }

    suspend fun updateDiaryDateToPrevious(id: String) {
        withContext(Dispatchers.IO) {
            diaryRepository.updateDiaryDateToPrevious(id)
        }
        viewModelScope.launch(Dispatchers.IO) {
            backUpUseCase.executeBackup(diaryDao)
        }
    }

    suspend fun deleteEditingDiary() {
        editingId.value?.let {
            withContext(Dispatchers.IO) {
                diaryRepository.deleteDiaryById(it)
            }
            stopEditingCurrent()
            
            // 削除後のUser Propertyを更新
            updateUserProperties(diariesUiModel.value.size)
        }
    }

    fun startEditing(id: String) {
        editingId.value = id
        
        // 編集開始イベントを送信
        analytics.logEvent(
            AnalyticsEvents.DIARY_EDIT_STARTED,
            bundleOf(
                "diary_id" to id
            )
        )
    }

    fun stopEditingCurrent() {
        val currentEditingId = editingId.value
        editingId.value = null
        
        // 編集終了イベントを送信
        currentEditingId?.let { id ->
            analytics.logEvent(
                AnalyticsEvents.DIARY_EDIT_ENDED,
                bundleOf(
                    "diary_id" to id
                )
            )
        }
        
        viewModelScope.launch(Dispatchers.IO) {
            backUpUseCase.executeBackup(diaryDao)
        }
    }

    /**
     * タブ切り替え時にAnalyticsイベントを送信
     * @param tabIndex タブのインデックス (0: ホーム, 1: プレミアム, 2: 設定)
     * @param tabName タブの名前
     */
    fun logTabSelected(tabIndex: Int, tabName: TabName) {
        analytics.logEvent(
            FirebaseAnalytics.Event.SELECT_CONTENT,
            bundleOf(
                FirebaseAnalytics.Param.CONTENT_TYPE to "navigation_tab",
                FirebaseAnalytics.Param.ITEM_ID to "tab_$tabIndex",
                "tab_name" to tabName.value
            )
        )
    }

    /**
     * User Propertyとして日記数とユーザーカテゴリを更新
     * Firebase Analyticsの匿名IDに紐づけて統計情報を管理
     * @param diaryCount 現在の日記総数
     */
    private fun updateUserProperties(diaryCount: Int) {
        // 日記の総数
        analytics.setUserProperty("total_diaries", diaryCount.toString())
        
        // ユーザーカテゴリ（エンゲージメント分析用）
        val category = when {
            diaryCount == 0 -> "no_entries"
            diaryCount < 10 -> "beginner"
            diaryCount < 50 -> "regular"
            diaryCount < 100 -> "active"
            else -> "power_user"
        }
        analytics.setUserProperty("user_category", category)
    }

}