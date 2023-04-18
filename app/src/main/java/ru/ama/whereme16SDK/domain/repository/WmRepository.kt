package ru.ama.whereme16SDK.domain.repository

import android.location.Location
import androidx.lifecycle.LiveData
import okhttp3.RequestBody
import ru.ama.whereme16SDK.domain.entity.*

interface WmRepository {


    suspend fun loadData(): List<Int>
    suspend fun GetLocationsFromBd(): LiveData<List<LocationDb>>
    suspend fun getLocationById(mDate: String): LiveData< List<LocationDb>>
    suspend fun getGropingDays(): List<LocationDbByDays>
    fun isInternetConnected(): Boolean

    suspend fun stopData(): Int
    fun isMyServiceRunning(serviceClass: Class<*>): Boolean
    fun runAlarm(timeInterval: Long)
    fun cancelAlarm()

    fun getSettingsModel(): SettingsDomModel
    fun setWorkingTime(dm:SettingsDomModel)

    fun getWmUserInfoSetings(): SettingsUserInfoDomModel
    fun setWmUserInfoSetings(set:SettingsUserInfoDomModel)

    suspend fun checkWmJwToken(request : RequestBody):ResponseEntity

    suspend fun getLastLocation(): Location?
    suspend fun checkKod(request : RequestBody): ResponseJwtEntity
}
