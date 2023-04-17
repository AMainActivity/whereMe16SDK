package ru.ama.whereme16SDK.presentation

import android.util.Log
import androidx.lifecycle.*
import ru.ama.whereme16SDK.domain.entity.SettingsDomModel
import ru.ama.whereme16SDK.domain.usecase.*
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
    private val getWorkingTimeUseCase: GetWorkingTimeUseCase,
    private val сheckServiceUseCase: CheckServiceUseCase,
    private val setWorkingTimeUseCase: SetWorkingTimeUseCase,
    private val cancalAlarmServiceUseCase: CancalAlarmServiceUseCase

) : ViewModel() {

    init {
        Log.e("SettingsViewModel", getWorkingTimeUseCase().toString())
    }

    fun getWorkingTime(): SettingsDomModel {
        return getWorkingTimeUseCase()
    }

    fun cancelAlarmService() {
        cancalAlarmServiceUseCase()
    }

    fun сheckService(): Boolean {
        Log.e("fromSet", сheckServiceUseCase(MyForegroundService::class.java).toString())
        return сheckServiceUseCase(MyForegroundService::class.java)
    }



    fun setWorkingTime(dm: SettingsDomModel) {
        setWorkingTimeUseCase(dm)
    }


}