package ru.ama.whereme16SDK.domain.usecase

import ru.ama.whereme16SDK.domain.entity.SettingsDomModel
import ru.ama.whereme16SDK.domain.repository.WmRepository
import javax.inject.Inject

class SetSettingsUseCase @Inject constructor(
    private val repository: WmRepository
) {

    operator fun invoke(dm: SettingsDomModel) = repository.setWorkingTime(dm)
}
