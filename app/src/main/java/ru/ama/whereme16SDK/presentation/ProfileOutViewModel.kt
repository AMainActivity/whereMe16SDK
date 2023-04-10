package ru.ama.whereme16SDK.presentation


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONObject
import ru.ama.whereme16SDK.domain.entity.SettingsUserInfoDomModel
import ru.ama.whereme16SDK.domain.usecase.*
import javax.inject.Inject


class ProfileOutViewModel @Inject constructor(
    private val logOutUseCase: LogOutUseCase,
    private val setWmJwTokenUseCase: SetJwTokenUseCase,
    private val getJwTokenUseCase: GetJwTokenUseCase
  //  private val setIsActivateUseCase: SetIsActivateUseCase
) : ViewModel() {

    private lateinit var wmTokenInfoModel: SettingsUserInfoDomModel
    private val _isSuccess = MutableLiveData<Unit>()
    val isSuccess: LiveData<Unit>
        get() = _isSuccess
    init {
        // Log.e("getJwTokenUseCase",getJwTokenUseCase().toString())
		wmTokenInfoModel=getJwTokenUseCase()
    }


fun getSetUserInfo() = wmTokenInfoModel


fun logOut()
{val json = JSONObject()
        json.put("kod", wmTokenInfoModel.tokenJwt)
        val requestBody: RequestBody = RequestBody.create(MediaType.parse("application/json"), json.toString())

    Log.e("response1",json.toString())
 viewModelScope.launch {
     val response = logOutUseCase(requestBody)
     Log.e("responseCode",response.respCode.toString())
     Log.e("response",response.toString())
     if (response.respIsSuccess) {
         response.mBody?.let { 
             if (it.error==false && it.message.equals("1"))
             {
				 setWmJwTokenUseCase(SettingsUserInfoDomModel(
                            "",
                            0,
                            0,
                            "",
                            "",
                            false
                        ))
           //  setIsActivateUseCase(false)
             _isSuccess.value = Unit
			 }
         }
     }
     else
     {
         try {
             val jObjError = JSONObject(response.respError?.string())

             Log.e("responseError",jObjError.toString()/*.getJSONObject("error").getString("message")*/)
         } catch (e: Exception) {
             Log.e("responseError",e.message.toString())
         }}

        }
}
}