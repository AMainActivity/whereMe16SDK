package ru.ama.whereme16SDK.domain.usecase

import ru.ama.whereme16SDK.domain.repository.WmRepository
import javax.inject.Inject

class GetGropingDaysUseCase @Inject constructor(
    private val repository: WmRepository
) {

    suspend operator fun invoke() = repository.getGropingDays()
}
