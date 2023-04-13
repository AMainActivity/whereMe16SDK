package ru.ama.whereme16SDK.presentation

import androidx.lifecycle.*
import kotlinx.coroutines.*
import ru.ama.whereme16SDK.domain.entity.*
import ru.ama.whereme16SDK.domain.usecase.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class MapViewModel @Inject constructor(
    private val getGropingDaysUseCase: GetGropingDaysUseCase,
    private val getLocationsFromBdByIdUseCase: GetLocationsFromBdByIdUseCase
) : ViewModel() {

    var lldByDay: LiveData<List<LocationDb>>? = null

    private val _ld_days = MutableLiveData<List<LocationDbByDays>>()
    val ld_days: LiveData<List<LocationDbByDays>>
        get() = _ld_days

    init {

        viewModelScope.launch {
            _ld_days.value = getGropingDaysUseCase()
        }
        getDataByDate(getCurrentDate())
    }


    fun getCurrentDate(): String {
        val formatter = SimpleDateFormat("dd.MM.yyyy")
        return formatter.format(Date())
    }

    fun getDataByDate(mDate: String) {
        viewModelScope.launch {
            lldByDay = getLocationsFromBdByIdUseCase(mDate)
        }
    }

}