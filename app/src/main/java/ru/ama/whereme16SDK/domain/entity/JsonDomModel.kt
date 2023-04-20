package ru.ama.whereme16SDK.domain.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import okhttp3.ResponseBody

@Parcelize
data class JsonDomModel(
    val error: Boolean,
    val message: String
) : Parcelable

data class ResponseDomModel(
    val mBody: JsonDomModel? = null,
    val respIsSuccess: Boolean,
    val respError: ResponseBody? = null,
    val respCode: Int
)