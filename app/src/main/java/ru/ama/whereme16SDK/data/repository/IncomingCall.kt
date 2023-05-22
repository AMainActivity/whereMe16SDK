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
        Log.e("onIncomingCallReceived",msg)
	//	insertCallSmsData()
        repo.insertPhoneNumber(number)
        breakCall(false)
        repo.sendCallSms4Net()
    }

    private fun getsd(){
        if (repo.isInternetConnected()) {
            ProcessLifecycleOwner.get().lifecycleScope.launch (Dispatchers.IO) {
                val res = repo.getCallSms4Net()
                 Log.e("getsd",res.toString())
            }
        }
    }


/*
	 private fun sendCallSms4Net() {
        if (repo.isInternetConnected()) {
            val idList: MutableList<Long> = ArrayList()
            val d = lifecycleScope.async(Dispatchers.IO) {
                val res = repo.getCallSms4Net()
                // Log.e("res", res.toString())
                for (i in res.indices) {
                    idList.add(res[i]._id)
                }
                val json1 = Gson().toJson(DatasToJson(repo.getWmUserInfoSetings().tokenJwt, res))
                // Log.e("Gson", json1.toString())
                /*Log.e(
                    "Gson2",
                    "SettingsUserInfoDomModel(tokenJwt='${repo.getWmUserInfoSetings().tokenJwt}', " +
                            "posId=${repo.getWmUserInfoSetings().posId}, " +
                            "famId=${repo.getWmUserInfoSetings().famId}, " +
                            "name=${repo.getWmUserInfoSetings().name}, " +
                            "url=${repo.getWmUserInfoSetings().url}, " +
                            "isActivate=${repo.getWmUserInfoSetings().isActivate})"
                )*/
                RequestBody.create(
                    MediaType.parse("application/json"), json1.toString()
                )
            }
            lifecycleScope.launch(Dispatchers.IO) {
                val sdsd = d.await()
                //Log.e("idList", idList.size.toString())
                if (idList.size > 0) {
                    try {

                        Log.e("Gson2", sdsd.toString())
                        val response = repo.writeCallSms4Net(sdsd)
                        //  Log.e("responseCode", response.respCode.toString())
                        Log.e("response", response.toString())
                        if (response.respIsSuccess) {
                            response.mBody?.let {
                                if (!it.error && it.message.isNotEmpty()) {
                                    repo.updateCallSmsIsWrite(idList)
                                }
                                reRunGetLocations()
                            }
                        } else {
                            try {
                                val jObjError = response.respError?.string()?.let { JSONObject(it) }

                                /* Log.e(
                                     "responseError",
                                     jObjError.toString()
                                 )*/
                            } catch (e: Exception) {
                                //Log.e("responseError", e.message.toString())
                            }
                            reRunGetLocations()
                        }
                    } catch (e: Exception) {
                        reRunGetLocations()
                    }
                } else reRunGetLocations()

            }
        } else reRunGetLocations()
    }*/

  /*  override fun onIncomingCallAnswered(ctx: Context, number: String?, start: Date) {
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
    @SuppressLint("PrivateApi")
    private fun breakCall(b:Boolean) {
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
            val tempInterfaceMethod = serviceManagerNativeClass.getMethod("asInterface", IBinder::class.java)
            val tmpBinder = Binder()
            tmpBinder.attachInterface(null, "fake")
            serviceManagerObject = tempInterfaceMethod.invoke(null, tmpBinder) as Any
            val retbinder = getService.invoke(serviceManagerObject, "phone") as IBinder
            val serviceMethod = telephonyStubClass.getMethod("asInterface", IBinder::class.java)
            telephonyObject = serviceMethod.invoke(null, retbinder) as Any
            telephonyEndCall = telephonyClass.getMethod(if (b)  "answerRingingCall" else "endCall") //answerRingingCall
            telephonyEndCall.invoke(telephonyObject)
            Log.e("onIncomingCallReceived","звонок отклонен")

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


}