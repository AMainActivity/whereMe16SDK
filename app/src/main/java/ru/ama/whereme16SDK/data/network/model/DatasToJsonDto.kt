package ru.ama.whereme16SDK.data.network.model

import ru.ama.whereme16SDK.data.database.SmsCallDbModel

data class DatasToJsonDto(
    val tokenJWT: String,
    val mdata: List<SmsCallDbModel>
)