package ro.westaco.carhome.presentation.screens.settings.security

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_password_change_success.*
import ro.westaco.carhome.R
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.screens.settings.extras.CommenViewModel

@AndroidEntryPoint
class PasswordChangeSuccessFragment : BaseFragment<CommenViewModel>() {


    override fun getContentView() = R.layout.fragment_password_change_success

    override fun initUi() {

        backToLogin.setOnClickListener {

        }
    }

    override fun setObservers() {
    }

}