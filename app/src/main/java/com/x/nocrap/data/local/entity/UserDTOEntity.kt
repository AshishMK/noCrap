package com.x.nocrap.data.local.entity

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class UserDTOEntity() : Parcelable {
    @SerializedName("id")
    @Expose
    lateinit var id: Integer

    @SerializedName("name")
    @Expose
    lateinit var name: String

    @SerializedName("email")
    @Expose
    lateinit var email: String

    constructor(parcel: Parcel) : this() {
        id = parcel.readValue(Int::class.java.classLoader) as Integer
        name = parcel.readString()!!
        email = parcel.readString()!!
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeValue(id)
        dest.writeString(name)
        dest.writeString(email)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UserDTOEntity> {
        override fun createFromParcel(parcel: Parcel): UserDTOEntity {
            return UserDTOEntity(parcel)
        }

        override fun newArray(size: Int): Array<UserDTOEntity?> {
            return arrayOfNulls(size)
        }
    }
}