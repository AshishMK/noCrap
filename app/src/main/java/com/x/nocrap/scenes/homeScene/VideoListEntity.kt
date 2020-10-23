package com.x.nocrap.scenes.homeScene

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.x.nocrap.data.local.entity.SuccessResponseEntity
import java.util.*

class VideoListEntity() : SuccessResponseEntity(), Parcelable {
    @SerializedName("data")
    @Expose
    public var data: ArrayList<VideoEntity>? = null

    constructor(parcel: Parcel) : this() {
        data = parcel.readValue(ArrayList::class.java.classLoader) as ArrayList<VideoEntity>

    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(data)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<VideoListEntity> {
        override fun createFromParcel(parcel: Parcel): VideoListEntity {
            return VideoListEntity(parcel)
        }

        override fun newArray(size: Int): Array<VideoListEntity?> {
            return arrayOfNulls(size)
        }
    }

}