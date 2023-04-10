package ru.ama.whereme16SDK.data.mapper

import ru.ama.whereme16SDK.data.database.*
import ru.ama.whereme16SDK.domain.entity.SettingsDomModel
import javax.inject.Inject

class WmMapperSettings @Inject constructor() {



    fun mapDataModelToDomain(dataModel: SettingsDataModel) = SettingsDomModel(
           days=dataModel.days,
           start =dataModel.start,
		end=dataModel.end,
        minDist =dataModel.minDist,
        accuracy =dataModel.accuracy,
        timeOfWaitAccuracy = dataModel.timeOfWaitAccuracy,
        timeOfWorkingWM = dataModel.timeOfWorkingWM,
        isEnable = dataModel.isEnable
    )

    fun mapDomainToDataModel(domainModel: SettingsDomModel) = SettingsDataModel(
        days=domainModel.days,
        start =domainModel.start,
        end =domainModel.end,
        minDist =domainModel.minDist,
        accuracy =domainModel.accuracy,
        timeOfWaitAccuracy = domainModel.timeOfWaitAccuracy,
        timeOfWorkingWM = domainModel.timeOfWorkingWM,
        isEnable = domainModel.isEnable
    )

  
}
