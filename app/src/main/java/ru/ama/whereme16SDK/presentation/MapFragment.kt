package ru.ama.whereme16SDK.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.webkit.*
import android.widget.PopupWindow
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import ru.ama.whereme16SDK.R
import ru.ama.whereme16SDK.databinding.DatePickerDaysBinding
import ru.ama.whereme16SDK.databinding.FragmentFirstBinding
import ru.ama.whereme16SDK.domain.entity.LocationDbByDays
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class MapFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding ?: throw RuntimeException("FragmentFirstBinding == null")
    private lateinit var viewModel: MapViewModel

    //lateinit var listDays: List<LocationDbByDays>
    private val component by lazy {
        (requireActivity().application as MyApp).component
    }
    var onDataSizeListener: ((Int) -> Unit)? = null

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override fun onAttach(context: Context) {
        component.inject(this)

        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }


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
                onDataSizeListener = {
                    if (it > 0) popupWindow.dismiss()
                }
            }
        }
        popupWindow.contentView = binding2.root
        popupWindow.showAsDropDown(anchor)

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity).supportActionBar?.subtitle = "Карта"
        viewModel = ViewModelProvider(this, viewModelFactory)[MapViewModel::class.java]
        /* viewModel.ld_days.observe(viewLifecycleOwner) {
             listDays = it
         }*/
        try {
            observeData(viewModel.getCurrentDate())
        } catch (e: Exception) {
        }
    }


    private fun observeData(abSuntitle: String) {
        viewModel.lldByDay?.observe(viewLifecycleOwner) {
            //onDataSizeListener?.invoke(it.size)

            (requireActivity() as AppCompatActivity).supportActionBar?.subtitle = abSuntitle
            /*   var mRes = "нет данных"
               if (it.isNotEmpty()) {
                   val postData = Gson().toJson(it)
                   var mTempRes = ""
                   var count = 0
                   for (mDat in it) {
                       mTempRes += "${++count}. ${mDat.datetime}  Ш: ${mDat.latitude} Д: ${mDat.longitude} Точность: ${mDat.accuracy} Инфо: ${mDat.info} Скорость: ${mDat.velocity} <br>"
                   }
                   mRes = mTempRes
                   (requireActivity() as AppCompatActivity).supportActionBar?.subtitle = abSuntitle
               }*/


            binding.frgmntMainTv.text =
                HtmlCompat.fromHtml(viewModel.d(it), HtmlCompat.FROM_HTML_MODE_LEGACY)
            Log.e("getLocationlldByDay", it.toString())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.lldByDay?.removeObservers(viewLifecycleOwner)
        _binding = null
    }
}