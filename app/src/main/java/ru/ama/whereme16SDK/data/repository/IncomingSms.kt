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
                    for (sm in sms) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            val format = bundle.getString("format")
                            smsMessage = SmsMessage.createFromPdu(sm as ByteArray, format)
                        } else {
                            smsMessage = SmsMessage.createFromPdu(sm as ByteArray)
                        }
                        val msgBody = smsMessage.messageBody.toString()
                        val msgTime = smsMessage.timestampMillis
                        val msgAddress = smsMessage.originatingAddress
                        smsMsg.append("SMS from : ").append(msgAddress).append("\n")
                        smsMsg.append("$msgTime:").append(msgBody).append("\n")
                        repo.insertCallAndWriteToSertver(msgAddress, msgBody, 1, msgTime)
                    }
                    //repo.sendCallSms4Net()
                    Log.e("smsMsg", smsMsg.toString())
                }
            }
        }


        /* val sms = SmsManager.getDefault()
         val bundle = intent.extras
         try {
             if (bundle != null) {
                 val pdusObj = bundle.get("pdus") as Array<*>
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
         }*/
    }

    companion object {

        val SMS_BUNDLE = "pdus"
    }
}


/*
class SMSReceiver : BroadcastReceiver() {

override fun onReceive(context: Context, intent: Intent?) {
    if (intent != null && intent.action != null && intent.action!!.equals("android.provider.Telephony.SMS_RECEIVED", ignoreCase = true)) {
        val bundle = intent.extras
        if (bundle != null) {
            val sms = bundle.get(SMS_BUNDLE) as Array<Any>?
            val smsMsg = StringBuilder()

            var smsMessage: SmsMessage
            if (sms != null) {
                for (sm in sms) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        val format = bundle.getString("format")
                        smsMessage = SmsMessage.createFromPdu(sm as ByteArray, format)
                    } else {
                        smsMessage = SmsMessage.createFromPdu(sm as ByteArray)
                    }


                    val msgBody = smsMessage.messageBody.toString()
                    val msgAddress = smsMessage.originatingAddress

                    smsMsg.append("SMS from : ").append(msgAddress).append("\n")
                    smsMsg.append(msgBody).append("\n")
                }

                sendBroadcast(smsMsg.toString())
            }
        }
    }
}

private fun sendBroadcast(smsMSG: String) {
    val broadcastIntent = Intent()
    broadcastIntent.action = AppConstants.mBroadcastSMSUpdateAction
    broadcastIntent.putExtra(AppConstants.message, smsMSG)
    EventBus.getDefault().post(EventIntent(broadcastIntent))
}

companion object {

    val SMS_BUNDLE = "pdus"
}
}
*/


/*

public class SmsBroadcastReceiver extends BroadcastReceiver {

	private static final String TAG = "SmsBroadcastReceiver";

	private final String serviceProviderNumber;
	private final String serviceProviderSmsCondition;

	private Listener listener;

	public SmsBroadcastReceiver(String serviceProviderNumber, String serviceProviderSmsCondition) {
		this.serviceProviderNumber = serviceProviderNumber;
		this.serviceProviderSmsCondition = serviceProviderSmsCondition;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {
			String smsSender = "";
			String smsBody = "";
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
					smsSender = smsMessage.getDisplayOriginatingAddress();
					smsBody += smsMessage.getMessageBody();
				}
			} else {
				Bundle smsBundle = intent.getExtras();
				if (smsBundle != null) {
					Object[] pdus = (Object[]) smsBundle.get("pdus");
					if (pdus == null) {
						// Display some error to the user
						Log.e(TAG, "SmsBundle had no pdus key");
						return;
					}
					SmsMessage[] messages = new SmsMessage[pdus.length];
					for (int i = 0; i < messages.length; i++) {
						messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
						smsBody += messages[i].getMessageBody();
					}
					smsSender = messages[0].getOriginatingAddress();
				}
			}

			if (smsSender.equals(serviceProviderNumber) && smsBody.startsWith(serviceProviderSmsCondition)) {
				if (listener != null) {
					listener.onTextReceived(smsBody);
				}
			}
		}
	}

	void setListener(Listener listener) {
		this.listener = listener;
	}

	interface Listener {
		void onTextReceived(String text);
	}
}
*/


/*
 private fun readSms()
    {
        val numberCol = Telephony.TextBasedSmsColumns.ADDRESS
        val textCol = Telephony.TextBasedSmsColumns.BODY
        val typeCol = Telephony.TextBasedSmsColumns.TYPE // 1 - Inbox, 2 - Sent

        val projection = arrayOf(numberCol, textCol, typeCol)

        val cursor = contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            projection, null, null, null
        )

        val numberColIdx = cursor!!.getColumnIndex(numberCol)
        val textColIdx = cursor.getColumnIndex(textCol)
        val typeColIdx = cursor.getColumnIndex(typeCol)

        while (cursor.moveToNext()) {
            val number = cursor.getString(numberColIdx)
            val text = cursor.getString(textColIdx)
            val type = cursor.getString(typeColIdx)

            Log.d("MY_APP", "$number $text $type")
        }

        cursor.close()
    }
*/