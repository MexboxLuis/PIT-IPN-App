package com.example.pitapp.ui.features.classes.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.pitapp.R

@Composable
fun SegmentedButtonRow(
    selectedView: StatsViewType,
    onSelectionChange: (StatsViewType) -> Unit
) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        StatsViewType.entries.forEachIndexed { index, viewType ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = StatsViewType.entries.size
                ),
                onClick = { onSelectionChange(viewType) },
                selected = selectedView == viewType,
                label = {
                    Text(
                        text =
                            if (viewType == StatsViewType.REGULARITY)
                                stringResource(R.string.label_status)
                            else
                                stringResource(R.string.label_program)
                    )
                }
            )
        }
    }
}