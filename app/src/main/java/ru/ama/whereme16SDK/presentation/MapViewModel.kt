package ru.ama.whereme16SDK.presentation

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
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

   // var lldByDay: LiveData<List<LocationDomModel>>? = null
   var resData: LiveData<LocationDomModel>? = null
   /* private val _resList = MutableLiveData<LocationDomModel>()
    val resList: LiveData<LocationDomModel>?
        get() = _resList*/

    init {
        getDataByDate(getCurrentDate())
    }


    fun getCurrentDate(): String {
        val formatter = SimpleDateFormat("dd.MM.yyyy")
        return formatter.format(Date())
    }


    fun d(s: LocationDomModel?): String {
        var mRes = app.getString(R.string.no_data)
        if (s!=null) {
            var mTempRes = ""
            var count = 0
                mTempRes += String.format(
                    app.getString(R.string.logs_format),
                    ++count,
                    s.datestart,
                    s.latitude,
                    s.longitude,
                    s.accuracy,
                    s.info,
                    s.velocity
                )
            mRes = mTempRes
        }
        return mRes
    }

    fun getDataByDate(mDate: String) {
        val ss=viewModelScope.launch(Dispatchers.IO)  {
            resData=getLocationsFromBdByIdUseCase()
        }

    }

}