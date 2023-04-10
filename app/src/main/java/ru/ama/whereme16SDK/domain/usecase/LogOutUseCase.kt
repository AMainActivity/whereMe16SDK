package ru.ama.whereme16SDK.domain.usecase

import okhttp3.RequestBody
import ru.ama.whereme16SDK.domain.repository.WmRepository
import javax.inject.Inject

class LogOutUseCase @Inject constructor(
    private val repository: WmRepository
) {

    operator suspend fun invoke(request : RequestBody) = repository.logOut(request)
}
