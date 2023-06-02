package ru.ama.whereme16SDK.data.repository

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Build
import com.google.gson.Gson
import ru.ama.whereme16SDK.data.database.SettingsDataModel
import ru.ama.whereme16SDK.data.database.SettingsUserInfoDataModel
import javax.inject.Inject

class WmSettings @Inject constructor(
    private val mSettings: SharedPreferences
) {
    var worktime: String?
        get() {
            val k: String?
            if (mSettings.contains(APP_PREFERENCES_worktime)) {
                k = mSettings.getString(
                    APP_PREFERENCES_worktime, defaultTime
                )
            } else k = defaultTime
            return k
        }
        @SuppressLint("NewApi") set(k) {
            val editor = mSettings.edit()
            editor.putString(APP_PREFERENCES_worktime, k)
            if (Build.VERSION.SDK_INT > 9) {
                editor.apply()
            } else editor.commit()
        }
    var jwToken: String
        get() {
            val k: String
            if (mSettings.contains(APP_PREFERENCES_jwt)) {
                k = mSettings.getString(
                    APP_PREFERENCES_jwt, defaultUserInfo
                ).toString()
            } else k = defaultUserInfo
            return k
        }
        @SuppressLint("NewApi") set(k) {
            val editor = mSettings.edit()
            editor.putString(APP_PREFERENCES_jwt, k)
            if (Build.VERSION.SDK_INT > 9) {
                editor.apply()
            } else editor.commit()
        }
    var isOnOff: Int
        get() {
            val k: Int
            if (mSettings.contains(APP_PREFERENCES_isOnOff)) {
                k = mSettings.getInt(
                    APP_PREFERENCES_isOnOff, IS_ON_OFF_DEFAULT_INT
                )
            } else k = IS_ON_OFF_DEFAULT_INT
            return k
        }
        @SuppressLint("NewApi") set(k) {
            val editor = mSettings.edit()
            editor.putInt(APP_PREFERENCES_isOnOff, k)
            if (Build.VERSION.SDK_INT > 9) {
                editor.apply()
            } else editor.commit()
        }

    val defaultTime = Gson().toJson(
        SettingsDataModel(
            50, 100
        )
    )
    val defaultUserInfo = Gson().toJson(
        SettingsUserInfoDataModel(
            EMPTY_STRING, 0, 0, EMPTY_STRING, EMPTY_STRING, false
        )
    )

    private companion object {
        const val APP_PREFERENCES_worktime = "worktime"
        const val APP_PREFERENCES_jwt = "jwt"
        const val APP_PREFERENCES_isOnOff = "isOnOff"
        const val EMPTY_STRING = ""
        const val IS_ON_OFF_DEFAULT_INT = 0
    }
}