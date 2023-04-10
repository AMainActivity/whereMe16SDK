package ru.ama.whereme16SDK.domain.usecase

import ru.ama.whereme16SDK.domain.repository.WmRepository
import javax.inject.Inject

class RunAlarmUseCase @Inject constructor(
    private val repository: WmRepository
) {

    operator fun invoke(timeInterval: Long) = repository.runAlarm(timeInterval)
}
