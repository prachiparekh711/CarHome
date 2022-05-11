package ro.westaco.carhome.presentation.screens.dashboard.profile.close

import android.text.Editable
import android.text.TextWatcher
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_close_account.*
import ro.westaco.carhome.R
import ro.westaco.carhome.prefrences.SharedPrefrences
import ro.westaco.carhome.presentation.base.BaseFragment

@AndroidEntryPoint
class CloseAccountFragment : BaseFragment<CloseAccountViewModel>() {
    override fun getContentView() = R.layout.fragment_close_account

    override fun getStatusBarColor() = ContextCompat.getColor(requireContext(), R.color.white)

    override fun initUi() {
        back.setOnClickListener {
            viewModel.onBack()
        }

        home.setOnClickListener {
            viewModel.onMain()
        }

        root.setOnClickListener {
            viewModel.onRootClicked()
        }

        input.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.toString()?.let { viewModel.onConfirmationTextChanged(it) }
            }
        })

        cta.setOnClickListener {
            viewModel.onCloseAccount(input.text.toString())

        }
    }

    override fun setObservers() {
        viewModel.isDeletionConfirmedLiveData.observe(viewLifecycleOwner) { confirmed ->
            ViewCompat.setBackgroundTintList(
                cta,
                if (confirmed) {
                    null
                } else {
                    ContextCompat.getColorStateList(requireContext(), R.color.deleteDisabledRed)
                }
            )
        }

        viewModel.actionStream.observe(viewLifecycleOwner) {
            when (it) {

                is CloseAccountViewModel.ACTION.CloseAccount -> {
                    SharedPrefrences.setBiometricsSetup(requireContext(), false)
                }
            }
        }
    }
}