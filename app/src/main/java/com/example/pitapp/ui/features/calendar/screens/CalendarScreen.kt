package com.example.pitapp.ui.features.calendar.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.pitapp.R
import com.example.pitapp.ui.shared.components.BackScaffold
import com.example.pitapp.ui.features.calendar.components.GeneralCalendarView
import com.example.pitapp.model.NonWorkingDay
import com.example.pitapp.model.Period
import com.example.pitapp.datasource.AuthManager
import com.example.pitapp.datasource.FireStoreManager
import java.time.LocalDate


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarScreen(
    navController: NavHostController,
    authManager: AuthManager,
    fireStoreManager: FireStoreManager
) {
    var isEditing by rememberSaveable { mutableStateOf(false) }
    var editingPeriod by remember { mutableStateOf<Period?>(null) }
    var currentMonth by remember { mutableStateOf(LocalDate.now()) }
    var nonWorkingDays by remember { mutableStateOf<List<NonWorkingDay>>(emptyList()) }
    var periods by remember { mutableStateOf<List<Period>>(emptyList()) }


    LaunchedEffect(currentMonth.year, isEditing) {
        if (!isEditing) {
            nonWorkingDays = fireStoreManager.getNonWorkingDays(currentMonth.year.toString())
            periods = fireStoreManager.getPeriods(currentMonth.year.toString())
        }
    }

    BackScaffold(
        navController = navController,
        authManager = authManager,
        topBarTitle = stringResource(id = R.string.calendar_title)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = isEditing,
                transitionSpec = {
                    slideInVertically(
                        initialOffsetY = { fullHeight -> -fullHeight },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    ) togetherWith slideOutVertically(
                        targetOffsetY = { fullHeight -> -fullHeight },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )
                }, label = ""
            ) { editing ->
                if (editing) {
                    EditCalendarEntriesScreen(
                        fireStoreManager = fireStoreManager,
                        editingPeriod = editingPeriod,
                        displayedMonth = currentMonth,
                        onMonthChange = { currentMonth = it },
                        onFinish = {
                            isEditing = false
                            editingPeriod = null
                        }
                    )
                } else {
                    GeneralCalendarView(
                        displayedMonth = currentMonth,
                        onMonthChange = { currentMonth = it },
                        nonWorkingDays = nonWorkingDays,
                        periods = periods
                    )
                }
            }

            OutlinedButton(
                onClick = { isEditing = !isEditing },
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                val iconSize by animateDpAsState(
                    targetValue = if (isEditing) 32.dp else 24.dp,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
                Icon(
                    imageVector = if (isEditing) Icons.Default.Close else Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(iconSize)
                )
            }

        }
    }
}