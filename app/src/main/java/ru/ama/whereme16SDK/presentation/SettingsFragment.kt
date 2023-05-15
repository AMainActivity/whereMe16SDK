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
import ru.ama.whereme16SDK.R
import ru.ama.whereme16SDK.databinding.FragmentSettingsBinding
import ru.ama.whereme16SDK.domain.entity.SettingsDomModel
import javax.inject.Inject


class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding
        get() = _binding ?: throw RuntimeException(getString(R.string.fragment_settings_binding_null))
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
                String.format(getString(R.string.set_format),10)
            } else {
                null
            }
            binding.frgmntSetMdEt.error = message
        }
        viewModel.errorAccuracy.observe(viewLifecycleOwner) {
            val message = if (it) {
                String.format(getString(R.string.set_format),50)
            } else {
                null
            }
            binding.frgmntSetAccurEt.error = message
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity).supportActionBar?.subtitle = getString(R.string.frgmnt_set_label)
        viewModel = ViewModelProvider(this, viewModelFactory)[SettingsViewModel::class.java]
        workingTimeModel = viewModel.getSettings()
        observeViewModel()
        binding.frgmntSetSwitchStart.isChecked = viewModel.checkService()
        binding.frgmntSetSwitchStart.setOnCheckedChangeListener{ view,isChecked->
            if (isChecked)
            {
                if (!viewModel.checkService()) {
                    ContextCompat.startForegroundService(
                        requireContext(),
                        MyForegroundService.newIntent(requireContext())
                    )
                    Log.e("frgmntSetSwitchStart", "isMyServiceRunning")
                }
            }
            else
            {
                if (viewModel.checkService()) {
                    Log.e("frgmntSetSwitchStart", "isMyServiceRunningFalse")
                    requireContext().stopService(MyForegroundService.newIntent(requireContext()))
                   // viewModel.cancelAlarmService()
                }
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


    }

    private fun setOtherSettings() {
        binding.frgmntSetAccurEt.setText(workingTimeModel.accuracy.toString())
        binding.frgmntSetMdEt.setText(workingTimeModel.minDist.toString())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}