package ru.ama.whereme16SDK.domain.usecase

import ru.ama.whereme16SDK.domain.repository.WmRepository
import javax.inject.Inject

class SetIsActivateUseCase @Inject constructor(
    private val repository: WmRepository
) {

   // operator fun invoke(b:Boolean) = repository.setIsActivate(b)
}
