package net.unifar.mydiary.presentation.ui

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.play.core.review.ReviewException
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.model.ReviewErrorCode
import kotlinx.coroutines.launch
import net.unifar.mydiary.MainActivity
import net.unifar.mydiary.domain.model.DayType
import net.unifar.mydiary.presentation.viewmodel.DiariesViewModel
import net.unifar.mydiary.presentation.theme.localExtraColors

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DiaryListContent(
    id: String,
    date: String,     // 例: "2025/05/03"
    dayOfWeek: String, // 例: "土"
    dayType: DayType,
    content: String,    // 本文
    isEditing: Boolean,
) {
    val viewModel: DiariesViewModel = hiltViewModel()
    val isPremium by viewModel.isPremium.collectAsState()
    val backupCode by viewModel.backUpCodeFlow.collectAsState()


    var textFieldValue by remember { mutableStateOf(TextFieldValue(content)) } // ← 入力内容を覚えておく
    if (!isEditing) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = MaterialTheme.colorScheme.surfaceContainer)
                .padding(vertical = 8.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.width(IntrinsicSize.Min),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = date,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = dayOfWeek,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (dayType) {
                        DayType.WEEKDAY -> localExtraColors.current.weekDayText
                        DayType.SATURDAY -> localExtraColors.current.saturdayText
                        DayType.SUNDAY -> localExtraColors.current.sundayText
                        DayType.HOLIDAY -> localExtraColors.current.holidayText
                    }
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = content,
                fontSize = 16.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    } else {
        val focusRequester = remember { FocusRequester() }
        val interactionSource = remember { MutableInteractionSource() }
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
        val context = LocalContext.current
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = MaterialTheme.colorScheme.surfaceContainer)
                .padding(top = 0.dp, bottom = 0.dp, start = 7.2.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.width(IntrinsicSize.Min),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(
                    onClick = { viewModel.viewModelScope.launch { viewModel.updateDiaryDateToNext(id) } },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowUp,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = date,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = dayOfWeek,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (dayType) {
                        DayType.WEEKDAY -> localExtraColors.current.weekDayText
                        DayType.SATURDAY -> localExtraColors.current.saturdayText
                        DayType.SUNDAY -> localExtraColors.current.sundayText
                        DayType.HOLIDAY -> localExtraColors.current.holidayText
                    },
                )
                IconButton(onClick = {
                    viewModel.viewModelScope.launch {
                        viewModel.updateDiaryDateToPrevious(
                            id
                        )
                    }
                }, modifier = Modifier.size(48.dp)) {
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            BasicTextField(
                value = textFieldValue,
                onValueChange = {
                    textFieldValue = it
                    viewModel.viewModelScope.launch {
                        viewModel.updateDiaryContentWithoutSaving(id, it.text)
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    viewModel.stopEditingCurrent()
                    if (hasSevenDaysPassedSinceFirstLaunch(context = context)
                        && viewModel.diariesUiModel.value.size >= 7
                        && !hasReviewBeenShown(context = context)
                    ) {
                        val manager = ReviewManagerFactory.create(context)
                        val request = manager.requestReviewFlow()
                        request.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // We got the ReviewInfo object
                                val reviewInfo = task.result
                                val flow = manager.launchReviewFlow(context as Activity, reviewInfo)
                                flow.addOnCompleteListener { _ ->
                                    // The flow has finished. The API does not indicate whether the user
                                    // reviewed or not, or even whether the review dialog was shown. Thus, no
                                    // matter the result, we continue our app flow.
                                    markReviewShown(context)
                                }
                            } else {
                                // There was some problem, log or handle the error code.
                                @ReviewErrorCode val reviewErrorCode =
                                    (task.exception as ReviewException).errorCode
                            }
                        }
                    }

                }),
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .onFocusChanged { focus ->
                        if (focus.isFocused) {
                            textFieldValue = textFieldValue.copy(
                                selection = TextRange(textFieldValue.text.length)
                            )
                        }

                    }
                    .background(Color.Transparent)
                    .padding(top = 8.dp, bottom = 8.dp)
                    .requiredWidthIn(min = 250.dp),
                singleLine = false,
                textStyle = LocalTextStyle.current.copy( // テキストスタイルは自前指定
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)),

                ) { innerTextField ->
                TextFieldDefaults.DecorationBox(
                    value = textFieldValue.text,
                    visualTransformation = VisualTransformation.None,
                    innerTextField = innerTextField,
                    singleLine = false,
                    enabled = true,
                    interactionSource = interactionSource,
                    contentPadding = PaddingValues(bottom = 8.dp), // this is how you can remove the padding
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.primary,     // 非フォーカス時の下線色
                    )
                )
            }
        }
    }

}

fun hasSevenDaysPassedSinceFirstLaunch(context: Context): Boolean {
    val prefs = context.getSharedPreferences(MainActivity.APP_PREFS, Context.MODE_PRIVATE)
    val firstLaunchDate = prefs.getLong(MainActivity.FIRST_LAUNCH_DATE, -1)
    if (firstLaunchDate == -1L) return false

    val now = System.currentTimeMillis()
    val fiveDaysMillis = 7L * 24 * 60 * 60 * 1000
    return (now - firstLaunchDate) >= fiveDaysMillis
}

fun markReviewShown(context: Context) {
    val prefs = context.getSharedPreferences(MainActivity.APP_PREFS, Context.MODE_PRIVATE)
    prefs.edit {
        putBoolean(MainActivity.REVIEW_SHOWN, true)
    }
}

fun hasReviewBeenShown(context: Context): Boolean {
    val prefs = context.getSharedPreferences(MainActivity.APP_PREFS, Context.MODE_PRIVATE)
    return prefs.getBoolean(MainActivity.REVIEW_SHOWN, false)
}


@OptIn(ExperimentalFoundationApi::class)
@Preview(showBackground = true)
@Composable
fun OneLineDiaryPreview() {
    DiaryListContent(
        id = "1111",
        date = "03",
        dayOfWeek = "土",
        dayType = DayType.SATURDAY,
        content = "今日はすごく良い天気だった。新しいアプリの設計を考えた。sssssssssssssssssssssssssssssssssssssssssss",
        isEditing = true,
    )
}