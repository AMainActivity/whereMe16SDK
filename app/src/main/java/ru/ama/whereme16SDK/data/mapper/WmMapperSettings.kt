package ru.ama.whereme16SDK.data.mapper

import ru.ama.whereme16SDK.data.database.*
import ru.ama.whereme16SDK.domain.entity.SettingsDomModel
import javax.inject.Inject

class WmMapperSettings @Inject constructor() {



    fun mapDataModelToDomain(dataModel: SettingsDataModel) = SettingsDomModel(
        minDist =dataModel.minDist,
        accuracy =dataModel.accuracy,
        timeOfWaitAccuracy = dataModel.timeOfWaitAccuracy,
        timeOfWorkingWM = dataModel.timeOfWorkingWM
    )

    fun mapDomainToDataModel(domainModel: SettingsDomModel) = SettingsDataModel(
        minDist =domainModel.minDist,
        accuracy =domainModel.accuracy,
        timeOfWaitAccuracy = domainModel.timeOfWaitAccuracy,
        timeOfWorkingWM = domainModel.timeOfWorkingWM
    )

  
}
