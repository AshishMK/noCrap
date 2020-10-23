package com.x.nocrap.utils

import okhttp3.MediaType
import okhttp3.RequestBody
import java.io.File
import java.util.*

class RetrofitOkhttpUtil {
    companion object {
        fun getRequestBodyText(param: String): RequestBody {
            return RequestBody.create(MediaType.parse("text/plain"), param)
        }

        fun getRequestBodyImage(param: File): RequestBody? {
            return RequestBody.create(MediaType.parse("image/*"), param)
        }

        fun getRequestBodyVideo(param: File): RequestBody? {
            return RequestBody.create(MediaType.parse("video/*"), param)
        }

        fun getRequestList(
            list: ArrayList<String>,
            includeDummyIndex: Boolean
        ): ArrayList<RequestBody>? {
            val tmp = ArrayList<RequestBody>()
            for (param in list) {
                tmp.add(getRequestBodyText(param))
            }
            if (tmp.size > 0 && includeDummyIndex) {
                tmp.add(
                    0,
                    getRequestBodyText("dummy data as atleast size of arraylist must be 2")
                )
            }
            return tmp
        }
    }
}