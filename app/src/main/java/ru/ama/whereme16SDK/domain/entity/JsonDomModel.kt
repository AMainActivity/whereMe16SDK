package ru.ama.whereme16SDK.domain.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class JsonDomModel(
    val error: Boolean,
    val message: String
) : Parcelable