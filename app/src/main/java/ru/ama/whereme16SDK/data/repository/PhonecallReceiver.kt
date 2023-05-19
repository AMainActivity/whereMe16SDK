package ru.ama.whereme16SDK.data.repository
import android.telephony.TelephonyManager
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import java.util.*


abstract class PhonecallReceiver : BroadcastReceiver() {


    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.NEW_OUTGOING_CALL") {
            savedNumber = intent.extras?.getString("android.intent.extra.PHONE_NUMBER")
        } else {
            val stateStr = intent.extras?.getString(TelephonyManager.EXTRA_STATE)
            val number = intent.extras!!.getString(TelephonyManager.EXTRA_INCOMING_NUMBER)
            var state = 0
            if (stateStr == TelephonyManager.EXTRA_STATE_IDLE) {
                state = TelephonyManager.CALL_STATE_IDLE
            } else if (stateStr == TelephonyManager.EXTRA_STATE_OFFHOOK) {
                state = TelephonyManager.CALL_STATE_OFFHOOK
            } else if (stateStr == TelephonyManager.EXTRA_STATE_RINGING) {
                state = TelephonyManager.CALL_STATE_RINGING
            }
            onCallStateChanged(context, state, number)
        }
    }

    protected abstract fun onIncomingCallReceived(ctx: Context, number: String?, start: Date)
    /*protected abstract fun onIncomingCallAnswered(ctx: Context, number: String?, start: Date)
    protected abstract fun onIncomingCallEnded(ctx: Context, number: String?, start: Date?, end: Date)
    protected abstract fun onOutgoingCallStarted(ctx: Context, number: String?, start: Date)
    protected abstract fun onOutgoingCallEnded(ctx: Context, number: String?, start: Date?, end: Date)
    protected abstract fun onMissedCall(ctx: Context, number: String?, start: Date?)
*/
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
            /*TelephonyManager.CALL_STATE_OFFHOOK ->
                if (lastState != TelephonyManager.CALL_STATE_RINGING) {
                    isIncoming = false
                    callStartTime = Date()
                    onOutgoingCallStarted(context, savedNumber, callStartTime!!)
                } else {
                    isIncoming = true
                    callStartTime = Date()
                    onIncomingCallAnswered(context, savedNumber, callStartTime!!)
                }
            TelephonyManager.CALL_STATE_IDLE ->
                if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                    onMissedCall(context, savedNumber, callStartTime)
                } else if (isIncoming) {
                    onIncomingCallEnded(context, savedNumber, callStartTime, Date())
                } else {
                    onOutgoingCallEnded(context, savedNumber, callStartTime, Date())
                }*/
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