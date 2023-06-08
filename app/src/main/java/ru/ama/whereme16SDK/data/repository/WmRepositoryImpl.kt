package ru.ama.whereme16SDK.data.repository

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Looper
import android.provider.Telephony
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.gson.Gson
import kotlinx.coroutines.*
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONObject
import ru.ama.ottest.data.mapper.WmMapperJwt
import ru.ama.ottest.data.network.WmApiService
import ru.ama.whereme16SDK.data.database.*
import ru.ama.whereme16SDK.data.mapper.WmMapperLocation
import ru.ama.whereme16SDK.data.mapper.WmMapperSettings
import ru.ama.whereme16SDK.data.mapper.WmMapperUserInfoSettings
import ru.ama.whereme16SDK.data.network.model.DatasToJsonDto
import ru.ama.whereme16SDK.di.ApplicationScope
import ru.ama.whereme16SDK.domain.entity.*
import ru.ama.whereme16SDK.domain.repository.WmRepository
import ru.ama.whereme16SDK.presentation.MyForegroundService
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
    private val wmSettings: WmSettings
) : WmRepository {


    private lateinit var workingTimeModel: SettingsDomModel
    var mBestLoc = Location("bestLocationOfBadAccuracy")
    var onLocationChangedListener: ((Boolean) -> Unit)? = null
    private val callback = Callback()
    private val _isEnathAccuracy = MutableLiveData<Boolean>()
    val isEnathAccuracy: LiveData<Boolean>
        get() = _isEnathAccuracy

    suspend fun isGooglePlayServicesAvailable(): Boolean = withContext(Dispatchers.Default) {
        when (googleApiAvailability.isGooglePlayServicesAvailable(application)) {
            ConnectionResult.SUCCESS -> true
            else -> false
        }
    }

    fun checkInboxSms() {
        externalScope.launch(Dispatchers.IO) {
            readWriteSms()
        }
    }

    private fun readWriteSms() {
        val numberCol = Telephony.TextBasedSmsColumns.ADDRESS
        val textCol = Telephony.TextBasedSmsColumns.BODY
        val timeCol = Telephony.TextBasedSmsColumns.DATE
        //val inboxCol = Telephony.TextBasedSmsColumns.MESSAGE_TYPE_INBOX
        val typeCol = Telephony.TextBasedSmsColumns.TYPE

        val projection = arrayOf(numberCol, textCol, timeCol, typeCol)

        val cursor = application.contentResolver.query(
            Uri.parse("content://sms/inbox"),//Telephony.Sms.CONTENT_URI,
            projection,
            null, null, null
        )
        val numberColIdx = cursor!!.getColumnIndex(numberCol)
        val textColIdx = cursor.getColumnIndex(textCol)
        val timeMesg = cursor.getColumnIndex(timeCol)
        val typeColIdx = cursor.getColumnIndex(typeCol)
        while (cursor.moveToNext()) {
            val number = cursor.getString(numberColIdx)
            var text = cursor.getString(textColIdx)
            text = text.replace("\n", "")
            val time = cursor.getLong(timeMesg)
            val type = cursor.getString(typeColIdx)
            val checkId = externalScope.async(Dispatchers.IO) {
                checkCallSms(time, text.substring(0, 5), number)
            }
            externalScope.launch(Dispatchers.IO) {
                val resId = checkId.await() ?: -1
                if (resId < 0) {
                    Log.e("resId", "$resId")
                    insertSmsCallData(number, text, 1, time)
                }
            }
        }
        cursor.close()
        sendCallSms4Net()
    }

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

    override fun getSettingsModel() = mapperSetTime.mapDataModelToDomain(
        Gson().fromJson(
            wmSettings.worktime, SettingsDataModel::class.java
        )
    )

    override fun setWorkingTime(dm: SettingsDomModel) {
        wmSettings.worktime = Gson().toJson(mapperSetTime.mapDomainToDataModel(dm))
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

    suspend fun getCallSms4Net(): List<SmsCallDbModel> {
        val res = locationDao.getCallSms4Net()
        Log.e("getCallSms4Net", "CallSms={$res}")
        return res
    }

    private suspend fun checkCallSms(
        datetime: Long,
        message: String,
        phoneNumber: String
    ): Int? {
        Log.e("checkCallSms1", "дата: $datetime, сообщенеи: $message ,отправитель: $phoneNumber")
        val res =
            if (datetime > 0 && message.isNotEmpty() && phoneNumber.isNotEmpty()) locationDao.checSmsExist(
                datetime,
                message,
                phoneNumber
            ) else 1
        Log.e("checkCallSms", "checkCallSms={$res}")
        //  Log.e("resId11", "{$res}")
        return res
    }

    private fun updateCallSmsIsWrite(idList: List<Long>) = locationDao.updateCallSmsQuery(idList)

    suspend fun insertCallSmsData(dbModel: SmsCallDbModel) = locationDao.insertCallSms(dbModel)

    suspend fun writeCallSms4Net(request: RequestBody): ResponseDomModel {
        val responc = apiService.writeCallSmsData(request)
        Log.e("writeCallSms4Net", responc.toString())
        val mBody = responc.body()?.let { mapperJwt.mapAllDtoToModel(it) }
        val res = ResponseDomModel(
            mBody, responc.isSuccessful, responc.errorBody(), responc.code()
        )
        return res
    }

    fun insertSmsCallData(number: String?, msg: String?, sourceId: Int, dateTime: Long) {
        externalScope.launch(Dispatchers.IO) {
            val res = SmsCallDbModel(
                dateTime,
                msg,
                number,
                0,
                sourceId
            )
            insertCallSmsData(res)
        }
    }

    fun insertCallAndWriteToSertver(number: String?, msg: String?, sourceId: Int, dateTime: Long) {
        val insertData = externalScope.async(Dispatchers.IO) {
            val res = SmsCallDbModel(
                dateTime,
                msg,
                number,
                0,
                sourceId
            )
            insertCallSmsData(res)
        }
        externalScope.launch {
            val resultInsert = insertData.await()
            sendCallSms4Net()
        }
    }

    private suspend fun send2Server(list: MutableList<Long>, request: RequestBody) {
        if (list.size > 0) {
            try {
                Log.e("Gson2", request.toString())
                val response = writeCallSms4Net(request)
                //  Log.e("responseCode", response.respCode.toString())
                Log.e("response", response.toString())
                if (response.respIsSuccess) {
                    response.mBody?.let {
                        if (!it.error && it.message.isNotEmpty()) {
                            var mSize = list.size
                            while (mSize > 0) {
                                val ssize = if (mSize > 500) 500 else mSize
                                val tempList = list.subList(0, ssize)
                                val r = updateCallSmsIsWrite(tempList)
                                if (r > 0) {
                                    list.subList(0, ssize).clear()
                                    mSize = list.size
                                }
                            }
                        }
                    }
                } else {
                    try {
                        val jObjError = response.respError?.string()?.let { JSONObject(it) }
                        Log.e(
                            "responseError",
                            jObjError.toString()
                        )
                    } catch (e: Exception) {
                        Log.e("responseError", e.message.toString())
                    }
                }
            } catch (e: Exception) {
            }
        }
    }

    private fun sendCallSms4Net() {
        if (isInternetConnected()) {
            val idList: MutableList<Long> = ArrayList()
            val d = externalScope.async(Dispatchers.IO) {
                val res = getCallSms4Net()
                for (i in res.indices) {
                    idList.add(res[i]._id)
                }
                val json1 = Gson().toJson(DatasToJsonDto(getWmUserInfoSetings().tokenJwt, res))
                // Log.e("Gson", json1.toString())
                RequestBody.create(
                    MediaType.parse("application/json"), json1.toString()
                )
            }
            externalScope.launch(Dispatchers.IO) {
                val sdsd = d.await()
                //Log.e("idList", idList.size.toString())
                send2Server(idList, sdsd)
            }
        }
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
            interval = 5000
            fastestInterval = 5000
        }
        fusedLocationProviderClient.requestLocationUpdates(
            request, callback, Looper.myLooper()!!
        )
        Log.e("getLocation00", fusedLocationProviderClient.toString())
    }

    private fun updateTimeEndDb(id: Int, time: Long) = locationDao.updateTime2ById(id, time)

    private fun updateValueDb(
        id: Int, newInfo: String
    ) = locationDao.updateLocationById(id, newInfo)

    fun updateLocationOnOff(id: Int, isOnOff: Int) {
        externalScope.launch(Dispatchers.IO) { locationDao.updateLocationOnOff(id, isOnOff) }
    }

    fun updateIsOnOff(isOnOf: Int) {
        wmSettings.isOnOff = isOnOf
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
                    if (wmSettings.isOnOff == IS_ON_INT) isInOff4Bd = IS_ON_INT
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
                                wmSettings.isOnOff = IS_ON_OFF_DEFAULT_INT
                                isInOff4Bd = IS_ON_OFF_DEFAULT_INT
                                locationDao.insertLocation(res)
                                _isEnathAccuracy.postValue(true)
                                onLocationChangedListener?.invoke(true)
                                Log.e("insertLocation", res.toString())
                            } else {
                                if (wmSettings.isOnOff == IS_ON_INT && lastDbValue.isOnOff == IS_OFF_INT) updateLocationOnOff(
                                    lastDbValue._id.toInt(), IS_ON_OFF_INT
                                )
                                wmSettings.isOnOff = IS_ON_OFF_DEFAULT_INT
                                isInOff4Bd = IS_ON_OFF_DEFAULT_INT
                                updateTimeEndDb(lastDbValue._id.toInt(), lTime)
                                updateValueDb(
                                    lastDbValue._id.toInt(),
                                    getDate(lastDbValue.datetime.toLong()) + " - " + getDate(lTime)
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
                            wmSettings.isOnOff = IS_ON_OFF_DEFAULT_INT
                            isInOff4Bd = IS_ON_OFF_DEFAULT_INT
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

    override fun getWmUserInfoSetings() = mapperUserInfoSettings.mapDataModelToDomain(
        Gson().fromJson(
            wmSettings.jwToken, SettingsUserInfoDataModel::class.java
        )
    )

    override fun setWmUserInfoSetings(dm: SettingsUserInfoDomModel) {
        wmSettings.jwToken = Gson().toJson(mapperUserInfoSettings.mapDomainToDataModel(dm))
    }

    private companion object {
        const val IS_ON_OFF_DEFAULT_INT = 0
        const val IS_ON_INT = 1
        const val IS_OFF_INT = 2
        const val IS_ON_OFF_INT = 3
    }
}