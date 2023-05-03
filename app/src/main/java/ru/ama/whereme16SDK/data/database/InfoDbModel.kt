package ru.ama.whereme16SDK.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.ama.whereme16SDK.data.database.InfoDbModel.Companion.tabPowerOnOffInfo

@Entity(tableName = tabPowerOnOffInfo)
data class InfoDbModel(
    val datetime: String,
    val dateL: Long,
    val actionId: Int,
    val isWrite: Int
) {
    @PrimaryKey(autoGenerate = true)
    var _id: Long = 0

    companion object {
        const val tabPowerOnOffInfo = "tab_power_on_off_info"
    }
}
