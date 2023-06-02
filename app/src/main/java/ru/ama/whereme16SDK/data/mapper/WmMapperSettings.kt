package ru.ama.whereme16SDK.data.mapper

import ru.ama.whereme16SDK.data.database.SettingsDataModel
import ru.ama.whereme16SDK.domain.entity.SettingsDomModel
import javax.inject.Inject

class WmMapperSettings @Inject constructor() {

    fun mapDataModelToDomain(dataModel: SettingsDataModel) = SettingsDomModel(
        minDist = dataModel.minDist,
        accuracy = dataModel.accuracy
    )

    fun mapDomainToDataModel(domainModel: SettingsDomModel) = SettingsDataModel(
        minDist = domainModel.minDist,
        accuracy = domainModel.accuracy
    )
}