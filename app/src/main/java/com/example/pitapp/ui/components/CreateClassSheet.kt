package com.example.pitapp.ui.components

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.pitapp.R
import com.example.pitapp.data.ClassData
import com.example.pitapp.ui.model.ClassState
import com.example.pitapp.utils.determineClassState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateClassSheet(
    sheetState: SheetState,
    scope: CoroutineScope,
    onStartNowClick: () -> Unit,
    onScheduleClick: () -> Unit,
    classes: List<Pair<String, ClassData>>
) {

    val context = LocalContext.current

    val hasClassInProgress = remember(classes) {
        classes.any { (_, classData) ->
            determineClassState(classData) == ClassState.IN_PROGRESS
        }
    }


    if (sheetState.isVisible) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = {
                scope.launch {
                    sheetState.hide()
                }
            },
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.3f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(id = R.string.headline_sheet),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleSmall
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ClassInstantIcon(
                            iconTitle = stringResource(id = R.string.create_class),
                            icon = Icons.Default.Schedule,
                            onClick = {
                                scope.launch {
                                if (hasClassInProgress) {
                                    Toast.makeText(
                                        context,
                                        R.string.class_in_progress,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    sheetState.hide()
                                } else {
                                        sheetState.hide()
                                        onStartNowClick()
                                    }
                                }
                            }
                        )

                        ClassInstantIcon(
                            iconTitle = stringResource(id = R.string.schedule_class),
                            icon = Icons.Default.CalendarMonth,
                            onClick = {
                                scope.launch {
                                    sheetState.hide()
                                    onScheduleClick()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}



