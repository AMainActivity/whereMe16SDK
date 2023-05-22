package ru.ama.whereme16SDK.domain.entity

import okhttp3.ResponseBody

data class ResponseJwtDomModel(
    val mBody: JsonJwtDomModel? = null,
    val respIsSuccess: Boolean,
    val respError: ResponseBody? = null,
    val respCode: Int
)