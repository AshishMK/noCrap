package com.x.nocrap.scenes.likeScene

import com.x.nocrap.data.NetworkBoundResource
import com.x.nocrap.data.Resource
import com.x.nocrap.data.local.pref.SharedPrefStorage
import com.x.nocrap.data.local.pref.UserManager
import com.x.nocrap.data.remote.api.ContentApiService
import com.x.nocrap.scenes.homeScene.VideoListEntity
import com.x.nocrap.utils.RetrofitOkhttpUtil
import io.reactivex.Flowable
import io.reactivex.Observable
import javax.inject.Singleton

@Singleton
class MyLikesRepository(
    private val preferencesStorage: SharedPrefStorage,
    private val contentApiService: ContentApiService
) {

    /*
     * We are using this method to fetch the content list
     * NetworkBounlodResource is part of the Android architecture
     * components. You will notice that this is a modified version of
     * that class. That class is based on LiveData but here we are
     * using Observable from RxJava.
     *
     * There are three methods called:
     * a. fetch data from server
     * b. fetch data from local
     * c. save data from api in local
     *
     * So basically we fetch data from server, store it locally
     * and then fetch data from local and update the UI with
     * this data.
     *
     * */
    fun getUserLikeVideos(offset: Int): Observable<Resource<VideoListEntity>> {
        return object : NetworkBoundResource<VideoListEntity, VideoListEntity>() {
            override fun saveCallResult(item: VideoListEntity) {}
            override fun shouldFetch(): Boolean {
                return true
            }


            //argument item just to return the response flowable
            // you must retrieve data from databas if you are using database
            override fun loadFromDb(item: Resource<VideoListEntity>?): Flowable<VideoListEntity> {
//it just a return the flowable like a placeholder data use @link Status for result status
                return if (item == null) {
                    Flowable.just(VideoListEntity())
                } else Flowable.just(item.data)
            }


            override fun createCall(): Observable<Resource<VideoListEntity>> {
                return contentApiService.getUserLikesVideos(
                    RetrofitOkhttpUtil.getRequestBodyText(
                        "" + UserManager.getUserId(
                            preferencesStorage
                        )
                    ),
                    RetrofitOkhttpUtil.getRequestBodyText("" + offset)
                )
                    .flatMap { apiResponse ->
                        Observable.just(
                            if (apiResponse == null) Resource.error("", VideoListEntity())
                            else Resource.success(apiResponse)
                        )
                    }
            }

        }.getAsObservable()
    }
}