package ru.ama.whereme16SDK.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.PopupWindow
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ru.ama.whereme16SDK.R
import ru.ama.whereme16SDK.databinding.DatePickerDaysBinding
import ru.ama.whereme16SDK.databinding.FragmentFirstBinding
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class MapFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding
        get() = _binding ?: throw RuntimeException(getString(R.string.fragment_first_binding_null))
    private lateinit var viewModel: MapViewModel
    private val component by lazy {
        (requireActivity().application as MyApp).component
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override fun onAttach(context: Context) {
        component.inject(this)

        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setHasOptionsMenu(true)
    }

/*
    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_map_fragment, menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_day_picker -> {
                showPopupDatePicker(requireActivity().findViewById(R.id.menu_day_picker))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun showPopupDatePicker(anchor: View) {
        val popupWindow = PopupWindow(requireContext())
        popupWindow.setBackgroundDrawable(
            ResourcesCompat.getDrawable(
                getResources(),
                R.drawable.nulldr,
                null
            )
        )
        popupWindow.isFocusable = true
        popupWindow.width = WindowManager.LayoutParams.WRAP_CONTENT
        popupWindow.height = WindowManager.LayoutParams.WRAP_CONTENT
        val binding2 = DatePickerDaysBinding.inflate(layoutInflater)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            binding2.frgmntMapDp.setOnDateChangedListener { datePicker, year, monthOfYear, dayOfMonth ->
                val formatter = SimpleDateFormat("dd.MM.yyyy")
                val calendar: Calendar = Calendar.getInstance()
                calendar.set(year, monthOfYear, dayOfMonth)
                val s = formatter.format(calendar.getTime())
                viewModel.getDataByDate(s)
                observeData(s)
                popupWindow.dismiss()
            }
        }
        popupWindow.contentView = binding2.root
        popupWindow.showAsDropDown(anchor)
    }*/

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity).supportActionBar?.subtitle =
            getString(R.string.first_fragment_label)
        viewModel = ViewModelProvider(this, viewModelFactory)[MapViewModel::class.java]
        try {
            observeData(viewModel.getCurrentDate())
        } catch (_: Exception) {
        }
    }


    private fun observeData(abSuntitle: String) {
        viewModel.resList?.observe(viewLifecycleOwner) {
            (requireActivity() as AppCompatActivity).supportActionBar?.subtitle = abSuntitle
            binding.frgmntMainTv.text =
                HtmlCompat.fromHtml(viewModel.d(it), HtmlCompat.FROM_HTML_MODE_LEGACY)
            //   binding.frgmntMapSv.post { binding.frgmntMapSv.fullScroll(ScrollView.FOCUS_DOWN) }
           //Log.e("getLocationlldByDay", it.toString())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.resList?.removeObservers(viewLifecycleOwner)
        _binding = null
    }
}