package com.example.pitapp.core.devicepolicy


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged


fun timeConfigChangesFlow(context: Context) = callbackFlow {
    val filter = IntentFilter().apply {
        addAction(Intent.ACTION_TIME_CHANGED)
        addAction(Intent.ACTION_TIMEZONE_CHANGED)
        addAction(Intent.ACTION_DATE_CHANGED)
        addAction(Intent.ACTION_TIME_TICK)
    }
    val receiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            trySend(Unit)
        }
    }
    context.registerReceiver(receiver, filter)
    awaitClose { context.unregisterReceiver(receiver) }
}.distinctUntilChanged()
