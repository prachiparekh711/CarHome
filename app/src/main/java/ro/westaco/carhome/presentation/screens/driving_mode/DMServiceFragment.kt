package ro.westaco.carhome.presentation.screens.driving_mode

import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_d_m_service.*
import ro.westaco.carhome.R
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.screens.home.HomeViewModel

@AndroidEntryPoint
class DMServiceFragment : BaseFragment<HomeViewModel>() {

    override fun getStatusBarColor() =
        ContextCompat.getColor(requireContext(), R.color.white)

    companion object {
        fun newInstance(): DMServiceFragment {
            return DMServiceFragment()
        }
    }

    override fun getContentView() = R.layout.fragment_d_m_service

    override fun initUi() {

        rovinieta.setOnClickListener {
            viewModel.onServiceClicked("RO_VIGNETTE")
        }

        bridge_tax.setOnClickListener {
            viewModel.onServiceClicked("RO_PASS_TAX")
        }

        cars.setOnClickListener {
            viewModel.onDataClicked(0)
        }

        person.setOnClickListener {
            viewModel.onDataClicked(1)
        }

        companies.setOnClickListener {
            viewModel.onDataClicked(2)
        }

        insurance.setOnClickListener {
            viewModel.onInsurance()
        }

    }

    override fun setObservers() {
    }
}