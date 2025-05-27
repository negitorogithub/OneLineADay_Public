package net.unifar.mydiary.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.unifar.mydiary.BuildConfig
import net.unifar.mydiary.db.Diary
import net.unifar.mydiary.model.DiaryUiModel
import net.unifar.mydiary.model.toUiModel
import net.unifar.mydiary.repository.DiaryRepository
import net.unifar.mydiary.util.demoDiaries
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
class DiariesViewModel @Inject constructor(private val diaryRepository: DiaryRepository) :
    ViewModel() {

    val editingId = MutableStateFlow<String?>(null)
    val diariesUiModel: StateFlow<List<DiaryUiModel>> = combine(
        diaryRepository.allDiaries, // Flow<List<Diary>>
        editingId // Flow<String?>
    ) { diaries, editingId ->
        diaries.sortedByDescending { it.date.time }
            .map { it.toUiModel(editingId == it.id) }
    }.stateIn(
        viewModelScope,
        SharingStarted.Companion.Lazily,
        initialValue = emptyList()
    )

    init {
        if (BuildConfig.DEBUG) {
            viewModelScope.launch {
                diaryRepository.deleteAllDiaries()
                if (diaryRepository.diariesCount() == 0) {
                    demoDiaries.forEach { diary ->
                        diaryRepository.insert(diary)
                    }
                }
            }
        }
    }

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
        .stateIn(viewModelScope, SharingStarted.Companion.Lazily, emptyMap())

    fun isEditing(): Boolean {
        return editingId.value != null && diariesUiModel.value.any { it.id == editingId.value }
    }

    suspend fun addNewDiary(): String {
        val diary = Diary(generateSortableId(), "", Date())
        diaryRepository.insert(diary)
        return diary.id
    }

    suspend fun updateDiaryContent(id: String, content: String) {
        diaryRepository.updateContent(id, content)
    }


    suspend fun updateDiaryDateToNext(id: String) {
        val diary = diaryRepository.getDiaryById(id)

        val currentLocalDate = diary.date.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        val nextLocalDate = currentLocalDate.plusDays(1)

        val nextDate = Date.from(
            nextLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
        )

        diaryRepository.update(diary.copy(date = nextDate))
    }

    suspend fun updateDiaryDateToPrevious(id: String) {
        val diary = diaryRepository.getDiaryById(id)

        val currentLocalDate = diary.date.toInstant()

            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        val nextLocalDate =
            currentLocalDate.minusDays(1).atTime(23, 59, 59).atZone(ZoneId.systemDefault())

        val nextDate = Date.from(
            nextLocalDate.toInstant()
        )

        diaryRepository.update(diary.copy(date = nextDate))
    }

    suspend fun deleteEditingDiary() {
        editingId.value?.let {
            diaryRepository.deleteDiaryById(it)
            stopEditingCurrent()
        }
    }

    fun startEditing(id: String) {
        editingId.value = id
    }

    fun stopEditingCurrent() {
        editingId.value = null
    }

}