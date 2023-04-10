package ru.ama.whereme16SDK.domain.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import okhttp3.ResponseBody

@Parcelize
data class JsonJwt(
    val error: Boolean,
    val message: String,
    val tokenJwt: String,
    val posId: Int,
    val famId: Int,
    val name: String? = null,
    val url: String? = null,
    val isActivate: Int
) : Parcelable


data class ResponseJwtEntity(
    val mBody: JsonJwt?=null,
    val respIsSuccess: Boolean,
    val respError:ResponseBody?=null,
    val respCode:Int
)