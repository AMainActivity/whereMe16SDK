package ru.ama.whereme16SDK.data.alarms

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.ama.whereme16SDK.data.repository.WmRepositoryImpl
import ru.ama.whereme16SDK.presentation.MyApp
import javax.inject.Inject

class StartServiceAfterBootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repo: WmRepositoryImpl


    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context?, intent: Intent?) {
         val component =
             (context?.applicationContext as MyApp).component
        component.inject(this)
        Log.e("StartServiceReceiver","onReceive сработал")
        repo.externalScope.launch(Dispatchers.IO)
        {
            val dbModel=repo.getLastValueFromDbOnOff()
            repo.updateLocationOnOff(dbModel._id.toInt(), IS_OFF_INT.toString())
            repo.isOnOff=IS_ON_INT
            repo.runAlarm(3)
        }
        //repo.getSettingsModel()
    }
	
	 private companion object {
        const val IS_ON_INT = 1
        const val IS_OFF_INT = 2
    }
}