package ru.ama.whereme16SDK.data.database

data class LocationDbModelByDays(
  val _id: Long,
  val datestart: Long,
  val dateend: Long?=null
)