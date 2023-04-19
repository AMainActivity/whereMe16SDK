package ru.ama.whereme16SDK.presentation

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ru.ama.whereme16SDK.databinding.FragmentSettingsBinding
import ru.ama.whereme16SDK.domain.entity.SettingsDomModel
import javax.inject.Inject


class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding
        get() = _binding ?: throw RuntimeException("FragmentSettingsBinding == null")
    private lateinit var viewModel: SettingsViewModel
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root

    }


    private fun observeViewModel() {
        viewModel.errorMinDistance.observe(viewLifecycleOwner) {
            val message = if (it) {
                "введите число больше 10"
            } else {
                null
            }
            binding.frgmntSetMdEt.error = message
        }
        viewModel.errorAccuracy.observe(viewLifecycleOwner) {
            val message = if (it) {
                "введите число больше 50"
            } else {
                null
            }
            binding.frgmntSetAccurEt.error = message
        }
        viewModel.errorTimeAccuracy.observe(viewLifecycleOwner) {
            val message = if (it) {
                "введите число больше 20"
            } else {
                null
            }
            binding.frgmntSetTimeAcEt.error = message
        }
        viewModel.errorTimePeriod.observe(viewLifecycleOwner) {
            val message = if (it) {
                "введите число больше 15"
            } else {
                null
            }
            binding.frgmntSetTimePovtorEt.error = message
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity).supportActionBar?.subtitle = "Настройки"
        viewModel = ViewModelProvider(this, viewModelFactory)[SettingsViewModel::class.java]
        workingTimeModel = viewModel.getSettings()
        observeViewModel()
        binding.frgmntSetSwitchStart.isChecked = viewModel.checkService()
        binding.frgmntSetSwitchStart.setOnClickListener { view ->
            if (!viewModel.checkService()) {
                ContextCompat.startForegroundService(
                    requireContext(),
                    MyForegroundService.newIntent(requireContext())
                )
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
                viewModel.resetError(SettingsViewNames.MIN_DISTANCE)
            }

            override fun afterTextChanged(s: Editable) {
                viewModel.validateInputData(s.toString(), SettingsViewNames.MIN_DISTANCE)
            }
        })
        binding.frgmntSetAccurEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                viewModel.resetError(SettingsViewNames.ACCURACY)
            }

            override fun afterTextChanged(s: Editable) {
                viewModel.validateInputData(s.toString(), SettingsViewNames.ACCURACY)
            }
        })
        binding.frgmntSetTimeAcEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                viewModel.resetError(SettingsViewNames.TIME_ACCURACY)
            }

            override fun afterTextChanged(s: Editable) {
                viewModel.validateInputData(s.toString(), SettingsViewNames.TIME_ACCURACY)
            }
        })
        binding.frgmntSetTimePovtorEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                viewModel.resetError(SettingsViewNames.TIME_PERIOD)
            }

            override fun afterTextChanged(s: Editable) {
                viewModel.validateInputData(s.toString(), SettingsViewNames.TIME_PERIOD)
            }
        })


    }

    private fun setOtherSettings() {
        binding.frgmntSetAccurEt.setText(workingTimeModel.accuracy.toString())
        binding.frgmntSetMdEt.setText(workingTimeModel.minDist.toString())
        binding.frgmntSetTimeAcEt.setText(workingTimeModel.timeOfWaitAccuracy.toString())
        binding.frgmntSetTimePovtorEt.setText(workingTimeModel.timeOfWorkingWM.toString())
    }


    override fun onDestroy() {
        super.onDestroy()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}