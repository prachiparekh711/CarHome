package ro.westaco.carhome.presentation.screens.settings.extras

import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_share_app.*
import ro.westaco.carhome.BuildConfig
import ro.westaco.carhome.R
import ro.westaco.carhome.presentation.base.BaseFragment

//C- Share Screen
@AndroidEntryPoint
class ShareTheAppFragment : BaseFragment<CommenViewModel>() {

    override fun getContentView() = R.layout.fragment_share_app

    override fun initUi() {
        back.setOnClickListener {
            viewModel.onBack()
        }

        home.setOnClickListener {
            viewModel.onMain()
        }
        txtShareapplink.text =
            getString(R.string.play_store_app_template, BuildConfig.APPLICATION_ID)
        btnShareLink.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.putExtra(
                Intent.EXTRA_TEXT,
                getString(R.string.play_store_app_template, BuildConfig.APPLICATION_ID)
            )
            intent.type = "text/plain"
            startActivity(Intent.createChooser(intent, "Share To:"))
        }
    }

    override fun setObservers() {
    }
}