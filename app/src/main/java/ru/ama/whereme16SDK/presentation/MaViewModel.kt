package ru.ama.whereme16SDK.presentation

import androidx.lifecycle.*
import ru.ama.whereme16SDK.domain.usecase.*
import javax.inject.Inject

class MaViewModel @Inject constructor(
    private val runAlarmUseCase: RunAlarmUseCase,
    private val getIsActivateUseCase: GetIsActivateUseCase,
    private val getJwTokenUseCase: GetJwTokenUseCase
) : ViewModel() {

private val _isSuccess = MutableLiveData<Boolean>()
    val isSuccess: LiveData<Boolean>
        get() = _isSuccess
		
		
init {
	//_isSuccess.value=getIsActivateUseCase()
}
    fun checkIsActivate()=getJwTokenUseCase().isActivate
    

}