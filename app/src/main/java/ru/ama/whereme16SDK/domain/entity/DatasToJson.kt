package ru.ama.whereme16SDK.domain.entity

data class DatasToJson(
    val tokenJWT: String,
    val mdata: List<LocationDomModel>
)