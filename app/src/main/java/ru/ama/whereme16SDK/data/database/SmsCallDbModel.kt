package ru.ama.whereme16SDK.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.ama.whereme16SDK.data.database.SmsCallDbModel.Companion.tabCallSms

@Entity(tableName = tabCallSms)
data class SmsCallDbModel(
    val datetime: Long,
    val message: String? = null,
    val phoneNumber: String? = null,
    val isWrite: Int,
	// 1 - sms, 2 - call
    val sourceId: Int
) {
    @PrimaryKey(autoGenerate = true)
    var _id: Long = 0

    companion object {
        const val tabCallSms = "tab_call_sms"
    }
}
