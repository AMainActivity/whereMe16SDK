package ru.ama.whereme16SDK.di

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import ru.ama.whereme16SDK.presentation.AboutViewModel
import ru.ama.whereme16SDK.presentation.MaViewModel
import ru.ama.whereme16SDK.presentation.MapViewModel
import ru.ama.whereme16SDK.presentation.SettingsViewModel
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
    @ViewModelKey(AboutViewModel::class)
    fun bindAboutViewModel(viewModel: AboutViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ViewModelSplash::class)
    fun bindSplashViewModel(viewModel: ViewModelSplash): ViewModel
}
