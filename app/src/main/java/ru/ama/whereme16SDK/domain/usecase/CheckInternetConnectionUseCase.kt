package ru.ama.whereme16SDK.domain.usecase

import ru.ama.whereme16SDK.domain.repository.WmRepository
import javax.inject.Inject

class CheckInternetConnectionUseCase @Inject constructor(
    private val repository: WmRepository
) {
    operator fun invoke() = repository.isInternetConnected()
}
