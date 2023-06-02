package ru.ama.whereme16SDK.domain.usecase

import ru.ama.whereme16SDK.domain.entity.SettingsUserInfoDomModel
import ru.ama.whereme16SDK.domain.repository.WmRepository
import javax.inject.Inject

class SetJwTokenUseCase @Inject constructor(
    private val repository: WmRepository
) {
    operator fun invoke(set: SettingsUserInfoDomModel) = repository.setWmUserInfoSetings(set)
}
