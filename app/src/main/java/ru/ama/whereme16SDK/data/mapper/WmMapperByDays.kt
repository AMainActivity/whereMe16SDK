package ru.ama.whereme16SDK.data.mapper

import ru.ama.whereme16SDK.data.database.LocationDbModelByDays
import ru.ama.whereme16SDK.domain.entity.LocationDbByDays
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class WmMapperByDays @Inject constructor() {


    fun mapDbModelToEntity(dbModel: LocationDbModelByDays) = LocationDbByDays(
        _id = dbModel._id,
        datestart = convertLongToTime(dbModel.datestart),
        dateend = dbModel.dateend?.let { convertLongToTime(it) }
    )

    fun mapEntityToDbModelByDays(db: LocationDbByDays) = LocationDbModelByDays(
        _id = db._id,
        datestart = convertDateToLong(db.datestart),
        dateend = db.dateend?.let { convertDateToLong(it) }
    )

    private fun convertDateToLong(date: String): Long {
        val df = SimpleDateFormat("dd.MM.yyyy")
        return df.parse(date).time
    }

    private fun convertLongToTime(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("dd.MM.yyyy")
        return format.format(date)
    }

}
