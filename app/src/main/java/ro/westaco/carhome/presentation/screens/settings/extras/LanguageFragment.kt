package ro.westaco.carhome.presentation.screens.settings.extras

import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_language.*
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.local.prefs.AppPreferencesDelegates
import ro.westaco.carhome.presentation.base.BaseFragment
import java.util.*


//C- Language Screen
@AndroidEntryPoint
class LanguageFragment : BaseFragment<CommenViewModel>() {

    companion object {
        const val TAG = "LanguageFragment"
    }

    var myLocale: Locale? = null
    private val appPreferences = AppPreferencesDelegates.get()
    var locale: String? = null

    override fun getContentView() = R.layout.fragment_language

    override fun getStatusBarColor() =
        ContextCompat.getColor(requireContext(), R.color.settingsHeaderBg)

    override fun initUi() {
        back.setOnClickListener {
            viewModel.onBack()
        }

        home.setOnClickListener {
            viewModel.onMain()
        }
        locale = appPreferences.language
        setLanguage()
        englishRL.setOnClickListener {
            locale = requireContext().resources.getString(R.string.english_lan)
            setLanguage()
        }

        romaniaRL.setOnClickListener {
            locale = requireContext().resources.getString(R.string.romania_lan)
            setLanguage()
        }

        cta.setOnClickListener {
            appPreferences.language = locale as String
            setAppLanguage()
            viewModel.onMain()
        }
    }

    private fun setLanguage() {

        if (locale == requireContext().resources.getString(R.string.english_lan)) {
            mSelection1.isVisible = true
            mSelection2.isVisible = false
        } else {
            mSelection1.isVisible = false
            mSelection2.isVisible = true
        }
    }

    override fun setObservers() {
    }

    private fun setAppLanguage() {
        val lang = if (AppPreferencesDelegates.get().language == "en-US") {
            "en"
        } else {
            "ro"
        }
        myLocale = Locale(lang)
        val res = resources
        val dm = res.displayMetrics
        val conf = res.configuration
        conf.locale = myLocale
        res.updateConfiguration(conf, dm)

    }

}