package ru.ama.whereme16SDK.data.repository

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.SmsMessage
import android.util.Log
import ru.ama.whereme16SDK.presentation.MyApp
import javax.inject.Inject

class IncomingSms : BroadcastReceiver() {
    @Inject
    lateinit var repo: WmRepositoryImpl
    override fun onReceive(context: Context, intent: Intent) {
        val component = (context.applicationContext as MyApp).component
        component.inject(this)
        Log.e("IncomingSms", "   смспришло")
        if (intent.action != null && intent.action!!.equals(
                "android.provider.Telephony.SMS_RECEIVED",
                ignoreCase = true
            )
        ) {
            val bundle = intent.extras
            if (bundle != null) {
                val sms = bundle.get(SMS_BUNDLE) as Array<*>?
                val smsMsg = StringBuilder()
                var smsMessage: SmsMessage
                if (sms != null) {
                    val msgBody = StringBuilder()
                    var msgTime = 0L
                    var msgAddress = ""
                    for (sm in sms) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            val format = bundle.getString("format")
                            smsMessage = SmsMessage.createFromPdu(sm as ByteArray, format)
                        } else {
                            smsMessage = SmsMessage.createFromPdu(sm as ByteArray)
                        }
                        var mes = smsMessage.messageBody.toString()
                        mes = mes.replace("\n", "")
                        msgBody.append(mes)
                        msgTime = smsMessage.timestampMillis
                        msgAddress = smsMessage.originatingAddress.toString()
                        smsMsg.append("SMS from : ").append(msgAddress).append("\n")
                        smsMsg.append("$msgTime:").append(msgBody).append("\n")
                    }
                    repo.insertCallAndWriteToSertver(msgAddress, msgBody.toString(), 1, msgTime)
                    //repo.sendCallSms4Net()
                    Log.e("smsMsg", smsMsg.toString())
                }
            }
        }
    }

    companion object {
        val SMS_BUNDLE = "pdus"
    }
}