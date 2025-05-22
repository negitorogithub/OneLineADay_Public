package net.unifar.mydiary.model.entity_and_dao

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    internal val diaryDao: DiaryDao
) : ViewModel() {
    // diaryDaoをここで使う
}