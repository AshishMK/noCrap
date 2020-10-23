package com.x.nocrap.data.local.pref

import android.app.Application
import android.content.Context
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPrefStorage @Inject constructor(application: Application) : PreferencesStorage<Any> {
    companion object {
        var PREF_NAME = "preferencesDB"
    }

    private var context: Context = application


    /*@Inject
    fun SharedPrefStorage(context: Context) {
        this.context = context
    }*/

    override fun writeValue(key: String, value: Any) {
        val editor = context.getSharedPreferences(
            PREF_NAME,
            Context.MODE_PRIVATE
        )
            .edit()
        if (value is String) {
            editor.putString(key, value as String)
        } else if (value is Int) {
            editor.putInt(key, value)
        } else if (value is Long) {
            editor.putLong(key, value)
        } else if (value is Boolean) {
            editor.putBoolean(key, value)
        } else if (value is Set<*>) {
            editor.putStringSet(key, value as Set<String>)
        }
        editor.commit()
    }

    override fun readValue(key: String, defaultValue: Any): Any {
        val pref = context.getSharedPreferences(
            PREF_NAME,
            Context.MODE_PRIVATE
        )
        when (defaultValue) {
            is String -> {
                return pref.getString(key, defaultValue as String)!!
            }
            is Int -> {
                return pref.getInt(key, defaultValue)
            }
            is Long -> {
                return pref.getLong(key, defaultValue)
            }
            is Boolean -> {
                return pref.getBoolean(key, defaultValue as Boolean)
            }
            is Set<*> -> {
                return pref.getStringSet(key, defaultValue as Set<String>)!!
            }
        }
        return 0
    }


}