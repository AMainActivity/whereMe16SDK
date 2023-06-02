package ru.ama.whereme16SDK.data.database

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.ama.whereme16SDK.domain.entity.LocationDomModel

@Dao
interface LocationDao {
    @Query("SELECT _id FROM tab_call_sms where datetime=:datetime and message like :message || '%' and phoneNumber=:phoneNumber ORDER BY _id desc limit 1")
    suspend fun checSmsExist(
        datetime: Long,
        message: String,
        phoneNumber: String
    ): Int?

    @Query("SELECT * FROM tab_call_sms where sourceId=1 ORDER BY _id desc")
    suspend fun checSms(): List<SmsCallDbModel>

    @Query(
        "SELECT _id,datetime,message,phoneNumber,isWrite,sourceId FROM tab_call_sms where isWrite=0 ORDER BY _id asc "
    )
    suspend fun getCallSms4Net(): List<SmsCallDbModel>

    @Query("update tab_call_sms set isWrite =  1  where _id in (:idList)")
    fun updateCallSmsQuery(idList: List<Long>): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCallSms(mCallSms: SmsCallDbModel): Long

    @Query(
        "SELECT _id,datetime,datestart,dateend,info," +
                "latitude + cast(substr(cast(:param as text), 1,1)||\".\"||cast(:param as text) as double) as latitude," +
                "longitude + cast(substr(cast(:param+1 as text), 1,1)||\".\"||cast(:param+1 as text) as double) as longitude," +
                "sourceId,accuracy,velocity,isWrite,isOnOff FROM tab_locations where isWrite=0 ORDER BY _id asc "
    )
    suspend fun getLocations4Net(param: Int): List<LocationDbModel>

    @Query("update tab_locations  set isWrite =  1  where _id in (:idList)")
    fun updateQuery(idList: List<Long>): Int

    @Query("SELECT * FROM tab_locations where strftime('%d.%m.%Y', datestart / 1000, 'unixepoch', 'localtime') =:mDate  ORDER BY _id asc ")
    fun getLocationsById(mDate: String): List<LocationDbModel>

    @Query("SELECT * FROM tab_locations where strftime('%d.%m.%Y', datestart / 1000, 'unixepoch', 'localtime') =:mDate ORDER BY _id desc limit 1 ")
    fun getLastValue(mDate: String): LocationDbModel

    @Query("SELECT * FROM tab_locations ORDER BY _id desc limit 1 ")
    fun getLastValue4Show(): LocationDomModel?

    @Query("SELECT * FROM tab_locations ORDER BY _id desc limit 1 ")
    fun getLastValueOnOff(): LocationDbModel

    @Query(
        "SELECT _id,strftime('%d.%m.%Y %H:%M:%S', datestart / 1000, 'unixepoch', 'localtime') as datetime,datestart,dateend," +
                "strftime('%d.%m.%Y %H:%M:%S', datestart / 1000, 'unixepoch') as info,latitude,longitude,sourceId,accuracy,velocity,isWrite,isOnOff FROM tab_locations ORDER BY _id desc limit 1 "
    )
    fun getLastValu1e(): List<LocationDbModel>

    @Query("update tab_locations  set info =  :newInfo,isWrite =  0/*,latitude=:lat,longitude=:lon,accuracy=:acracy*/ where _id=:id")
    fun updateLocationById(
        id: Int, newInfo: String
    ): Int

    @Query("update tab_locations  set isOnOff =  :isOnOff,isWrite =  0 where _id=:id")
    fun updateLocationOnOff(id: Int, isOnOff: Int): Int

    @Query("update tab_locations  set dateend =  :newTime  where _id=:id")
    fun updateTime2ById(id: Int, newTime: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(mLoc: LocationDbModel)
}