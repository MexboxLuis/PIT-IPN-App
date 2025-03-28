package com.example.pitapp.ui.features.calendar.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.pitapp.R
import com.example.pitapp.ui.features.calendar.components.EditNonWorkingDayTab
import com.example.pitapp.ui.features.calendar.components.EditPeriodTab
import com.example.pitapp.model.Period
import com.example.pitapp.ui.features.calendar.helpers.getEndLocalDate
import com.example.pitapp.ui.features.calendar.helpers.getStartLocalDate
import com.example.pitapp.datasource.FireStoreManager
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EditCalendarEntriesScreen(
    fireStoreManager: FireStoreManager,
    editingPeriod: Period? = null,
    displayedMonth: LocalDate,
    onMonthChange: (LocalDate) -> Unit,
    onFinish: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val tabTitles = listOf(
        stringResource(id = R.string.tab_title_days),
        stringResource(id = R.string.tab_title_periods)
    )
    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(text = title) }
                )
            }
        }
        when (selectedTab) {
            0 -> EditNonWorkingDayTab(
                fireStoreManager = fireStoreManager,
                onFinish = onFinish,
                displayedMonth = displayedMonth,
                onMonthChange = onMonthChange
            )

            1 -> EditPeriodTab(
                fireStoreManager = fireStoreManager,
                onFinish = onFinish,
                initialStartDate = editingPeriod?.getStartLocalDate(),
                initialEndDate = editingPeriod?.getEndLocalDate(),
                displayedMonth = displayedMonth,
                onMonthChange = onMonthChange
            )
        }
    }
}