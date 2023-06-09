package ru.ama.whereme16SDK.domain.usecase

import okhttp3.RequestBody
import ru.ama.whereme16SDK.domain.repository.WmRepository
import javax.inject.Inject

class CheckJwtTokenUseCase @Inject constructor(
    private val repository: WmRepository
) {
    suspend operator fun invoke(request: RequestBody) = repository.checkWmJwToken(request)
}
