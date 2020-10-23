package com.x.nocrap.scenes.likeScene

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.x.nocrap.data.Resource
import com.x.nocrap.data.local.pref.SharedPrefStorage
import com.x.nocrap.data.remote.api.ContentApiService
import com.x.nocrap.scenes.homeScene.HomeRepository
import com.x.nocrap.scenes.homeScene.VideoListEntity
import javax.inject.Inject

/*
     * We are injecting the ContentDao class
     * and the ContentApiService class to the ViewModel.
     * */
class MyLikesViewModel @Inject constructor(
    preferenceStorage: SharedPrefStorage,
    contentApiService: ContentApiService
) : ViewModel() {

    private val homeRepository: MyLikesRepository =
        MyLikesRepository(preferenceStorage, contentApiService)
    private val likeLiveData =
        MutableLiveData<Resource<Boolean>>()
    private val repository: HomeRepository =
        HomeRepository(preferenceStorage, contentApiService)

    /* We are using LiveData to update the UI with the data changes.
     */
    private val contentLiveData =
        MutableLiveData<Resource<VideoListEntity>>()
    private val reportLiveData =
        MutableLiveData<Resource<Boolean>>()

    /*
     * Method called by UI to fetch movies list
     * */
    fun getUserLikesVideos(offset: Int) {
        homeRepository.getUserLikeVideos(offset)
            .subscribe { resource -> getContentLiveData().postValue(resource) }
    }

    fun updateViews(mid: Int) {
        repository.updateViews(mid)
            .subscribe { }
    }


    /*
     * LiveData observed by the UI
     * */
    fun getContentLiveData(): MutableLiveData<Resource<VideoListEntity>> {
        return contentLiveData
    }

    /*
     * Method called by UI to fetch movies list
     * */
    fun reportVideo(contentId: String, message: String) {
        repository.reportVideo(contentId, message)
            .subscribe { resource -> getReportLiveData().postValue(resource) }
    }

    fun getReportLiveData(): MutableLiveData<Resource<Boolean>> {
        return reportLiveData
    }

    fun addLike(mid: Int) {
        repository.addLike(mid)
            .subscribe { resource -> getLikeLiveData().postValue(resource) }
    }

    fun getLikeLiveData(): MutableLiveData<Resource<Boolean>> {
        return likeLiveData
    }


}
