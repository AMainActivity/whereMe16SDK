package ru.ama.whereme16SDK.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ru.ama.whereme16SDK.R
import ru.ama.whereme16SDK.databinding.FragmentAboutBinding
import javax.inject.Inject

class AboutFragment : Fragment() {

    private var _binding: FragmentAboutBinding? = null
    private val binding
        get() = _binding ?: throw RuntimeException("FragmentAboutBinding == null")
    private lateinit var viewModel: AboutViewModel
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
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_about_fragment, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.menu_url -> {
                shereUrlAlertDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun sharetext(
        textZagol: String,
        textBody: String,
        isEmail: Boolean
    ) {
        val sharingIntent = Intent(Intent.ACTION_SEND)

        if (isEmail) {
            sharingIntent.putExtra(
                Intent.EXTRA_EMAIL,
                arrayOf(getString(R.string.frgmnt_menu_share_mail))
            )
            sharingIntent.type = SHARE_MAIL_TYPE
        } else
            sharingIntent.type = SHARE_TEXT_TYPE
        sharingIntent.putExtra(
            Intent.EXTRA_SUBJECT,
            textZagol
        )
        sharingIntent.putExtra(
            Intent.EXTRA_TEXT,
            textBody
        )
        val d = Intent.createChooser(
            sharingIntent,
            getString(R.string.frgmnt_menu_share_use)
        )
        requireActivity().startActivity(d)
    }

    private fun shereUrlAlertDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Важная информация")
            .setMessage(
                "Вы можете поделиться ссылкой для просмотра своих данных." +
                        "  \nДелитесь с сылкой с теми лицами, которым Вы доверяете просмотр своих данных. " +
                        "Перейдя по ссылке, эти лица могут контролировать как и текущие Ваши местоположения," +
                        "так и видеть Ваши местоположения за определенный день.\n" +
                        "Для запрета использования ссылки перейдите в telegram-bot и сформируйте новый код."
            )
            .setCancelable(true)
            .setPositiveButton("поделиться") { _, _ ->

                val res = viewModel.getSetUserInfo()
                if (res.name != null && res.url != null) sharetext(
                    res.name,
                    "https://kol.hhos.ru/gkk/map.php?wm=" + res.url,
                    false
                )
                else
                    Toast.makeText(requireContext(), "нет данных", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root

    }


    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity).supportActionBar?.subtitle = "О приложении"
        viewModel = ViewModelProvider(this, viewModelFactory)[AboutViewModel::class.java]

        binding.frgmntAbTv.linksClickable = true
        binding.frgmntAbTv.movementMethod = LinkMovementMethod.getInstance()
        binding.frgmntAbTv.text =
            HtmlCompat.fromHtml(
                getString(R.string.frgmnt_ab_main),
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )

    }

    override fun onDestroy() {
        super.onDestroy()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {

        private const val SHARE_MAIL_TYPE = "message/rfc822"
        private const val SHARE_TEXT_TYPE = "text/plain"
    }
}