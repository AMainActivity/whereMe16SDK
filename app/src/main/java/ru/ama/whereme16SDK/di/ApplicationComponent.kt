package ru.ama.whereme16SDK.di


import android.app.Application
import dagger.BindsInstance
import dagger.Component
import ru.ama.whereme16SDK.data.alarms.StartServiceAfterBootReceiver
import ru.ama.whereme16SDK.data.repository.IncomingCall
import ru.ama.whereme16SDK.data.repository.PhonecallReceiver
import ru.ama.whereme16SDK.diO.DataModule
import ru.ama.whereme16SDK.presentation.*

@ApplicationScope
@Component(
    modules = [
        DataModule::class,
        ViewModelModule::class
    ]
)
interface ApplicationComponent {

    fun inject(activity: MainActivity)
    fun inject(activity: SplashActivity)
    fun inject(startServiceAfterBootReceiver: StartServiceAfterBootReceiver)
    fun inject(incomingCall: IncomingCall)
    fun inject(fragment: SettingsFragment)
    fun inject(fragment: MapFragment)
    fun inject(fragment: AboutFragment)
    fun inject(myForegroundService: MyForegroundService)
    fun inject(application: MyApp)


    @Component.Factory
    interface Factory {

        fun create(
            @BindsInstance application: Application
        ): ApplicationComponent
    }
}