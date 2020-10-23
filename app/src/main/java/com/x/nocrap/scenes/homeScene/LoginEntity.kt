package com.x.nocrap.scenes.homeScene

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.x.nocrap.data.local.entity.SuccessResponseEntity
import com.x.nocrap.data.local.entity.UserDTOEntity

class LoginEntity() : SuccessResponseEntity(), Parcelable {
    @SerializedName("data")
    @Expose
    var data: UserDTOEntity? = null

    constructor(parcel: Parcel) : this() {
        data = parcel.readValue(UserDTOEntity::class.java.classLoader) as UserDTOEntity?

    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        //super.writeToParcel(parcel, flags)
        parcel.writeValue(data)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LoginEntity> {
        override fun createFromParcel(parcel: Parcel): LoginEntity {
            return LoginEntity(parcel)
        }

        override fun newArray(size: Int): Array<LoginEntity?> {
            return arrayOfNulls(size)
        }
    }

}