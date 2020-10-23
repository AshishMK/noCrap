package com.x.nocrap.scenes.homeScene

import android.os.Parcel
import android.os.Parcelable
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class VideoEntity() : Parcelable, BaseObservable() {
    @SerializedName("id")
    @Expose
    @get:Bindable
    var id: Integer = Integer(0)
        set(value) {
            field = value
            notifyPropertyChanged(com.x.nocrap.BR.id)
        }

    @SerializedName("thumb")
    @Expose
    @get:Bindable
    var thumb: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(com.x.nocrap.BR.thumb)
        }

    @SerializedName("url")
    @Expose
    @get:Bindable
    var url: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(com.x.nocrap.BR.url)
        }

    @SerializedName("description")
    @Expose
    @get:Bindable
    var description: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(com.x.nocrap.BR.description)
        }

    @SerializedName("title")
    @Expose
    @get:Bindable
    var title: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(com.x.nocrap.BR.title)
        }

    @SerializedName("user_id")
    @Expose
    @get:Bindable
    var user_id: Integer = Integer(0)
        set(value) {
            field = value
            notifyPropertyChanged(com.x.nocrap.BR.user_id)
        }

    @SerializedName("media_type")
    @Expose
    @get:Bindable
    var media_type: Integer = Integer(0)
        set(value) {
            field = value
            notifyPropertyChanged(com.x.nocrap.BR.media_type)
        }

    @SerializedName("views")
    @Expose
    @get:Bindable
    var views: Integer = Integer(0)
        set(value) {
            field = value
            notifyPropertyChanged(com.x.nocrap.BR.views)
        }

    @SerializedName("likes")
    @Expose
    @get:Bindable
    var likes: Integer = Integer(0)
        set(value) {
            field = value
            notifyPropertyChanged(com.x.nocrap.BR.likes)
        }

    @SerializedName("shares")
    @Expose
    @get:Bindable
    var shares: Integer = Integer(0)
        set(value) {
            field = value
            notifyPropertyChanged(com.x.nocrap.BR.shares)
        }

    @SerializedName("category")
    @Expose
    @get:Bindable
    var category: Integer = Integer(0)
        set(value) {
            field = value
            notifyPropertyChanged(com.x.nocrap.BR.category)
        }

    @SerializedName("emotion")
    @Expose
    @get:Bindable
    var emotion: Integer = Integer(0)
        set(value) {
            field = value
            notifyPropertyChanged(com.x.nocrap.BR.emotion)
        }

    @SerializedName("mid")
    @Expose
    @get:Bindable
    var mid: Integer? = null
        set(value) {
            field = value
            notifyPropertyChanged(com.x.nocrap.BR.mid)
        }

    constructor(parcel: Parcel) : this() {
        id = parcel.readValue(Int::class.java.classLoader) as Integer
        thumb = parcel.readString()!!
        url = parcel.readString()!!
        description = parcel.readString()!!
        title = parcel.readString()!!
        user_id = parcel.readValue(Int::class.java.classLoader) as Integer
        media_type = parcel.readValue(Int::class.java.classLoader) as Integer
        views = parcel.readValue(Int::class.java.classLoader) as Integer
        likes = parcel.readValue(Int::class.java.classLoader) as Integer
        shares = parcel.readValue(Int::class.java.classLoader) as Integer
        category = parcel.readValue(Int::class.java.classLoader) as Integer
        emotion = parcel.readValue(Int::class.java.classLoader) as Integer
        mid = parcel.readValue(Int::class.java.classLoader) as Integer?
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeString(thumb)
        parcel.writeString(url)
        parcel.writeString(description)
        parcel.writeString(title)
        parcel.writeValue(user_id)
        parcel.writeValue(media_type)
        parcel.writeValue(views)
        parcel.writeValue(likes)
        parcel.writeValue(shares)
        parcel.writeValue(category)
        parcel.writeValue(emotion)
        parcel.writeValue(mid)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<VideoEntity> {
        override fun createFromParcel(parcel: Parcel): VideoEntity {
            return VideoEntity(parcel)
        }

        override fun newArray(size: Int): Array<VideoEntity?> {
            return arrayOfNulls(size)
        }
    }

    fun setValues(entity: VideoEntity) {
        this.id = entity.id
        this.thumb = entity.thumb
        this.url = entity.url
        this.description = entity.description
        this.title = entity.title
        this.user_id = entity.user_id
        this.media_type = entity.media_type
        this.views = entity.views
        this.likes = entity.likes
        this.shares = entity.shares
        this.category = entity.category
        this.emotion = entity.emotion
        this.mid = entity.mid
    }

}