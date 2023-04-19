package ru.ama.whereme16SDK.presentationn

import android.app.Application
import android.os.Build
import android.provider.Settings.Secure
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONObject
import ru.ama.whereme16SDK.data.repository.WmRepositoryImpl
import ru.ama.whereme16SDK.domain.entity.JsonJwt
import ru.ama.whereme16SDK.domain.entity.SettingsUserInfoDomModel
import ru.ama.whereme16SDK.domain.usecase.CheckJwtTokenUseCase
import ru.ama.whereme16SDK.domain.usecase.CheckKodUseCase
import ru.ama.whereme16SDK.domain.usecase.GetJwTokenUseCase
import ru.ama.whereme16SDK.domain.usecase.SetJwTokenUseCase
import javax.inject.Inject


class ViewModelSplash @Inject constructor(
    private val checkJwtTokenUseCase: CheckJwtTokenUseCase,
    private val checkKodUseCase: CheckKodUseCase,
    private val setWmJwTokenUseCase: SetJwTokenUseCase,
    private val getJwTokenUseCase: GetJwTokenUseCase,
    private val app: Application,
    private val repositoryImpl: WmRepositoryImpl
) : ViewModel() {
    private lateinit var wmTokenInfoModel: SettingsUserInfoDomModel

    private val _isSuccess = MutableLiveData<JsonJwt>()
    val isSuccess: LiveData<JsonJwt>
        get() = _isSuccess
    private val _isError = MutableLiveData<Unit>()
    val isError: LiveData<Unit>
        get() = _isError
    private val android_id by lazy {
        Secure.getString(
            app.getContentResolver(),
            Secure.ANDROID_ID
        )
    }

    init {
        Log.e("datetime", repositoryImpl.df())
        Log.e("tokenJwt", getJwTokenUseCase().tokenJwt)

        if (getJwTokenUseCase().tokenJwt.length > 3)
            checkJwt(getJwTokenUseCase().tokenJwt)
        else
            checkKod("")
        viewModelScope.launch(Dispatchers.IO) {
            Log.e(
                "getLastValue1",
                repositoryImpl.getLastValue1().toString()
            )
        }
    }

    private val _canStart = MutableLiveData<Unit>()
    val canStart: LiveData<Unit>
        get() = _canStart

    private fun saveUserInfo(res: JsonJwt) {
        setWmJwTokenUseCase(
            SettingsUserInfoDomModel(
                res.tokenJwt,
                res.posId,
                res.famId,
                res.name,
                res.url,
                true
            )
        )
    }

    private fun checkKod(kod: String) {
        val json = JSONObject()
        json.put("phoneId", android_id)
        json.put("phoneName", "${Build.BRAND} ${Build.MODEL} ${Build.ID}")
        val requestBody: RequestBody =
            RequestBody.create(MediaType.parse("application/json"), json.toString())

        Log.e("response1", json.toString())
        viewModelScope.launch {
            val response = checkKodUseCase(requestBody)
            Log.e("responseCode", response.respCode.toString())
            Log.e("response", response.toString())

            if (response.respIsSuccess) {
                response.mBody?.let {
                    if (it.error == false && it.message.equals("1")) {
                        Log.e("tokenJwt2", it.toString())
                        saveUserInfo(it)
                        checkJwt(getJwTokenUseCase().tokenJwt)//_isSuccess.value = it
                    } else
                        _isError.value = Unit

                }
            } else {
                _isError.value = Unit


                try {
                    val jObjError = JSONObject(response.respError?.string())

                    Log.e(
                        "responseError",
                        jObjError.toString()
                    )
                } catch (e: Exception) {
                    Log.e("responseError", e.message.toString())
                }
            }


        }
    }

    private fun checkJwt(kod: String) {
        val json = JSONObject()
        json.put("kod", kod)
        Log.e("kod", kod)
        val requestBody: RequestBody =
            RequestBody.create(MediaType.parse("application/json"), json.toString())

        Log.e("checkJwt1", json.toString())
        viewModelScope.launch {
            val response = checkJwtTokenUseCase(requestBody)
            Log.e("checkJwtCode", response.respCode.toString())
            Log.e("checkJwt", response.toString())
            Log.e("mBody", response.mBody.toString())
            if (response.respIsSuccess) {
                wmTokenInfoModel = getJwTokenUseCase()
                response.mBody?.let {
                    setWmJwTokenUseCase(
                        wmTokenInfoModel.copy(
                            isActivate = (it.message).equals("1")
                        )
                    )
                }
                _canStart.value = Unit
            } else {
                try {
                    val jObjError = JSONObject(response.respError?.string())

                    Log.e(
                        "checkJwtError",
                        jObjError.toString()
                    )
                } catch (e: Exception) {
                    Log.e("checkJwtError", e.message.toString())
                }
                _isError.value = Unit
                setWmJwTokenUseCase(
                    SettingsUserInfoDomModel(
                        "",
                        0,
                        0,
                        "",
                        "",
                        false
                    )
                )
            }


        }
    }

    companion object {}
}
