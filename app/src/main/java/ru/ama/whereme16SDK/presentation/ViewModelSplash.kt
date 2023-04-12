package ru.ama.whereme16SDK.presentationn

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
import ru.ama.whereme16SDK.domain.usecase.*
import javax.inject.Inject

class ViewModelSplash @Inject constructor(
    private val checkJwtTokenUseCase: CheckJwtTokenUseCase,
    private val checkKodUseCase: CheckKodUseCase,
    private val getJwtFromSetingsUseCase: GetJwtFromSetingsUseCase,
    private val setWmJwTokenUseCase: SetJwTokenUseCase,
    private val getJwTokenUseCase: GetJwTokenUseCase,
    private val repositoryImpl: WmRepositoryImpl
) : ViewModel() {
    private var wmTokenInfoModel: SettingsUserInfoDomModel

    private val _isSuccess = MutableLiveData<JsonJwt>()
    val isSuccess: LiveData<JsonJwt>
    get() = _isSuccess
    private val _isError = MutableLiveData<Unit>()
    val isError: LiveData<Unit>
        get() = _isError
    init {
        wmTokenInfoModel = getJwTokenUseCase()
        Log.e("datetime", repositoryImpl.df())

        if (getJwtFromSetingsUseCase().tokenJwt.length>3)
            checkJwt(getJwtFromSetingsUseCase().tokenJwt)
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
        json.put("kod", "3bd97dde-d8ff-11ed-9d1d-901b0ecbcd5f11223")
        val requestBody: RequestBody =
            RequestBody.create(MediaType.parse("application/json"), json.toString())

        Log.e("response1", json.toString())
        viewModelScope.launch {
            val response = checkKodUseCase(requestBody)
            Log.e("responseCode", response.respCode.toString())
            Log.e("response", response.toString())
            /*

response: ResponseJwtEntity(mBody=JsonJwt(error=true, message=5, tokenJwt=, posId=2, famId=0, name=, url=, isActivate=0), respIsSuccess=true, respError=null, respCode=200)
            * */
            if (response.respIsSuccess) {
                response.mBody?.let {
                    if (it.error == false && it.message.equals("1")) {
                        saveUserInfo(it)
                        checkJwt(getJwtFromSetingsUseCase().tokenJwt)//_isSuccess.value = it
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

    private fun checkJwt(kod:String) {
        val json = JSONObject()
        json.put("kod", kod)
        Log.e("kod",kod)
        val requestBody: RequestBody =
            RequestBody.create(MediaType.parse("application/json"), json.toString())

        Log.e("checkJwt1", json.toString())
        viewModelScope.launch {
            val response = checkJwtTokenUseCase(requestBody)
            Log.e("checkJwtCode", response.respCode.toString())
            Log.e("checkJwt", response.toString())
            Log.e("mBody", response.mBody.toString())
            if (response.respIsSuccess) {
                response.mBody?.let {
                    setWmJwTokenUseCase(
                        wmTokenInfoModel.copy(
                            isActivate = (it.message).equals("1")
                        )
                    )
                    // setIsActivateUseCase(it.message.equals("1"))
                }
                _canStart.value = Unit
            }
            else {
                try {
                    val jObjError = JSONObject(response.respError?.string())

                    Log.e(
                        "checkJwtError",
                        jObjError.toString()
                    )
                } catch (e: Exception) {
                    Log.e("checkJwtError", e.message.toString())
                }
                _isError.value = Unit//_canStart.value = Unit
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
