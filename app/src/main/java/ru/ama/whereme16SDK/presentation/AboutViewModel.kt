package ru.ama.whereme16SDK.presentation


import androidx.lifecycle.ViewModel
import ru.ama.whereme16SDK.domain.entity.SettingsUserInfoDomModel
import ru.ama.whereme16SDK.domain.usecase.GetJwTokenUseCase
import javax.inject.Inject

class AboutViewModel @Inject constructor(
    private val getJwTokenUseCase: GetJwTokenUseCase

) : ViewModel() {

    private var wmTokenInfoModel: SettingsUserInfoDomModel

    init {

        wmTokenInfoModel = getJwTokenUseCase()
    }

    fun getSetUserInfo() = wmTokenInfoModel


}