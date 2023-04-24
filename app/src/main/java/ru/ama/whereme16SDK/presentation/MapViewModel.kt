package ru.ama.whereme16SDK.presentation

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.ama.whereme16SDK.R
import ru.ama.whereme16SDK.domain.entity.LocationDomModel
import ru.ama.whereme16SDK.domain.usecase.GetLocationsFromBdByIdUseCase
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class MapViewModel @Inject constructor(
    private val getLocationsFromBdByIdUseCase: GetLocationsFromBdByIdUseCase,
    private val app: Application
) : ViewModel() {

    var lldByDay: LiveData<List<LocationDomModel>>? = null


    init {
        getDataByDate(getCurrentDate())
    }


    fun getCurrentDate(): String {
        val formatter = SimpleDateFormat("dd.MM.yyyy")
        return formatter.format(Date())
    }


    fun d(s: List<LocationDomModel>): String {
        var mRes = app.getString(R.string.no_data)
        if (s.isNotEmpty()) {
            var mTempRes = ""
            var count = 0
            for (mDat in s) {
                mTempRes += String.format(
                    app.getString(R.string.logs_format),
                    ++count,
                    mDat.datestart,
                    mDat.latitude,
                    mDat.longitude,
                    mDat.accuracy,
                    mDat.info,
                    mDat.velocity
                )
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