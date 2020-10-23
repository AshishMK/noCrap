package com.x.nocrap.scenes.homeScene

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.x.nocrap.data.Resource
import com.x.nocrap.data.local.pref.SharedPrefStorage
import com.x.nocrap.data.remote.api.ContentApiService
import javax.inject.Inject

/*
     * We are injecting the ContentDao class
     * and the ContentApiService class to the ViewModel.
     * */
class HomeViewModel @Inject constructor(
    preferenceStorage: SharedPrefStorage,
    contentApiService: ContentApiService
) : ViewModel() {

    private val homeRepository: HomeRepository =
        HomeRepository(preferenceStorage, contentApiService)

    /* We are using LiveData to update the UI with the data changes.
     */
    private val loginLiveData =
        MutableLiveData<Resource<Boolean>>()
    private val likeLiveData =
        MutableLiveData<Resource<Boolean>>()
    private val reportLiveData =
        MutableLiveData<Resource<Boolean>>()
    private val contentLiveData =
        MutableLiveData<Resource<VideoListEntity>>()

    public fun clearData() {
        contentLiveData.postValue(Resource.success(VideoListEntity()))
    }

    /*
     * Method called by UI to fetch movies list
     * */
    fun loginUser(name: String, email: String, profile_url: String) {
        homeRepository.loginUser(name, email, profile_url)
            .subscribe { resource -> getUserLiveData().postValue(resource) }
    }

    fun getUserLiveData(): MutableLiveData<Resource<Boolean>> {
        return loginLiveData
    }

    /*
     * Method called by UI to fetch movies list
     * */
    fun getVideos(mediaCategory: Int, offset: Int) {
        homeRepository.getVideos(mediaCategory, offset)
            .subscribe { resource -> getContentLiveData().postValue(resource) }
    }


    /*
     * Method called by UI to fetch movies list
     * */
    fun addLike(mid: Int) {
        homeRepository.addLike(mid)
            .subscribe { resource -> getLikeLiveData().postValue(resource) }
    }

    fun getLikeLiveData(): MutableLiveData<Resource<Boolean>> {
        return likeLiveData
    }


    /*
     * Method called by UI to fetch movies list
     * */
    fun reportVideo(contentId: String, message: String) {
        homeRepository.reportVideo(contentId, message)
            .subscribe { resource -> getReportLiveData().postValue(resource) }
    }

    fun getReportLiveData(): MutableLiveData<Resource<Boolean>> {
        return reportLiveData
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
    fun updateViews(mid: Int) {
        homeRepository.updateViews(mid)
            .subscribe { }
    }


}