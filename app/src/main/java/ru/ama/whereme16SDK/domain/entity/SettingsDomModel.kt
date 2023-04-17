package ru.ama.whereme16SDK.domain.entity


import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SettingsDomModel(
  var minDist: Int,
  var accuracy: Int,
  var timeOfWaitAccuracy: Int,
  var timeOfWorkingWM: Int
) : Parcelable