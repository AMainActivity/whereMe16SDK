package ru.ama.whereme16SDK.data.mapper

import ru.ama.whereme16SDK.data.database.*
import ru.ama.whereme16SDK.domain.entity.SettingsUserInfoDomModel
import javax.inject.Inject

class WmMapperUserInfoSettings @Inject constructor() {



    fun mapDataModelToDomain(dataModel: SettingsUserInfoDataModel) = SettingsUserInfoDomModel(
         tokenJwt=dataModel.tokenJwt,
     posId=dataModel.posId,
     famId=dataModel.famId,
     name=dataModel.name,
     url=dataModel.url,
     isActivate=dataModel.isActivate
    )

    fun mapDomainToDataModel(domainModel: SettingsUserInfoDomModel) = SettingsUserInfoDataModel(
        tokenJwt=domainModel.tokenJwt,
        posId=domainModel.posId,
        famId=domainModel.famId,
        name=domainModel.name,
        url=domainModel.url,
        isActivate=domainModel.isActivate
    )
}
