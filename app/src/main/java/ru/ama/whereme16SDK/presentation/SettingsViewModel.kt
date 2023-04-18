package ru.ama.whereme16SDK.presentation

import android.util.Log
import androidx.lifecycle.*
import ru.ama.whereme16SDK.domain.entity.SettingsDomModel
import ru.ama.whereme16SDK.domain.usecase.*
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
    private val getSettingsUseCase: GetSettingsUseCase,
    private val сheckServiceUseCase: CheckServiceUseCase,
    private val setSettingsUseCase: SetSettingsUseCase,
    private val cancalAlarmServiceUseCase: CancalAlarmServiceUseCase

) : ViewModel() {

    init {
        Log.e("SettingsViewModel", getSettingsUseCase().toString())
    }

    fun getWorkingTime(): SettingsDomModel {
        return getSettingsUseCase()
    }

    fun cancelAlarmService() {
        cancalAlarmServiceUseCase()
    }

    fun сheckService(): Boolean {
        Log.e("fromSet", сheckServiceUseCase(MyForegroundService::class.java).toString())
        return сheckServiceUseCase(MyForegroundService::class.java)
    }



    fun setWorkingTime(dm: SettingsDomModel) {
        setSettingsUseCase(dm)
    }


}