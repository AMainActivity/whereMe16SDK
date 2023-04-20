package ru.ama.whereme16SDK.data.mapper

import ru.ama.whereme16SDK.data.database.LocationDbModel
import ru.ama.whereme16SDK.domain.entity.LocationDomModel
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class WmMapperLocation @Inject constructor() {


    fun mapDbModelToEntity(dbModel: LocationDbModel) = LocationDomModel(
        _id = dbModel._id,
        datetime = dbModel.datetime,
        datestart = convertLongToTime(dbModel.datestart),
        dateend = dbModel.dateend?.let { convertLongToTime(it) },
        info = dbModel.info,
        latitude = dbModel.latitude,
        longitude = dbModel.longitude,
        sourceId = dbModel.sourceId,
        accuracy = dbModel.accuracy,
        velocity = dbModel.velocity,
        isWrite = dbModel.isWrite
    )

    private fun convertLongToTime(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
        return format.format(date)
    }

}
