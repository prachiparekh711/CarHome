package ro.westaco.carhome.presentation.screens.settings.extras

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_contact_us.back
import kotlinx.android.synthetic.main.fragment_contact_us.home
import kotlinx.android.synthetic.main.fragment_social.*
import ro.westaco.carhome.R
import ro.westaco.carhome.presentation.base.BaseFragment

//C- Social screen
@AndroidEntryPoint
class SocialFragment : BaseFragment<CommenViewModel>() {

    override fun getContentView() = R.layout.fragment_social

    override fun initUi() {
        back.setOnClickListener {
            viewModel.onBack()
        }

        home.setOnClickListener {
            viewModel.onMain()
        }

        mInsta.setOnClickListener {
            viewModel.onSocialClick(requireContext().getString(R.string.instagram_url))
        }

        mFb.setOnClickListener {
            viewModel.onSocialClick(requireContext().getString(R.string.facebook_url))
        }

        mTwitter.setOnClickListener {
            viewModel.onSocialClick(requireContext().getString(R.string.twitter_url))
        }

        mYoutube.setOnClickListener {
            viewModel.onSocialClick(requireContext().getString(R.string.youtube_url))
        }
    }

    override fun setObservers() {
    }
}