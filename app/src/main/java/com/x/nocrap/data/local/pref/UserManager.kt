package com.x.nocrap.data.local.pref

import com.x.nocrap.data.local.entity.UserDTOEntity

class UserManager {
    companion object {
        var USER_NAME = "user_name"
        var USER_EMAIL = "user_email"
        var USER_ID = "user_id"
        var THEME_MODE = "theme_mode"
        var ADS_TIME = "ads_time"
        var GUIDE_SHOWN = "guide_shown"
        public fun saveUserDTO(
            preferencesStorage: SharedPrefStorage,
            userDTOEntity: UserDTOEntity
        ) {
            preferencesStorage.writeValue(USER_ID, userDTOEntity.id);
            preferencesStorage.writeValue(USER_NAME, userDTOEntity.name);
            preferencesStorage.writeValue(USER_EMAIL, userDTOEntity.email);
        }

        public fun resetUserDTO(preferencesStorage: SharedPrefStorage) {
            preferencesStorage.writeValue(UserManager.USER_ID, -1)
            preferencesStorage.writeValue(UserManager.USER_EMAIL, "")
            preferencesStorage.writeValue(UserManager.USER_NAME, "")

        }

        fun isLoggedIn(preferencesStorage: SharedPrefStorage): Boolean {
            return preferencesStorage.readValue(UserManager.USER_ID, -1) as Int != -1
        }

        fun getUserId(preferencesStorage: SharedPrefStorage): Int {
            return preferencesStorage.readValue(UserManager.USER_ID, -1) as Int
        }


        fun setThemeMode(preferencesStorage: SharedPrefStorage, mode: Int) {
            return preferencesStorage.writeValue(THEME_MODE, mode)
        }

        fun getThemeMode(preferencesStorage: SharedPrefStorage): Int {
            return preferencesStorage.readValue(THEME_MODE, 0) as Int
        }

        fun setRewardedTime(preferencesStorage: SharedPrefStorage, time: Long) {
            return preferencesStorage.writeValue(ADS_TIME, time)
        }

        fun getRewardedTime(preferencesStorage: SharedPrefStorage): Long {
            return preferencesStorage.readValue(ADS_TIME, 0L) as Long
        }

        fun hasShownGuide(preferencesStorage: SharedPrefStorage): Boolean {
            return preferencesStorage.readValue(GUIDE_SHOWN, false) as Boolean
        }

        fun setHasShownGuide(preferencesStorage: SharedPrefStorage) {
            return preferencesStorage.writeValue(GUIDE_SHOWN, true)
        }


    }
}