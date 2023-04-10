package ru.ama.whereme16SDK.data.database


data class SettingsDataModel(
  val days: List<String>,
  val start: String,
  val end: String,
  var minDist: Int,
  var accuracy: Int,
  var timeOfWaitAccuracy: Int,
  var timeOfWorkingWM: Int,
  var isEnable:Boolean
)