package ru.ama.whereme16SDK.data.alarms

import android.app.ActivityManager
import android.content.*
import android.os.IBinder
import android.util.Log
import androidx.core.content.ContextCompat
import ru.ama.whereme16SDK.presentation.MyForegroundService

class PeriodicAlarm : BroadcastReceiver() {
    private fun isMyServiceRunning(ctx: Context, serviceClass: Class<*>): Boolean {
        val manager =
            ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    override fun onReceive(p0: Context?, p1: Intent?) {
        Log.e("onReceiveAlarm", "doAlarm")
        try {
            if (!isMyServiceRunning(
                    (p0?.applicationContext as Context),
                    MyForegroundService::class.java
                )
            ) {
                ContextCompat.startForegroundService(
                    p0.applicationContext,
                    MyForegroundService.newIntent(p0.applicationContext)
                )
                Log.e("onStartCommand", "isMyServiceRunning")
            } else {
                Log.e("onStartCommand2", "isMyServiceRunning")
                p0.applicationContext.bindService(
                    MyForegroundService.newIntent(p0.applicationContext),
                    serviceConnection,
                    0
                )
            }
        } catch (e: Exception) {
            Log.d("doAlarm", "Exception getting location -->  ${e.message}")
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = (service as? MyForegroundService.LocalBinder) ?: return
            val foregroundService = binder.getService()
            foregroundService.startGetLocations()
            Log.e("serviceConnection", "onServiceConnected")
        }
        override fun onServiceDisconnected(name: ComponentName?) {
        }
    }
}