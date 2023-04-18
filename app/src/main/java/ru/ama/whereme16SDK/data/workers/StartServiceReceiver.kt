package ru.ama.whereme16SDK.data.workers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import ru.ama.whereme16SDK.data.repository.WmRepositoryImpl
import ru.ama.whereme16SDK.presentation.MyApp
import javax.inject.Inject

class StartServiceReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repo: WmRepositoryImpl


    override fun onReceive(context: Context?, intent: Intent?) {
         val component =
             (context!!.applicationContext as MyApp).component
        component.inject(this)
        Log.e("StartServiceReceiver","onReceive сработал")
        repo.getSettingsModel()
        repo.runAlarm(15)
    }
}