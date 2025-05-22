import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import net.unifar.mydiary.model.entity_and_dao.Diary
import net.unifar.mydiary.model.entity_and_dao.DiaryUiModel
import net.unifar.mydiary.model.entity_and_dao.toUiModel
import java.util.Date

class DiaryViewModel : ViewModel() {

    // 本物のEntityを持ってるとする
    private val _diaryEntity = MutableStateFlow(
        Diary(
            id = "a",
            content = "今日は新しいアプリの構想を練った。",
            date = Date() // 現在日時
        )
    )

    // 画面用に変換してExposeする
//    val diaryUiModel: StateFlow<DiaryUiModel> = MutableStateFlow(
//        _diaryEntity.value.toUiModel()
//    )

}
