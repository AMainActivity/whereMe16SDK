package ru.ama.whereme16SDK.presentation

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ru.ama.whereme16SDK.databinding.FragmentSettingsBinding
import ru.ama.whereme16SDK.domain.entity.SettingsDomModel
import java.text.SimpleDateFormat
import javax.inject.Inject


class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding
        get() = _binding ?: throw RuntimeException("FragmentSettingsBinding == null")
    private lateinit var viewModel: SettingsViewModel
    private var listOfCheckBox = listOf<AppCompatCheckBox>()
    private val component by lazy {
        (requireActivity().application as MyApp).component
    }
    private lateinit var workingTimeModel: SettingsDomModel


    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override fun onAttach(context: Context) {
        component.inject(this)

        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setHasOptionsMenu(true)
        requireActivity().bindService(
            MyForegroundService.newIntent(requireContext()),
            serviceConnection,
            0
        )
    }



    private fun setActionBarSubTitle(txt: String) {
        (requireActivity() as AppCompatActivity).supportActionBar?.subtitle = txt
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root

    }


    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity).supportActionBar?.subtitle = "Настройки"
        viewModel = ViewModelProvider(this, viewModelFactory)[SettingsViewModel::class.java]
        workingTimeModel = viewModel.getWorkingTime()
        binding.frgmntSetSwitchStart.isChecked = viewModel.сheckService()
        binding.frgmntSetSwitchStart.setOnClickListener { view ->
            if (!viewModel.сheckService()) {
                //  if (viewModel.isTimeToGetLocaton())
                ContextCompat.startForegroundService(
                    requireContext(),
                    MyForegroundService.newIntent(requireContext())
                )
                /* else {
                     Toast.makeText(requireContext(), "будильник установлен", Toast.LENGTH_SHORT)
                         .show()
                     viewModel.runAlarmClock()
                 }*/
                Log.e("frgmntSetSwitchStart", "isMyServiceRunning")
            } else {
                Log.e("frgmntSetSwitchStart", "isMyServiceRunningFalse")
                requireContext().stopService(MyForegroundService.newIntent(requireContext()))
                viewModel.cancelAlarmService()
            }
        }


        setOtherSettings()

        binding.frgmntSetMdEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                workingTimeModel = viewModel.getWorkingTime()
            }

            override fun afterTextChanged(s: Editable) {
                workingTimeModel = viewModel.getWorkingTime()
                if (s.length > 0) {
                    if (s.toString().toInt() >= 10) {
                        viewModel.setWorkingTime(
                            workingTimeModel.copy(
                                minDist = s.toString().toInt()
                            )
                        )
                        binding.frgmntSetMdEt.error = null
                    } else
                        binding.frgmntSetMdEt.error = "введите число больше 10"
                } else
                    binding.frgmntSetMdEt.error = "введите"
            }
        })
        binding.frgmntSetAccurEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                workingTimeModel = viewModel.getWorkingTime()
                if (s.length > 0) {
                    if (s.toString().toInt() >= 50) {
                        viewModel.setWorkingTime(
                            workingTimeModel.copy(
                                accuracy = s.toString().toInt()
                            )
                        )
                        binding.frgmntSetAccurEt.error = null
                    } else
                        binding.frgmntSetAccurEt.error = "введите число больше 50"
                } else
                    binding.frgmntSetAccurEt.error = "введите"
            }
        })
        binding.frgmntSetTimeAcEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                workingTimeModel = viewModel.getWorkingTime()
                if (s.length > 0) {
                    if (s.toString().toInt() >= 20) {
                        viewModel.setWorkingTime(
                            workingTimeModel.copy(
                                timeOfWaitAccuracy = s.toString().toInt()
                            )
                        )
                        binding.frgmntSetTimeAcEt.error = null
                    } else
                        binding.frgmntSetTimeAcEt.error = "введите число больше 20"
                } else
                    binding.frgmntSetTimeAcEt.error = "введите"

            }
        })
        binding.frgmntSetTimePovtorEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                workingTimeModel = viewModel.getWorkingTime()
                if (s.length > 0) {
                    if (s.toString().toInt() >= 15) {
                        viewModel.setWorkingTime(
                            workingTimeModel.copy(
                                timeOfWorkingWM = s.toString().toInt()
                            )
                        )
                        binding.frgmntSetTimePovtorEt.error = null
                    } else
                        binding.frgmntSetTimePovtorEt.error = "введите число больше 15"
                } else
                    binding.frgmntSetTimePovtorEt.error = "введите"
            }
        })


    }

    private fun setOtherSettings() {
        binding.frgmntSetAccurEt.setText(workingTimeModel.accuracy.toString())
        (binding.frgmntSetMdEt).setText(workingTimeModel.minDist.toString())
        (binding.frgmntSetTimeAcEt).setText(workingTimeModel.timeOfWaitAccuracy.toString())
        (binding.frgmntSetTimePovtorEt).setText(workingTimeModel.timeOfWorkingWM.toString())
    }


    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = (service as? MyForegroundService.LocalBinder) ?: return
            val foregroundService = binder.getService()
            foregroundService.isServiseAlive = { flag ->
                binding.frgmntSetSwitchStart.isChecked = flag //viewModel.сheckService()
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
        }
    }







    override fun onDestroy() {
        super.onDestroy()
        requireActivity().unbindService(serviceConnection)
    }

    private fun Boolean.toIntTxt() = if (this) "1" else "0"

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}