package com.x.nocrap.scenes.homeScene

import com.x.nocrap.data.NetworkBoundResource
import com.x.nocrap.data.Resource
import com.x.nocrap.data.local.entity.SuccessResponseEntity
import com.x.nocrap.data.local.pref.SharedPrefStorage
import com.x.nocrap.data.local.pref.UserManager
import com.x.nocrap.data.remote.api.ContentApiService
import com.x.nocrap.utils.RetrofitOkhttpUtil
import io.reactivex.Flowable
import io.reactivex.Observable
import javax.inject.Singleton

@Singleton
class HomeRepository(
    private val preferencesStorage: SharedPrefStorage,
    private val contentApiService: ContentApiService
) {
    fun loginUser(
        name: String,
        email: String, profile_Url: String
    ): Observable<Resource<Boolean>> {
        return object : NetworkBoundResource<Boolean, LoginEntity>() {

            override fun saveCallResult(item: LoginEntity) {

            }

            override fun shouldFetch(): Boolean {
                return true
            }

            override fun loadFromDb(item: Resource<LoginEntity>?): Flowable<Boolean> {
                if (item?.data?.data == null)
                    return Flowable.just(false)
                UserManager.saveUserDTO(preferencesStorage, item.data.data!!);
                //    AppController.getInstance().setUserDetails()
                return Flowable.just(item.data.status)

            }

            override fun createCall(): Observable<Resource<LoginEntity>> {
                return contentApiService.loginUser(
                    RetrofitOkhttpUtil.getRequestBodyText(name),
                    RetrofitOkhttpUtil.getRequestBodyText(email),
                    RetrofitOkhttpUtil.getRequestBodyText(profile_Url)
                )
                    .flatMap { apiResponses ->
                        Observable.just(
                            if (apiResponses == null)
                                Resource.error("", LoginEntity())
                            else
                                Resource.success(apiResponses)
                        )
                    }
            }
        }.getAsObservable()
    }

    fun addLike(mid: Int): Observable<Resource<Boolean>> {
        return object : NetworkBoundResource<Boolean, SuccessResponseEntity>() {

            override fun saveCallResult(item: SuccessResponseEntity) {

            }

            override fun shouldFetch(): Boolean {
                return true
            }

            override fun loadFromDb(item: Resource<SuccessResponseEntity>?): Flowable<Boolean> {
                if (item?.data == null)
                    return Flowable.empty()
                return Flowable.just(!(item.data!!.status && item.data!!.message == "0"))


            }

            override fun createCall(): Observable<Resource<SuccessResponseEntity>> {
                return contentApiService.addLike(
                    RetrofitOkhttpUtil.getRequestBodyText(
                        "" + UserManager.getUserId(
                            preferencesStorage
                        )
                    ),
                    RetrofitOkhttpUtil.getRequestBodyText("" + mid)
                )
                    .flatMap { apiResponses ->
                        Observable.just(
                            if (apiResponses == null)
                                Resource.error("", SuccessResponseEntity())
                            else
                                Resource.success(apiResponses)
                        )
                    }
            }
        }.getAsObservable()
    }


    fun updateViews(mid: Int): Observable<Resource<Boolean>> {
        return object : NetworkBoundResource<Boolean, SuccessResponseEntity>() {

            override fun saveCallResult(item: SuccessResponseEntity) {

            }

            override fun shouldFetch(): Boolean {
                return true
            }

            override fun loadFromDb(item: Resource<SuccessResponseEntity>?): Flowable<Boolean> {
                if (item?.data == null)
                    return Flowable.empty()
                return Flowable.just(!(item.data!!.status))
            }

            override fun createCall(): Observable<Resource<SuccessResponseEntity>> {
                return contentApiService.updateViews(
                    RetrofitOkhttpUtil.getRequestBodyText(
                        "" + UserManager.getUserId(
                            preferencesStorage
                        )
                    ),
                    RetrofitOkhttpUtil.getRequestBodyText("" + mid)
                )
                    .flatMap { apiResponses ->
                        Observable.just(
                            if (apiResponses == null)
                                Resource.error("", SuccessResponseEntity())
                            else
                                Resource.success(apiResponses)
                        )
                    }
            }
        }.getAsObservable()
    }


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
    fun getVideos(mediaCategory: Int, offset: Int): Observable<Resource<VideoListEntity>> {
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
                return contentApiService.getVideos(
                    RetrofitOkhttpUtil.getRequestBodyText(
                        "" + UserManager.getUserId(
                            preferencesStorage
                        )
                    ),
                    RetrofitOkhttpUtil.getRequestBodyText("" + mediaCategory),
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

    fun reportVideo(contentId: String, message: String): Observable<Resource<Boolean>> {
        return object : NetworkBoundResource<Boolean, SuccessResponseEntity>() {

            override fun saveCallResult(item: SuccessResponseEntity) {

            }

            override fun shouldFetch(): Boolean {
                return true
            }

            override fun loadFromDb(item: Resource<SuccessResponseEntity>?): Flowable<Boolean> {
                if (item?.data == null)
                    return Flowable.just(false)
                return Flowable.just(item.data.status)


            }

            override fun createCall(): Observable<Resource<SuccessResponseEntity>> {
                return contentApiService.reportContent(
                    RetrofitOkhttpUtil.getRequestBodyText(
                        "" + UserManager.getUserId(
                            preferencesStorage
                        )
                    ),
                    RetrofitOkhttpUtil.getRequestBodyText("" + contentId),
                    RetrofitOkhttpUtil.getRequestBodyText(message)
                )
                    .flatMap { apiResponses ->
                        Observable.just(
                            if (apiResponses == null)
                                Resource.error("", SuccessResponseEntity())
                            else
                                Resource.success(apiResponses)
                        )
                    }
            }
        }.getAsObservable()
    }

}