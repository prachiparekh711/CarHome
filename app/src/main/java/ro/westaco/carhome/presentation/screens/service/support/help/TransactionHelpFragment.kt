package ro.westaco.carhome.presentation.screens.service.support.help

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_insurance.back
import kotlinx.android.synthetic.main.fragment_transaction_help.*
import ro.westaco.carhome.R
import ro.westaco.carhome.presentation.base.BaseFragment

@AndroidEntryPoint
class TransactionHelpFragment : BaseFragment<TransactionHelpViewModel>() {

    override fun getContentView() = R.layout.fragment_transaction_help


    override fun initUi() {

        back.setOnClickListener { viewModel.onBack() }

        firstPhone.setOnClickListener {
            viewModel.openDialPad(getString(R.string.phone_number_demo_))
        }

        secondPhone.setOnClickListener {
            viewModel.openDialPad(getString(R.string.phone_number_second_demo_))
        }

        mainEmail.setOnClickListener {
            viewModel.openComposedMail(getString(R.string.demo_email_))
        }

    }

    override fun setObservers() {}

}