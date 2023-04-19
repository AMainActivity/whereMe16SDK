package ru.ama.whereme16SDK.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.ama.whereme16SDK.domain.entity.LocationDb
import ru.ama.whereme16SDK.domain.usecase.GetLocationsFromBdByIdUseCase
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class MapViewModel @Inject constructor(
    private val getLocationsFromBdByIdUseCase: GetLocationsFromBdByIdUseCase
) : ViewModel() {

    var lldByDay: LiveData<List<LocationDb>>? = null


    init {
        getDataByDate(getCurrentDate())
    }


    fun getCurrentDate(): String {
        val formatter = SimpleDateFormat("dd.MM.yyyy")
        return formatter.format(Date())
    }


    fun d(s: List<LocationDb>): String {
        var mRes = "нет данных"
        if (s.isNotEmpty()) {
            var mTempRes = ""
            var count = 0
            for (mDat in s) {
                mTempRes += "${++count}. ${mDat.datestart}  Ш: ${mDat.latitude} Д: ${mDat.longitude} Точность: ${mDat.accuracy} Инфо: ${mDat.info} Скорость: ${mDat.velocity} <br>"
            }
            mRes = mTempRes
        }
        return mRes
    }

    fun getDataByDate(mDate: String) {
        viewModelScope.launch {
            lldByDay = getLocationsFromBdByIdUseCase(mDate)
        }
    }

}