package com.x.nocrap.di.module

import com.x.nocrap.scenes.homeScene.playerFragment.PlayerFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

/*
 *  Module to inject specified list of fragments
 */
@Module
public abstract class FragmentModule {
    @ContributesAndroidInjector
    abstract fun contributePlayerFragment(): PlayerFragment

}