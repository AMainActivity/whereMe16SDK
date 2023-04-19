package ru.ama.whereme16SDK.presentation

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import ru.ama.whereme16SDK.presentationn.ViewModelSplash
import javax.inject.Inject

class SplashActivity : AppCompatActivity() {

    private lateinit var viewModel: ViewModelSplash

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private val component by lazy {
        (application as MyApp).component
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)
        viewModel = ViewModelProvider(this, viewModelFactory)[ViewModelSplash::class.java]
        viewModel.canStart.observe(this) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        viewModel.isError.observe(this) {
            showAlertDialog()
        }


    }
    private fun showAlertDialog() {
        AlertDialog.Builder(this)
            .setTitle("Ошибка")
            .setMessage("Неизвестное устройство, для продолжения свяжитесь с разработчиком...")
            .setCancelable(false)
            .setPositiveButton("Ok") { dialogInterface, i ->
                dialogInterface.dismiss()
                finish()
            }
            .show()
    }

}
