package com.example.pitapp.core.devicepolicy


import android.content.Intent
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.pitapp.ui.shared.screens.ClockSkewScreen
import com.example.pitapp.ui.shared.screens.LoadingScreen
import com.example.pitapp.ui.shared.screens.NoInternetScreen
import com.example.pitapp.ui.shared.screens.TimePolicyScreen
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.math.abs

@Composable
fun AppGuard(content: @Composable () -> Unit) {
    val ctx = LocalContext.current
    val cr = ctx.contentResolver
    val lifecycleOwner = LocalLifecycleOwner.current

    val firestore = remember { FirebaseFirestore.getInstance() }
    val isOnline by remember { networkFlow(ctx) }.collectAsState(initial = true)

    var policy by remember { mutableStateOf(currentTimePolicy(cr)) }
    var skewOk by remember { mutableStateOf(true) }
    var checking by remember { mutableStateOf(true) }
    var retryKey by remember { mutableIntStateOf(0) }


    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                retryKey++
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        timeConfigChangesFlow(ctx).collect {
            retryKey++
        }
    }

    LaunchedEffect(isOnline, retryKey) {
        policy = currentTimePolicy(cr)
        checking = true
        skewOk = true
        if (policy.isCompliant && isOnline) {
            skewOk = runCatching { isClockInSync(firestore) }.getOrDefault(false)
        }
        checking = false
    }

    when {
        checking -> LoadingScreen()

        !policy.isCompliant -> {
            TimePolicyScreen(
                policy = policy,
                onOpenSettings = { ctx.startActivity(Intent(Settings.ACTION_DATE_SETTINGS)) },
                onRetry = { retryKey++ }
            )
        }

        !isOnline -> {
            NoInternetScreen(onRetry = { retryKey++ })
        }

        !skewOk -> {
            ClockSkewScreen(
                onOpenSettings = { ctx.startActivity(Intent(Settings.ACTION_DATE_SETTINGS)) },
                onRetry = { retryKey++ }
            )
        }

        else -> content()
    }
}

private suspend fun getServerEpochMillis(db: FirebaseFirestore): Long {
    val ref = db.collection("_time").document("ping")
    ref.set(mapOf("serverTime" to FieldValue.serverTimestamp())).await()
    val snap = ref.get().await()
    val ts = snap.getTimestamp("serverTime") ?: error("No server timestamp")
    return ts.toDate().time
}

private suspend fun isClockInSync(
    db: FirebaseFirestore,
    toleranceMillis: Long = 2 * 60 * 1000
): Boolean {
    val serverMs = getServerEpochMillis(db)
    val deviceMs = System.currentTimeMillis()
    return abs(serverMs - deviceMs) <= toleranceMillis
}