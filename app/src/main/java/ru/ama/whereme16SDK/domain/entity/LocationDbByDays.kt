package ru.ama.whereme16SDK.domain.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LocationDbByDays(
    val _id: Long,
    val datestart: String,
    val dateend: String?=null
) : Parcelable