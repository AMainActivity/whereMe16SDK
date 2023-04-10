package ru.ama.whereme16SDK.domain.usecase

import ru.ama.whereme16SDK.domain.repository.WmRepository
import javax.inject.Inject

class GetLocationsFromBdUseCase @Inject constructor(
    private val repository: WmRepository
) {

    operator  suspend fun invoke() = repository.GetLocationsFromBd()
}
