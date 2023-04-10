package ru.ama.whereme16SDK.di

import androidx.lifecycle.ViewModel

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import ru.ama.whereme16SDK.presentation.*
import ru.ama.whereme16SDK.presentationn.ViewModelSplash

@Module
interface ViewModelModule {
 @Binds
    @IntoMap
    @ViewModelKey(MaViewModel::class)
    fun bindTestListViewModel(viewModel: MaViewModel): ViewModel
 @Binds
    @IntoMap
    @ViewModelKey(MapViewModel::class)
    fun bindMapViewModel(viewModel: MapViewModel): ViewModel
	
 @Binds
    @IntoMap
    @ViewModelKey(SettingsViewModel::class)
    fun bindSettingsViewModel(viewModel: SettingsViewModel): ViewModel
 @Binds
    @IntoMap
    @ViewModelKey(ProfileInViewModel::class)
    fun bindProfileViewModel(viewModel: ProfileInViewModel): ViewModel
 @Binds
    @IntoMap
    @ViewModelKey(ProfileOutViewModel::class)
    fun bindProfileOutViewModel(viewModel: ProfileOutViewModel): ViewModel
 @Binds
    @IntoMap
    @ViewModelKey(AboutViewModel::class)
    fun bindAboutViewModel(viewModel: AboutViewModel): ViewModel
 @Binds
    @IntoMap
    @ViewModelKey(ViewModelSplash::class)
    fun bindSplashViewModel(viewModel: ViewModelSplash): ViewModel
}
