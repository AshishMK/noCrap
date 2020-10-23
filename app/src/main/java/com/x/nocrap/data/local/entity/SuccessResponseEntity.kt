package com.x.nocrap.data.local.entity

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

open class SuccessResponseEntity() : Parcelable {
    @SerializedName("success")
    @Expose
    var status: Boolean = false

    @SerializedName("message")
    public lateinit var message: String

    constructor(parcel: Parcel) : this() {
        status = (parcel.readValue(Boolean::class.java.classLoader) as Boolean)
        message = parcel.readString()!!
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeValue(status)
        dest.writeString(message)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SuccessResponseEntity> {
        override fun createFromParcel(parcel: Parcel): SuccessResponseEntity {
            return SuccessResponseEntity(parcel)
        }

        override fun newArray(size: Int): Array<SuccessResponseEntity?> {
            return arrayOfNulls(size)
        }
    }
}