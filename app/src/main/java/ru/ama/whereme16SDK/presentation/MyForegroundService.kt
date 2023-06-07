package ru.ama.whereme16SDK.presentation

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONObject
import ru.ama.whereme16SDK.R
import ru.ama.whereme16SDK.data.repository.WmRepositoryImpl
import ru.ama.whereme16SDK.domain.entity.DatasToJson
import ru.ama.whereme16SDK.domain.entity.LocationDomModel
import ru.ama.whereme16SDK.domain.entity.SettingsDomModel
import java.util.*
import javax.inject.Inject

class MyForegroundService : LifecycleService() {
    private var timer: CountDownTimer? = null
    private lateinit var workingTimeModel: SettingsDomModel
    private var isEnath = false
    private val component by lazy {
        (application as MyApp).component
    }
    private val notificationManager by lazy {
        getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }

    @Inject
    lateinit var repo: WmRepositoryImpl

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return LocalBinder()
    }

    inner class LocalBinder : Binder() {
        fun getService() = this@MyForegroundService
    }

    private val notificationBuilder by lazy {
        createNotificationBuilder()
    }

    fun startGetLocations() {
        workingTimeModel = repo.getSettingsModel()
        isEnath = false
        val isGooglePlayServicesAvailab = lifecycleScope.async(Dispatchers.IO) {
            repo.isGooglePlayServicesAvailable()
        }
        lifecycleScope.launch(Dispatchers.Main) {
            if (isGooglePlayServicesAvailab.await()) {
                repo.startLocationUpdates()
            }
        }
    }

    private fun createNotificationBuilder(): NotificationCompat.Builder {
        val resultIntent = Intent(this, SplashActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val resultPendingIntent: PendingIntent? = PendingIntent.getActivity(
            this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val b = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.serv_title))
            .setContentText(getString(R.string.serv_text))
            .setSmallIcon(R.drawable.ic_launcher_background).setOnlyAlertOnce(true)
            .setContentIntent(resultPendingIntent)
        return b
    }

    private fun getFormattedLeftTime(millisUntilFinished: Long): String {
        val seconds = (millisUntilFinished / MILLIS_IN_SECONDS % SECONDS_IN_MINUTE).toInt()
        val minutes = millisUntilFinished / MILLIS_IN_SECONDS / SECONDS_IN_MINUTE
        return String.format(FORMATTED_STRING_MINUTE_SECOND, minutes, seconds)
    }

    private fun startTimer() {
        timer = object : CountDownTimer(
            20 * 1000, 1000
        ) {
            override fun onTick(millisUntilFinished: Long) {
                val notification =
                    notificationBuilder.setContentText("").setContentTitle(buildString {
                        append(getString(R.string.serv_title_))
                        append(
                            if (millisUntilFinished < 1000L) getString(R.string.serv_povtor) else getFormattedLeftTime(
                                millisUntilFinished
                            )
                        )
                    }).setProgress(
                        20,
                        (millisUntilFinished / MILLIS_IN_SECONDS).toInt(),
                        false
                    ).build()
                notificationManager.notify(NOTIFICATION_ID, notification)
            }

            override fun onFinish() {
                cancel()
                if (repo.mBestLoc.longitude != 0.0) lifecycleScope.launch(Dispatchers.IO) {
                    repo.saveLocation(repo.mBestLoc, repo.mBestLoc.time)
                }
                sendData4Net()
                updateNotify(
                    getString(R.string.app_name), getString(R.string.serv_ne_naiden) + repo.getDate(
                        Calendar.getInstance().time.time
                    )
                )
            }
        }
        timer?.start()
    }

    private fun sendData4Net() {
        if (repo.isInternetConnected()) {
            val idList: MutableList<Long> = ArrayList()
            val d = lifecycleScope.async(Dispatchers.IO) {
                val res = repo.getLocations4Net()
                for (i in res.indices) {
                    idList.add(res[i]._id)
                }
                val json1 = Gson().toJson(DatasToJson(repo.getWmUserInfoSetings().tokenJwt, res))
                RequestBody.create(
                    MediaType.parse("application/json"), json1.toString()
                )
            }
            lifecycleScope.launch(Dispatchers.IO) {
                val sdsd = d.await()
                if (idList.size > 0) {
                    try {
                        val response = repo.writeLoc4Net(sdsd)
                        Log.e("response", response.toString())
                        if (response.respIsSuccess) {
                            response.mBody?.let {
                                if (!it.error && it.message.isNotEmpty()) {
                                    var mSize = idList.size
                                    while (mSize > 0) {
                                        val sSize = if (mSize > 500) 500 else mSize
                                        val tempList = idList.subList(0, sSize)
                                        val r = repo.updateIsWrite(tempList)
                                        if (r > 0) {
                                            idList.subList(0, sSize).clear()
                                            mSize = idList.size
                                        }
                                    }
                                }
                                reRunGetLocations()
                            }
                        } else {
                            try {
                                val jObjError = response.respError?.string()?.let { JSONObject(it) }
                            } catch (e: Exception) {
                                Log.e("responseError", e.message.toString())
                            }
                            reRunGetLocations()
                        }
                    } catch (e: Exception) {
                        reRunGetLocations()
                    }
                } else reRunGetLocations()
            }
        } else reRunGetLocations()
    }

    private fun reRunGetLocations() {
        timer?.start()
    }

    private fun updateNotify(title: String, txtBody: String) {
        val notification = notificationBuilder.setContentTitle(title).setContentText(txtBody)
            .setProgress(0, 0, false).build()
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    override fun onCreate() {
        component.inject(this)
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        startTimer()
        repo.onLocationChangedListener = {
            if (it) {
                timer?.cancel()
                sendData4Net()
                updateNotify(
                    getString(R.string.app_name), getString(R.string.serv_uspeh) + repo.getDate(
                        Calendar.getInstance().getTime().time
                    )
                )
                isEnath = true
            }
        }
        startGetLocations()
        return START_STICKY
    }

    var onStartGetLovation: ((LocationDomModel) -> Unit)? = null

    override fun onDestroy() {
        super.onDestroy()
        repo.stopLocationUpdates()
        timer?.cancel()
        lifecycleScope.cancel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "channel_id"
        private const val CHANNEL_NAME = "channel_name"
        private const val NOTIFICATION_ID = 1
        private const val MILLIS_IN_SECONDS = 1000L
        private const val SECONDS_IN_MINUTE = 60
        private const val FORMATTED_STRING_MINUTE_SECOND = "%02d:%02d"

        fun newIntent(context: Context): Intent {
            return Intent(context, MyForegroundService::class.java)
        }
    }
}