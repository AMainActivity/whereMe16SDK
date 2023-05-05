package ru.ama.whereme16SDK.data.repository

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.gson.Gson
import kotlinx.coroutines.*
import okhttp3.RequestBody
import ru.ama.ottest.data.mapper.WmMapperJwt
import ru.ama.ottest.data.network.WmApiService
import ru.ama.whereme16SDK.data.alarms.PeriodicAlarm
import ru.ama.whereme16SDK.data.database.*
import ru.ama.whereme16SDK.data.mapper.WmMapperLocation
import ru.ama.whereme16SDK.data.mapper.WmMapperSettings
import ru.ama.whereme16SDK.data.mapper.WmMapperUserInfoSettings
import ru.ama.whereme16SDK.di.ApplicationScope
import ru.ama.whereme16SDK.domain.entity.*
import ru.ama.whereme16SDK.domain.repository.WmRepository
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class WmRepositoryImpl @Inject constructor(
    private val mapper: WmMapperLocation,
    private val mapperSetTime: WmMapperSettings,
    private val mapperUserInfoSettings: WmMapperUserInfoSettings,
    private val mapperJwt: WmMapperJwt,
    private val locationDao: LocationDao,
    private val application: Application,
    private val fusedLocationProviderClient: FusedLocationProviderClient,
    @ApplicationScope val externalScope: CoroutineScope,
    private val googleApiAvailability: GoogleApiAvailability,
    private val apiService: WmApiService,
    private val mSettings: SharedPreferences
) : WmRepository {


    private lateinit var workingTimeModel: SettingsDomModel
    var mBestLoc = Location("bestLocationOfBadAccuracy")
    var onLocationChangedListener: ((Boolean) -> Unit)? = null

    private val callback = Callback()

    suspend fun isGooglePlayServicesAvailable(): Boolean = withContext(Dispatchers.Default) {
        when (googleApiAvailability.isGooglePlayServicesAvailable(application)) {
            ConnectionResult.SUCCESS -> true
            else -> false
        }
    }


    private val _isEnathAccuracy = MutableLiveData<Boolean>()
    val isEnathAccuracy: LiveData<Boolean>
        get() = _isEnathAccuracy


    override suspend fun checkKod(request: RequestBody): ResponseJwtDomModel {
        val responc = apiService.chekcKod(request)
        val mBody = responc.body()?.let { mapperJwt.mapDtoToModel(it) }

        val res = ResponseJwtDomModel(
            mBody, responc.isSuccessful, responc.errorBody(), responc.code()
        )

        return res
    }


    override fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = application.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return manager.getRunningServices(Integer.MAX_VALUE)
            .any { it.service.className == serviceClass.name }
    }


    override fun isInternetConnected(): Boolean {
        val cm = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val n = cm.activeNetwork
            if (n != null) {
                val nc = cm.getNetworkCapabilities(n)
                return nc!!.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || nc.hasTransport(
                    NetworkCapabilities.TRANSPORT_WIFI
                )
            }
            return false
        } else {
            val netInfo = cm.activeNetworkInfo
            return netInfo != null && netInfo.isConnectedOrConnecting
        }
    }


    override fun runAlarm(timeInterval: Long) {

        Log.e("runAlarm", "" + timeInterval)
        val am = application.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val i = Intent(application, PeriodicAlarm::class.java)
        val pi = PendingIntent.getBroadcast(application, 0, i, 0)
        val alarmTimeAtUTC = System.currentTimeMillis() + timeInterval * 1_000L
        am.cancel(pi)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTimeAtUTC, pi)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val alarmClockInfo: AlarmManager.AlarmClockInfo =
                AlarmManager.AlarmClockInfo(alarmTimeAtUTC, pi)
            am.setAlarmClock(alarmClockInfo, pi)
        }//KITKAT 19 OR ABOVE
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            am.setExact(
                AlarmManager.RTC_WAKEUP, alarmTimeAtUTC, pi
            )
        }
        //FOR BELOW KITKAT ALL DEVICES
        else {
            am.set(
                AlarmManager.RTC_WAKEUP, alarmTimeAtUTC, pi
            )
        }
    }

    override fun cancelAlarm() {
        Log.e("runAlarm", "cancelAlarm")
        val intent = Intent(application, PeriodicAlarm::class.java)
        val sender = PendingIntent.getBroadcast(application, 0, intent, 0)
        val alarmManager = application.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(sender)
    }

    override fun getSettingsModel() = mapperSetTime.mapDataModelToDomain(
        Gson().fromJson(
            worktime, SettingsDataModel::class.java
        )
    )


    override fun setWorkingTime(dm: SettingsDomModel) {
        worktime = Gson().toJson(mapperSetTime.mapDomainToDataModel(dm))
    }


    override suspend fun getLocationById(mDate: String) =
        Transformations.map(locationDao.getLocationsById(mDate)) {
            it.map {
                mapper.mapDbModelToEntity(it)
            }
        }


    override suspend fun checkWmJwToken(request: RequestBody): ResponseDomModel {
        val responc = apiService.checkToken(request)
        val mBody = responc.body()?.let { mapperJwt.mapAllDtoToModel(it) }

        val res = ResponseDomModel(
            mBody, responc.isSuccessful, responc.errorBody(), responc.code()
        )
        return res
    }


    fun updateIsWrite(idList: List<Long>) = locationDao.updateQuery(idList)


    suspend fun getLocations4Net(): List<LocationDomModel> {
        val d = getWmUserInfoSetings().posId
        val dd = d.toString()
        val res = (locationDao.getLocations4Net(
            if (dd.length <= 8) d else (dd.substring(0, 8).toInt())
        )).map { mapper.mapDbModelToEntity(it) }
        Log.e("getLocations4Net", "posid=$d")
        Log.e("getLocations4Net", "LocationDb={$res}")
        return res
    }


    suspend fun writeLoc4Net(request: RequestBody): ResponseDomModel {
        val responc = apiService.writeLocDatas(request)
        Log.e("writeLoc4Net", responc.toString())
        val mBody = responc.body()?.let { mapperJwt.mapAllDtoToModel(it) }

        val res = ResponseDomModel(
            mBody, responc.isSuccessful, responc.errorBody(), responc.code()
        )
        return res
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        workingTimeModel = getSettingsModel()
        mBestLoc.latitude = 0.0
        mBestLoc.longitude = 0.0
        mBestLoc.accuracy = 1500f
        mBestLoc.speed = 0f
        mBestLoc.time = 0
        _isEnathAccuracy.value = false
        onLocationChangedListener?.invoke(false)
        val request = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 10000
            fastestInterval = 10000
        }

        fusedLocationProviderClient.requestLocationUpdates(
            request, callback, Looper.myLooper()!!
        )
        Log.e("getLocation00", fusedLocationProviderClient.toString())
    }


    private fun updateTimeEndDb(id: Int, time: Long) = locationDao.updateTime2ById(id, time)


    private fun updateValueDb(
        id: Int, newInfo: String/*,
                              lat: Double,
                              lon: Double,
                              acracy: Float*/
    ) = locationDao.updateLocationById(id, newInfo/*,lat,lon,acracy*/)


    fun updateLocationOnOff(id: Int, isOnOff: String) {
        externalScope.launch(Dispatchers.IO) { locationDao.updateLocationOnOff(id, isOnOff) }
    }


    fun getLastValue1() = locationDao.getLastValu1e()


    private fun getLastValueFromDb() = locationDao.getLastValue(getCurrentDate())


    fun getLastValueFromDbOnOff() = locationDao.getLastValueOnOff()


    fun getDate(milliSeconds: Long): String {
        val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = milliSeconds
        return formatter.format(calendar.time)
    }

    fun df(): String {
        val curUtc = System.currentTimeMillis()
        val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = curUtc
        val curCal = formatter.format(calendar.time)
        val curUtc1 = formatter.format(curUtc)
        return "curUtc:$curUtc # curUtc1:$curUtc1 \n cal:${calendar.timeInMillis} # curCal:$curCal"
    }

    fun getCurrentDateMil(): String {
        val formatter = SimpleDateFormat("dd.MM.yyyy")
        return formatter.format(System.currentTimeMillis())
    }

    private fun getCurrentDate(): String {
        val formatter = SimpleDateFormat("dd.MM.yyyy")
        return formatter.format(Date())
    }

    suspend fun saveLocation(location: Location, lTime: Long) {
        val res = LocationDbModel(
            lTime.toString(),
            lTime,
            lTime,
            getDate(lTime),
            location.latitude,
            location.longitude,
            1,
            location.accuracy,
            location.speed,
            0,
            IS_ON_OFF_DEFAULT_INT
        )
        locationDao.insertLocation(res)
        _isEnathAccuracy.postValue(true)
        onLocationChangedListener?.invoke(true)
    }

    private fun gerDistance2Locations(loc1: LocationDbModel, loc2: Location): Float {
        val locA = Location("lastValue")
        locA.latitude = loc1.latitude
        locA.longitude = loc1.longitude
        val locB = Location("newValue")
        locB.latitude = loc2.latitude
        locB.longitude = loc2.longitude
        return locA.distanceTo(locB)
    }

    private inner class Callback : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            val lTime = System.currentTimeMillis()
            if (mBestLoc.longitude == 0.0 || result.lastLocation.accuracy < mBestLoc.accuracy) {
                mBestLoc.latitude = result.lastLocation.latitude
                mBestLoc.longitude = result.lastLocation.longitude
                mBestLoc.accuracy = result.lastLocation.accuracy
                mBestLoc.speed = result.lastLocation.speed
                mBestLoc.time = lTime
            }
            if (result.lastLocation != null && result.lastLocation.accuracy < workingTimeModel.accuracy) {
                /*ProcessLifecycleOwner.get().lifecycleScope*/
                externalScope.launch(Dispatchers.IO) {
                    val lastDbValue = getLastValueFromDb()
                    var isInOff4Bd = IS_ON_OFF_DEFAULT_INT
                    if (isOnOff == IS_ON_INT) isInOff4Bd = IS_ON_INT
                    result.lastLocation.let {
                        if (lastDbValue != null) {
                            val dist = gerDistance2Locations(lastDbValue, it)
                            Log.e("distanceLastNew", dist.toString())
                            if (dist > workingTimeModel.minDist) {
                                val res = LocationDbModel(
                                    lTime.toString(),
                                    lTime,
                                    null,
                                    getDate(lTime),
                                    it.latitude,
                                    it.longitude,
                                    1,
                                    it.accuracy,
                                    it.speed,
                                    0,
                                    isInOff4Bd
                                )
                                isOnOff = IS_ON_OFF_DEFAULT_INT
                                locationDao.insertLocation(res)
                                _isEnathAccuracy.postValue(true)
                                onLocationChangedListener?.invoke(true)
                                Log.e("insertLocation", res.toString())
                            } else {
                                updateTimeEndDb(lastDbValue._id.toInt(), lTime)
                                updateValueDb(
                                    lastDbValue._id.toInt(),
                                    getDate(lastDbValue.datetime.toLong()) + " - " + getDate(lTime)/*,
                                    it.latitude,
                                    it.longitude,
                                    it.accuracy*/
                                )
                                _isEnathAccuracy.postValue(true)
                                onLocationChangedListener?.invoke(true)
                            }
                        } else {
                            val res = LocationDbModel(
                                lTime.toString(),
                                lTime,
                                null,
                                getDate(lTime),
                                it.latitude,
                                it.longitude,
                                1,
                                it.accuracy,
                                it.speed,
                                0,
                                isInOff4Bd
                            )
                            isOnOff = IS_ON_OFF_DEFAULT_INT
                            locationDao.insertLocation(res)
                            _isEnathAccuracy.postValue(true)
                            onLocationChangedListener?.invoke(true)

                            Log.e("insertLocationNull", res.toString())
                        }
                    }

                }
            }
        }
    }

    fun stopLocationUpdates() {
        Log.e("getLocationStop", fusedLocationProviderClient.toString())
        fusedLocationProviderClient.removeLocationUpdates(callback)
    }


    val defaultTime = Gson().toJson(
        SettingsDataModel(
            50, 100, 50, 15
        )
    )
    val defaultUserInfo = Gson().toJson(
        SettingsUserInfoDataModel(
            EMPTY_STRING, 0, 0, EMPTY_STRING, EMPTY_STRING, false
        )
    )

    override fun getWmUserInfoSetings() = mapperUserInfoSettings.mapDataModelToDomain(
        Gson().fromJson(
            jwToken, SettingsUserInfoDataModel::class.java
        )
    )


    override fun setWmUserInfoSetings(dm: SettingsUserInfoDomModel) {
        jwToken = Gson().toJson(mapperUserInfoSettings.mapDomainToDataModel(dm))
    }

    var worktime: String?
        get() {
            val k: String?
            if (mSettings.contains(APP_PREFERENCES_worktime)) {
                k = mSettings.getString(
                    APP_PREFERENCES_worktime, defaultTime
                )
            } else k = defaultTime
            return k
        }
        @SuppressLint("NewApi") set(k) {
            val editor = mSettings.edit()
            editor.putString(APP_PREFERENCES_worktime, k)
            if (Build.VERSION.SDK_INT > 9) {
                editor.apply()
            } else editor.commit()
        }
    var jwToken: String
        get() {
            val k: String
            if (mSettings.contains(APP_PREFERENCES_jwt)) {
                k = mSettings.getString(
                    APP_PREFERENCES_jwt, defaultUserInfo
                ).toString()
            } else k = defaultUserInfo
            return k
        }
        @SuppressLint("NewApi") set(k) {
            val editor = mSettings.edit()
            editor.putString(APP_PREFERENCES_jwt, k)
            if (Build.VERSION.SDK_INT > 9) {
                editor.apply()
            } else editor.commit()
        }
    var isOnOff: Int
        get() {
            val k: Int
            if (mSettings.contains(APP_PREFERENCES_isOnOff)) {
                k = mSettings.getInt(
                    APP_PREFERENCES_isOnOff, IS_ON_OFF_DEFAULT_INT
                )
            } else k = IS_ON_OFF_DEFAULT_INT
            return k
        }
        @SuppressLint("NewApi") set(k) {
            val editor = mSettings.edit()
            editor.putInt(APP_PREFERENCES_isOnOff, k)
            if (Build.VERSION.SDK_INT > 9) {
                editor.apply()
            } else editor.commit()
        }

    private companion object {
        const val APP_PREFERENCES_worktime = "worktime"
        const val APP_PREFERENCES_jwt = "jwt"
        const val APP_PREFERENCES_isOnOff = "isOnOff"
        const val EMPTY_STRING = ""
        const val IS_ON_OFF_DEFAULT_INT = 0
        const val IS_ON_INT = 1
    }

}