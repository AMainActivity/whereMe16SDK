package ru.ama.whereme16SDK.presentation

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.ama.whereme16SDK.domain.entity.SettingsDomModel
import ru.ama.whereme16SDK.domain.usecase.CheckServiceUseCase
import ru.ama.whereme16SDK.domain.usecase.GetSettingsUseCase
import ru.ama.whereme16SDK.domain.usecase.SetSettingsUseCase
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
    private val getSettingsUseCase: GetSettingsUseCase,
    private val checkServiceUseCase: CheckServiceUseCase,
    private val setSettingsUseCase: SetSettingsUseCase

) : ViewModel() {
    private val _errorMinDistance = MutableLiveData<Boolean>()
    val errorMinDistance: LiveData<Boolean>
        get() = _errorMinDistance
    private val _errorAccuracy = MutableLiveData<Boolean>()
    val errorAccuracy: LiveData<Boolean>
        get() = _errorAccuracy


    fun getSettings(): SettingsDomModel {
        return getSettingsUseCase()
    }

    fun validateInputData(name: String, idData: SettingsViewNames): Boolean {
        var result = true
        when (idData) {
            SettingsViewNames.MIN_DISTANCE -> {
                if (name.isNotEmpty()) {
                    if (name.toInt() >= 10) {
                        setSettings(
                            getSettings().copy(
                                minDist = name.toInt()
                            )
                        )
                        _errorMinDistance.value = false
                    } else
                        _errorMinDistance.value = true
                } else
                    _errorMinDistance.value = true
            }
            SettingsViewNames.ACCURACY -> {
                if (name.isNotEmpty()) {
                    if (name.toInt() >= 50) {
                        setSettings(
                            getSettings().copy(
                                accuracy = name.toInt()
                            )
                        )
                        _errorAccuracy.value = false
                    } else
                        _errorAccuracy.value = true
                } else
                    _errorAccuracy.value = true
            }
        }
        return result
    }

    fun resetError(idData: SettingsViewNames) {
        when (idData) {
            SettingsViewNames.MIN_DISTANCE -> _errorMinDistance.value = false
            SettingsViewNames.ACCURACY -> _errorAccuracy.value = false
        }

    }

   /* fun cancelAlarmService() {
        cancelAlarmServiceUseCase()
    }*/

    fun checkService(): Boolean {
        Log.e("fromSet", checkServiceUseCase(MyForegroundService::class.java).toString())
        return checkServiceUseCase(MyForegroundService::class.java)
    }


    private fun setSettings(dm: SettingsDomModel) {
        setSettingsUseCase(dm)
    }


}