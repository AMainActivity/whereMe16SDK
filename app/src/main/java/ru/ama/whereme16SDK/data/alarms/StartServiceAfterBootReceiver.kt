package ru.ama.whereme16SDK.data.alarms

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.*
import android.os.IBinder
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.ama.whereme16SDK.data.repository.WmRepositoryImpl
import ru.ama.whereme16SDK.presentation.MyApp
import ru.ama.whereme16SDK.presentation.MyForegroundService
import javax.inject.Inject

class StartServiceAfterBootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repo: WmRepositoryImpl
    private fun isMyServiceRunning(ctx: Context, serviceClass: Class<*>): Boolean {
        val manager = ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }


    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context?, intent: Intent?) {
        val component = (context?.applicationContext as MyApp).component
        component.inject(this)
        Log.e("StartServiceReceiver", "onReceive сработал")
        repo.externalScope.launch(Dispatchers.IO) {
            val dbModel = repo.getLastValueFromDbOnOff()
            repo.updateLocationOnOff(dbModel._id.toInt(), IS_OFF_INT)
            repo.isOnOff = IS_ON_INT
            // repo.runAlarm(7)
            startService(context)
        }
        //repo.getSettingsModel()

    }

    private fun startService(context: Context?) {
        Log.e("onReceiveAlarm", "doAlarm")
        try {
            if (!isMyServiceRunning(
                    (context?.applicationContext as Context), MyForegroundService::class.java
                )
            ) {
                ContextCompat.startForegroundService(
                    context.applicationContext,
                    MyForegroundService.newIntent(context.applicationContext)
                )
                Log.e("onStartCommand", "isMyServiceRunning")
            }
        } catch (e: Exception) {
            Log.d("doAlarm", "Exception getting location -->  ${e.message}")
        }

    }

    private companion object {
        const val IS_ON_INT = 1
        const val IS_OFF_INT = 2
    }
}