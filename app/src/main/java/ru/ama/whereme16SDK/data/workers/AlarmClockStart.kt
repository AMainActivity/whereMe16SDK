package ru.ama.whereme16SDK.data.workers

import android.content.*
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import ru.ama.whereme16SDK.data.repository.WmRepositoryImpl
import ru.ama.whereme16SDK.presentation.MyApp
import ru.ama.whereme16SDK.presentation.MyForegroundService
import javax.inject.Inject


class AlarmClockStart : BroadcastReceiver() {


    @Inject
    lateinit var repo: WmRepositoryImpl

    override fun onReceive(ctx: Context?, intent: Intent?) {
        Log.e("onReceiveAlarmClock", "doAlarm")
         val component =
             (ctx!!.applicationContext as MyApp).component
        component.inject(this)
      if (!repo.isMyServiceRunning(MyForegroundService::class.java)) {
                if (repo.IsTimeToGetLocaton())
                       ContextCompat.startForegroundService(
                           ctx!!.applicationContext,
                           MyForegroundService.newIntent(ctx!!.applicationContext)
                       )
                                   else
                                       Toast.makeText(ctx!!.applicationContext,"isTimeToGetLocaton",Toast.LENGTH_SHORT).show()
                       Log.e("onStartFromSet", "isMyServiceRunning")
                   }
    }

   
}