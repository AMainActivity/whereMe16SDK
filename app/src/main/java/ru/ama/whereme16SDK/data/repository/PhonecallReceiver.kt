package ru.ama.whereme16SDK.data.repository

import android.telephony.TelephonyManager
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import ru.ama.whereme16SDK.di.ApplicationScope
import ru.ama.whereme16SDK.presentation.MyApp
import java.util.*
import javax.inject.Inject


abstract class PhonecallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.e("PhonecallReceiver", "PhonecallReceiver")
        if (intent.action == "android.intent.action.NEW_OUTGOING_CALL") {
            savedNumber = intent.extras?.getString("android.intent.extra.PHONE_NUMBER")
        } else {
            val stateStr = intent.extras?.getString(TelephonyManager.EXTRA_STATE)
            val number = intent.extras!!.getString(TelephonyManager.EXTRA_INCOMING_NUMBER)
            var state = 0
            if (stateStr == TelephonyManager.EXTRA_STATE_RINGING) {
                state = TelephonyManager.CALL_STATE_RINGING
            }
            onCallStateChanged(context, state, number)
        }
    }

    protected abstract fun onIncomingCallReceived(ctx: Context, number: String?, start: Date)

    private fun onCallStateChanged(context: Context, state: Int, number: String?) {
        if (lastState == state) {
            return
        }
        when (state) {
            TelephonyManager.CALL_STATE_RINGING -> {
                isIncoming = true
                callStartTime = Date()
                savedNumber = number
                onIncomingCallReceived(context, number, callStartTime!!)
            }
        }
        lastState = state
    }

    companion object {
        private var lastState = TelephonyManager.CALL_STATE_IDLE
        private var callStartTime: Date? = null
        private var isIncoming: Boolean = false
        private var savedNumber: String? = null
    }
}