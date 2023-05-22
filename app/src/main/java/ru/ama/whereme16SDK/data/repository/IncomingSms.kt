package ru.ama.whereme16SDK.data.repository

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import android.telephony.SmsMessage
import android.util.Log

class IncomingSms : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.e("IncomingSms", "   смспришло")
        val sms = SmsManager.getDefault()
        val bundle = intent.extras
        try {
            if (bundle != null) {
                val pdusObj = bundle.get("pdus") as Array<Any>
                for (i in pdusObj.indices/* i = 0; i < pdusObj.length; i++*/) {
                    val currentMessage = SmsMessage.createFromPdu(pdusObj[i] as ByteArray)
                    val phoneNumber = currentMessage.displayOriginatingAddress
                    val senderNum = phoneNumber
                    val message = currentMessage.displayMessageBody
                    Log.e("SmsReceiver", "senderNum: $senderNum; message: $message")
                }
            }
        } catch (e: Exception) {
            Log.e("Receive Error", " $e")
        }
    }
}