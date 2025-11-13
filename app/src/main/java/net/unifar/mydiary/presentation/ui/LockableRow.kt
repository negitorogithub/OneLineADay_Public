package net.unifar.mydiary.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.unifar.mydiary.R

@Composable
fun LockableRow(
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    lockedOnClick: () -> Unit = {},
    isLocked: Boolean
) {
    if (!isLocked) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    onClick = onClick,
                )
                .padding(16.dp)
        ) {
            content()
        }

    } else {
        Row(
            modifier = Modifier
                .clickable(onClick = lockedOnClick)
                .then(modifier), 
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = stringResource(R.string.lock_icon_description),
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(8.dp))
            content()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LockableRowPreview() {
    Column {
        LockableRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            onClick = {},
            content = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.recover_from_backup),
                        fontSize = 14.sp,
                    )
                }
            },
            isLocked = true
        )
        HorizontalDivider()
        LockableRow(
            onClick = {},
            content = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.recover_from_backup),
                        fontSize = 14.sp,
                    )
                }
            },
            isLocked = false
        )
    }
}