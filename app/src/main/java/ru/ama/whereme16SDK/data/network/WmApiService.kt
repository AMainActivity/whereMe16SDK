package ru.ama.ottest.data.network

import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import ru.ama.whereme16SDK.BuildConfig
import ru.ama.whereme16SDK.data.network.model.JsonDto
import ru.ama.whereme16SDK.data.network.model.JsonJwtDto

interface WmApiService {
    @POST(BuildConfig.GKK_CHECK_PHONE)
    suspend fun chekcKod(
        @Body request: RequestBody
    ): Response<JsonJwtDto>

    @POST(BuildConfig.GKK_CHECK_TOKEN)
    suspend fun checkToken(
        @Body request: RequestBody
    ): Response<JsonDto>

    @POST(BuildConfig.GKK_WRITE_LOC_DATAS)
    suspend fun writeLocDatas(
        @Body request: RequestBody
    ): Response<JsonDto>

    @POST(BuildConfig.GKK_WRITE_CALL_SMS_DATA)
    suspend fun writeCallSmsData(
        @Body request: RequestBody
    ): Response<JsonDto>
}