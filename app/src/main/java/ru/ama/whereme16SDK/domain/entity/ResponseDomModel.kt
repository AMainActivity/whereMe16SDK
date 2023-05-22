package ru.ama.whereme16SDK.domain.entity

import okhttp3.ResponseBody

data class ResponseDomModel(
    val mBody: JsonDomModel? = null,
    val respIsSuccess: Boolean,
    val respError: ResponseBody? = null,
    val respCode: Int
)