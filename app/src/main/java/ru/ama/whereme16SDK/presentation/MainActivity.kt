package ru.ama.whereme16SDK.presentation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import ru.ama.whereme16SDK.R
import ru.ama.whereme16SDK.databinding.ActivityMainBinding
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    val REQUEST_PERMISSION_LOCATION = 10
    private lateinit var viewModel: MaViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val component by lazy {
        (application as MyApp).component
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        component.inject(this)

        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, viewModelFactory)[MaViewModel::class.java]

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        checkGPS()

        val mapFragment = MapFragment()
        val setFragment = SettingsFragment()
        val aboutFragment = AboutFragment()
        setCurrentFragment(mapFragment)
        binding.contentMain.bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.page_1 -> setCurrentFragment(mapFragment)
                R.id.page_2 -> setCurrentFragment(setFragment)
                R.id.page_4 -> setCurrentFragment(aboutFragment)
            }
            true
        }
    }

    private fun setCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.nav_host_fragment_content_main, fragment)
            commit()
        }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }


    private fun startService() {
        if (!viewModel.checkService()) {
            ContextCompat.startForegroundService(
                this,
                MyForegroundService.newIntent(this)
            )
            Log.e("MainActivity", "isMyServiceRunning")
        }
    }

    private fun checkGPS() {
        when {
            isAccessFineLocationGranted(this) -> {
                when {
                    isLocationEnabled(this) -> {
                        startService()
                    }
                    else -> {
                        showGPSNotEnabledDialog(this)
                    }
                }
            }
            else -> {
                requestAccessFineLocationPermission(
                    this,
                    REQUEST_PERMISSION_LOCATION
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_PERMISSION_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    when {
                        isLocationEnabled(this) -> {
                            startService()
                        }
                        else -> {
                            showGPSNotEnabledDialog(this)
                        }
                    }
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.access_denied),
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
            }
        }
    }

    private fun requestAccessFineLocationPermission(activity: AppCompatActivity, requestId: Int) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                Manifest.permission.FOREGROUND_SERVICE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_SMS,
                Manifest.permission.READ_PHONE_STATE
            ),
            requestId
        )
    }

    private fun isAccessFineLocationGranted(context: Context): Boolean {
        return ContextCompat
            .checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
    }


    private fun isLocationEnabled(context: Context): Boolean {
        val locationManager: LocationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun showGPSNotEnabledDialog(context: Context) {
        AlertDialog.Builder(context)
            .setTitle(getString(R.string.turn_on_GPS))
            .setMessage(getString(R.string.yes_or_no))
            .setCancelable(false)
            .setPositiveButton(getString(R.string.yes_string)) { _, _ ->
                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .show()
    }

}