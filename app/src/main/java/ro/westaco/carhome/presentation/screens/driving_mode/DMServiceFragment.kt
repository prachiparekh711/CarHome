package ro.westaco.carhome.presentation.screens.driving_mode

import android.app.Dialog
import android.view.Window
import android.widget.TextView
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_d_m_service.*
import ro.westaco.carhome.R
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.screens.home.HomeViewModel
import ro.westaco.carhome.presentation.screens.main.MainActivity

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
            viewModel.onRoadTax()
        }

        bridge_tax.setOnClickListener {
            viewModel.onRoadTax()
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
            showDialog()
        }

        rovinieta.setOnClickListener {
            viewModel.onRoadTax()
        }

        bridge_tax.setOnClickListener {
            viewModel.onRoadTax()
        }

    }

    private fun showDialog() {
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.info_layout)
        val mOK = dialog.findViewById(R.id.mOK) as TextView
        val mText = dialog.findViewById(R.id.mText) as TextView
        mText.text = requireContext().resources.getString(
            R.string.insurance_info,
            MainActivity.activeUser
        )

        mOK.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()

    }

    override fun setObservers() {
    }
}