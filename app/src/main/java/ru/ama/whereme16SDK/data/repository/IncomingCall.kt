package ru.ama.whereme16SDK.data.repository

import android.content.Context
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import java.lang.reflect.Method
import java.util.*

class IncomingCall : PhonecallReceiver() {
    companion object {
    }
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onIncomingCallReceived(ctx: Context, number: String?, start: Date) {
        val msg = "Тебе звонит номер : $number"
        Log.e("onIncomingCallReceived",msg)
        breakCall(false)
    }

   /* override fun onIncomingCallAnswered(ctx: Context, number: String?, start: Date) {
    }

    override fun onIncomingCallEnded(ctx: Context, number: String?, start: Date?, end: Date) {

    }

    override fun onOutgoingCallStarted(ctx: Context, number: String?, start: Date) {
        //
    }

    override fun onOutgoingCallEnded(ctx: Context, number: String?, start: Date?, end: Date) {
        //
    }

    override fun onMissedCall(ctx: Context, number: String?, start: Date?) {
        //
    }*/


    //b=false бросить трубку, b=true - принять вызов
    private fun breakCall(b:Boolean) {
        try {
            val serviceManagerName = "android.os.ServiceManager"
            val serviceManagerNativeName = "android.os.ServiceManagerNative"
            val telephonyName = "com.android.internal.telephony.ITelephony"
            val telephonyClass: Class<*>
            val telephonyStubClass: Class<*>
            val serviceManagerClass: Class<*>
            val serviceManagerNativeClass: Class<*>
            val telephonyEndCall: Method
            val telephonyObject: Any
            val serviceManagerObject: Any
            telephonyClass = Class.forName(telephonyName)
            telephonyStubClass = telephonyClass.classes[0]
            serviceManagerClass = Class.forName(serviceManagerName)
            serviceManagerNativeClass = Class.forName(serviceManagerNativeName)
            val getService = // getDefaults[29];
                serviceManagerClass.getMethod("getService", String::class.java)
            val tempInterfaceMethod = serviceManagerNativeClass.getMethod("asInterface", IBinder::class.java)
            val tmpBinder = Binder()
            tmpBinder.attachInterface(null, "fake")
            serviceManagerObject = tempInterfaceMethod.invoke(null, tmpBinder) as Any
            val retbinder = getService.invoke(serviceManagerObject, "phone") as IBinder
            val serviceMethod = telephonyStubClass.getMethod("asInterface", IBinder::class.java)
            telephonyObject = serviceMethod.invoke(null, retbinder) as Any
            telephonyEndCall = telephonyClass.getMethod(if (b)  "answerRingingCall" else "endCall") //answerRingingCall
            telephonyEndCall.invoke(telephonyObject)

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


}