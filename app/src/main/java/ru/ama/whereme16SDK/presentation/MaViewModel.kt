package ru.ama.whereme16SDK.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import ru.ama.whereme16SDK.domain.usecase.CheckServiceUseCase
import javax.inject.Inject

class MaViewModel @Inject constructor(
    private val checkServiceUseCase: CheckServiceUseCase
) : ViewModel() {

    fun checkService(): Boolean {
        Log.e("fromSet", checkServiceUseCase(MyForegroundService::class.java).toString())
        return checkServiceUseCase(MyForegroundService::class.java)
    }

}