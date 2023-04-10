package ru.ama.whereme16SDK.domain.entity


import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SettingsUserInfoDomModel(
  val tokenJwt: String,
  val posId: Int,
  val famId: Int,
  val name: String? = null,
  val url: String? = null,
  val isActivate: Boolean
) : Parcelable