package com.x.nocrap.data.local.pref

interface PreferencesStorage<T> {

    fun writeValue(key: String, value: T)

    fun readValue(key: String, defaultValue: T): T

}