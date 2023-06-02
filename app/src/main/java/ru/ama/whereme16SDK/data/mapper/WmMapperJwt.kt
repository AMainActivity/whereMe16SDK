package ru.ama.ottest.data.mapper

import ru.ama.whereme16SDK.data.network.model.JsonDto
import ru.ama.whereme16SDK.data.network.model.JsonJwtDto
import ru.ama.whereme16SDK.domain.entity.JsonDomModel
import ru.ama.whereme16SDK.domain.entity.JsonJwtDomModel
import javax.inject.Inject

class WmMapperJwt @Inject constructor() {
    fun mapDtoToModel(dto: JsonJwtDto) = JsonJwtDomModel(
        error = dto.error,
        message = dto.message,
        tokenJwt = dto.tokenJwt,
        posId = dto.posId,
        famId = dto.famId,
        name = dto.name,
        url = dto.url,
        isActivate = dto.isActivate
    )

    fun mapAllDtoToModel(dto: JsonDto) = JsonDomModel(
        error = dto.error,
        message = dto.message
    )
}