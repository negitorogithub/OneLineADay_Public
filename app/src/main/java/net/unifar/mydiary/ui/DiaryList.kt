package net.unifar.mydiary.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import net.unifar.mydiary.model.entity_and_dao.DiariesViewModel

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import net.unifar.mydiary.R
import net.unifar.mydiary.model.entity_and_dao.DiaryListUiState
import net.unifar.mydiary.model.entity_and_dao.DiaryUiModel


// リストComposable
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DiaryList(listState: LazyListState, bringIntoViewRequester: BringIntoViewRequester) {
    val viewModel: DiariesViewModel = hiltViewModel()
    val diaryListUiState = viewModel.diaryListUiState.collectAsState()
    val diariesGroupedByDate by viewModel.diaryGroupedByDate.collectAsState()

    if (diaryListUiState.value == DiaryListUiState.Empty && diariesGroupedByDate.isEmpty()) {
        // リストが空のとき専用画面
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(36.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.diary_list_empty_message),
                style = TextStyle(fontSize = 28.sp)
            )
        }
    } else {
        LazyColumn(state = listState, modifier = Modifier.imePadding()) {


            diariesGroupedByDate.forEach { (date, diariesOnDate) ->

                stickyHeader {
                    Text(
                        text = date,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.secondary)
                            .padding(8.dp),
                        color = MaterialTheme.colorScheme.onSecondary
                    )

                }
                itemsIndexed(diariesOnDate, key = { _, diary -> diary.id }) { index, diary ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = {
                                    viewModel.stopEditingCurrent()
                                },
                                onLongClick = {
                                    viewModel.startEditing(diary.id)
                                }
                            )
                            .bringIntoViewRequester(bringIntoViewRequester)
                    ) {
                        DiaryListContent(
                            id = diary.id,
                            date = diary.dateString,
                            dayOfWeek = diary.dayOfWeek,
                            content = diary.content,
                            dayType = diary.dayType,
                            isEditing = diary.isEditing,
                            bringIntoViewRequester = bringIntoViewRequester
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }


            }

        }
    }
}