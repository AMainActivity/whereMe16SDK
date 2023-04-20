package ru.ama.whereme16SDK.data.network.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import okhttp3.ResponseBody

data class JsonDto(
    @Expose
    @SerializedName("error") val error: Boolean,
    @Expose
    @SerializedName("message") val message: String
)