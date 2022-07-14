package ro.westaco.carhome.presentation.screens.service.insurance.request

import android.webkit.WebView
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_ins_acceptance_request.*
import kotlinx.android.synthetic.main.layout_bottom_sheet.*
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.TermsResponseItem
import ro.westaco.carhome.data.sources.remote.responses.models.Vehicle
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.screens.auth.TermsAdapter
import ro.westaco.carhome.views.Progressbar

@AndroidEntryPoint
class InsAcceptanceRequestFragment : BaseFragment<InsAcceptanceRequestModel>(),
    TermsAdapter.OnTermsClickListner {

    var progressbar: Progressbar? = null
    override fun getContentView() = R.layout.fragment_ins_acceptance_request
    var vehicleItem: Vehicle? = null
    var termsAdapter: TermsAdapter? = null

    companion object {
        const val ARG_CAR = "arg_car"
    }

    override fun initUi() {
        arguments?.let {
            vehicleItem = it.getSerializable(ARG_CAR) as? Vehicle?
        }

        progressbar = Progressbar(requireContext())

        back.setOnClickListener {
            viewModel.onBack()
        }

        home.setOnClickListener {
            viewModel.onMain()
        }

        mContinue.setOnClickListener {
            progressbar?.showPopup()

            val termsList = termsAdapter?.getItems()
            if (termsList.isNullOrEmpty()) {
                letsBegin()
            } else {
                viewModel.saveTerms(termsAdapter?.getItems())
            }
        }
    }

    private fun letsBegin() {
        progressbar?.dismissPopup()
        if (vehicleItem != null)
            viewModel.identifyVehicle(vehicleItem)
        else
            viewModel.onStart()
    }

    override fun setObservers() {
        viewModel.termsLiveData.observe(viewLifecycleOwner) { termsList ->
            if (!termsList.isNullOrEmpty()) {
                termsAdapter = TermsAdapter(requireContext(), termsList, this)
                termsRV.layoutManager =
                    LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
                termsRV.adapter = termsAdapter
                termsRV.isVisible = true
            }
        }

        viewModel.actionStream.observe(viewLifecycleOwner) {
            when (it) {
                is InsAcceptanceRequestModel.ACTION.TermsSuccess -> {
                    letsBegin()
                }
                is InsAcceptanceRequestModel.ACTION.GetTerms -> {
                    if (it.termRequired) {
                        mContinue.alpha = 0.4F
                        mContinue.isEnabled = false
                    } else {
                        mContinue.alpha = 1F
                        mContinue.isEnabled = true
                    }
                    progressbar?.dismissPopup()
                }
            }
        }
    }

    override fun onTermsClick(item: TermsResponseItem) {
        showBottomSheetDialog(item)
    }

    override fun onChecked() {
        checkTerms()
    }

    private fun checkTerms() {
        val termsList = termsAdapter?.getItems()
        if (termsList != null) {
            for (i in termsList.indices) {
                if (termsList[i].mandatory == true) {
                    if (!termsList[i].allowed) {
                        mContinue.alpha = 0.4F
                        mContinue.isEnabled = false
                    } else {
                        mContinue.alpha = 1F
                        mContinue.isEnabled = true
                    }
                }
            }
        }
    }

    private fun showBottomSheetDialog(
        item: TermsResponseItem
    ) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(R.layout.insurance_request_layout)

        bottomSheetDialog.findViewById<TextView>(R.id.title)?.text = item.title
        val webSettings = bottomSheetDialog.webView.settings
        webSettings.javaScriptEnabled = true

        bottomSheetDialog.webView.loadUrl("https://carhome-build.westaco.com/carhome/rest/public/terms/" + item.versionId)
        WebView.setWebContentsDebuggingEnabled(false)
        bottomSheetDialog.findViewById<ImageView>(R.id.dismiss)?.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.findViewById<TextView>(R.id.cancel)?.setOnClickListener {
            item.allowed = false
            checkTerms()
            termsAdapter?.notifyDataSetChanged()
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.findViewById<TextView>(R.id.mAccept)?.setOnClickListener {
            item.allowed = true
            checkTerms()
            termsAdapter?.notifyDataSetChanged()
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

}