package ru.ama.whereme16SDK.domain.repository

import androidx.lifecycle.LiveData
import okhttp3.RequestBody
import ru.ama.whereme16SDK.domain.entity.*

interface WmRepository {

    suspend fun getLocationById(mDate: String): LiveData<List<LocationDomModel>>
    fun isInternetConnected(): Boolean

    fun isMyServiceRunning(serviceClass: Class<*>): Boolean
    fun runAlarm(timeInterval: Long)
    fun cancelAlarm()

    fun getSettingsModel(): SettingsDomModel
    fun setWorkingTime(dm: SettingsDomModel)

    fun getWmUserInfoSetings(): SettingsUserInfoDomModel
    fun setWmUserInfoSetings(set: SettingsUserInfoDomModel)

    suspend fun checkWmJwToken(request: RequestBody): ResponseDomModel

    suspend fun checkKod(request: RequestBody): ResponseJwtDomModel
}
