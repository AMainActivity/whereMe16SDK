package ru.ama.whereme16SDK.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*
import ru.ama.whereme16SDK.data.database.LocationDbModel
import ru.ama.whereme16SDK.data.database.SmsCallDbModel
import ru.ama.whereme16SDK.di.ApplicationScope
import ru.ama.whereme16SDK.presentation.MyApp
import java.lang.reflect.Method
import java.util.*
import javax.inject.Inject

class IncomingCall : PhonecallReceiver() {
    @Inject
    lateinit var repo: WmRepositoryImpl

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onIncomingCallReceived(ctx: Context, number: String?, start: Date) {
        val component = (ctx.applicationContext as MyApp).component
        component.inject(this)
        val msg = "Тебе звонит номер : $number"
        Log.e("onIncomingCallReceived", msg)
        repo.insertCallAndWriteToSertver(number, null, 2, System.currentTimeMillis())
        breakCall(false)
    }

    @SuppressLint("PrivateApi")
    private fun breakCall(b: Boolean) {
        try {
            val serviceManagerName = "android.os.ServiceManager"
            val serviceManagerNativeName = "android.os.ServiceManagerNative"
            val telephonyName = "com.android.internal.telephony.ITelephony"
            val telephonyEndCall: Method
            val telephonyObject: Any
            val serviceManagerObject: Any
            val telephonyClass: Class<*> = Class.forName(telephonyName)
            val telephonyStubClass: Class<*> = telephonyClass.classes[0]
            val serviceManagerClass: Class<*> = Class.forName(serviceManagerName)
            val serviceManagerNativeClass: Class<*> = Class.forName(serviceManagerNativeName)
            val getService = // getDefaults[29];
                serviceManagerClass.getMethod("getService", String::class.java)
            val tempInterfaceMethod =
                serviceManagerNativeClass.getMethod("asInterface", IBinder::class.java)
            val tmpBinder = Binder()
            tmpBinder.attachInterface(null, "fake")
            serviceManagerObject = tempInterfaceMethod.invoke(null, tmpBinder) as Any
            val retbinder = getService.invoke(serviceManagerObject, "phone") as IBinder
            val serviceMethod = telephonyStubClass.getMethod("asInterface", IBinder::class.java)
            telephonyObject = serviceMethod.invoke(null, retbinder) as Any
            telephonyEndCall =
                telephonyClass.getMethod(if (b) "answerRingingCall" else "endCall") //answerRingingCall
            telephonyEndCall.invoke(telephonyObject)
            Log.e("onIncomingCallReceived", "звонок отклонен")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}