package com.x.nocrap.di.module

import com.x.nocrap.scenes.homeScene.HomeActivity
import com.x.nocrap.scenes.likeScene.MyLikesActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

/*
 *  Module to inject specified list of activities
 */
@Module
public abstract class ActivityModule {
    @ContributesAndroidInjector
    abstract fun contributeHomeActivity(): HomeActivity

    @ContributesAndroidInjector
    abstract fun contributeMyLikesActivity(): MyLikesActivity

}