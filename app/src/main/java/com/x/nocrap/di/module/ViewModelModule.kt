package com.x.nocrap.di.module

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.an.trailers.factory.ViewModelFactory
import com.x.nocrap.di.ViewModelKey
import com.x.nocrap.scenes.homeScene.HomeViewModel
import com.x.nocrap.scenes.likeScene.MyLikesViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
internal abstract class ViewModelModule {

    @Binds
    internal abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(HomeViewModel::class)
    protected abstract fun homeViewModelViewModel(homeViewModel: HomeViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MyLikesViewModel::class)
    protected abstract fun myLikesViewModel(myLikesViewModel: MyLikesViewModel): ViewModel

}