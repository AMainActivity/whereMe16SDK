package ru.ama.whereme16SDK.data.database


data class SettingsDataModel(
  var minDist: Int,
  var accuracy: Int,
  var timeOfWaitAccuracy: Int,
  var timeOfWorkingWM: Int
)