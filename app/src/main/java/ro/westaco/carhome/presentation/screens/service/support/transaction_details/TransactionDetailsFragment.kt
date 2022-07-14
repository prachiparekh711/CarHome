package ro.westaco.carhome.presentation.screens.service.support.transaction_details

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.view.KeyEvent
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_transaction_details.*
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.PaymentResponse
import ro.westaco.carhome.di.ApiModule
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.screens.pdf_viewer.PdfActivity
import ro.westaco.carhome.utils.ViewUtils
import java.text.SimpleDateFormat
import java.util.*


@AndroidEntryPoint
class TransactionDetailsFragment : BaseFragment<TransactionDetailsViewModel>() {

    private var transactionGuid: String? = null

    var transactionOf: String? = null
    var fromHistory = false
    var statusColor: Int? = null

    companion object {
        const val ARG_TRANSACTION_GUID = "arg_transaction_guid"
        const val ARG_OF = "arg_of"
        const val ARG_HISTORY = "arg_history"
    }

    override fun getContentView() = R.layout.fragment_transaction_details

    override fun getStatusBarColor() =
        ContextCompat.getColor(requireContext(), statusColor ?: R.color.white)

    private fun onBackPress() {
        if (fromHistory)
            viewModel.onBack()
        else
            viewModel.onMain()
    }

    override fun initUi() {
        arguments?.let {
            transactionGuid = it.getString(ARG_TRANSACTION_GUID)
            transactionOf = it.getString(ARG_OF)
            fromHistory = it.getBoolean(ARG_HISTORY)

            transactionOf?.let { it1 ->
                viewModel.onTransactionGuid(
                    transactionGuid,
                    it1
                )
            }
        }

        requireView().isFocusableInTouchMode = true
        requireView().requestFocus()
        requireView().setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                onBackPress()
                true
            } else false
        }


        back.setOnClickListener {
            viewModel.onMain()
        }

        needHelp.setOnClickListener {
            viewModel.onHelpCenter()
        }

        getHelp.setOnClickListener {
            viewModel.onHelpCenter()
        }

    }

    override fun setObservers() {

        viewModel.transactionLiveData.observe(viewLifecycleOwner) { transaction ->

//                    if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {

            val spf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            transaction?.let {
                it.status?.let { it1 -> changeTheme(it1) }


                when (transactionOf) {
                    "RO_VIGNETTE" -> {
                        type.text =
                            requireContext().resources.getString(R.string.transaction_type_ro)
                        duration.text = it.durationDescription
                    }
                    "RO_PASS_TAX" -> {
                        type.text =
                            requireContext().resources.getString(R.string.transaction_type_br)
                        duration.text = it.quantityDescription
                    }
                    "RO_RCA" -> {
                        type.text =
                            requireContext().resources.getString(R.string.transaction_type_in)
                        duration.text = it.durationDescription
                    }
                }

                val dr = ApiModule.BASE_URL_RESOURCES + it.vehicleLogoHref
                val options = RequestOptions()
                logo.clipToOutline = true
                Glide.with(requireContext())
                    .load(dr)
                    .apply(
                        options.fitCenter()
                            .skipMemoryCache(true)
                            .priority(Priority.HIGH)
                            .format(DecodeFormat.PREFER_ARGB_8888)
                    )
                    .error(R.drawable.logo_small)
                    .into(logo)

                licensePlate.text =
                    it.vehicleLpn ?: requireContext().resources.getString(R.string.car_plate_)
                val plateStr = if (it.vehicleBrandName != null || it.vehicleModelName != null) {
                    "${it.vehicleBrandName ?: ""} ${it.vehicleModelName ?: ""}"
                } else {
                    requireContext().resources.getString(R.string.car_model)
                }
                makeAndModel.text = plateStr
                transactionId.text = it.transactionNo
                val newDate: Date = spf.parse(it.availabilityStartDate)
                val spf1 = SimpleDateFormat("dd MMM yyyy")
                startDate.text = spf1.format(newDate)


                price.text = "${it.price} ${it.currency}"
                totalPayment.text = "${it.price} ${it.currency}"

                if (it.ticket != null) {
                    documentTitle.text = it.ticket.name
                    val uploadDate: Date = spf.parse(it.ticket.uploadedDate)
                    val spf2 = SimpleDateFormat("dd MMM yyyy")
                    documentDate.text =
                        spf2.format(uploadDate)

                    if (it.ticket.name?.isNotEmpty() == true) {
                        documentGroup.visibility = View.VISIBLE
                    } else {
                        documentGroup.visibility = View.GONE
                    }

                    viewDoc.setOnClickListener {
                        val url = ApiModule.BASE_URL_RESOURCES + transaction.ticket?.href
                        val intent = Intent(requireContext(), PdfActivity::class.java)
                        intent.putExtra(PdfActivity.ARG_DATA, url)
                        intent.putExtra(PdfActivity.ARG_FROM, "DOCUMENT")
                        requireContext().startActivity(intent)
                    }
                }
            }
        }

        viewModel.initTransactionDataItems.observe(viewLifecycleOwner)
        { model ->
            if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {

                if (model != null) {
                    if (model.warnings.isNullOrEmpty()) {
                        model.html?.let {
                            showPurchaseBottomSheetDialog(
                                model
                            )
                        }
                    } else {
                        var warning = ""
                        for (i in model.warnings) {
                            warning += i + "\n"
                        }

                        showPurchaseWarningsDialog(
                            model, warning
                        )
                    }
                }
            }
        }
    }

    private fun changeTheme(status: Int) {
        when (status) {
            305 -> {
                statusColor = R.color.orangeWarning
                bgColor.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.orangeWarning
                    )
                )
                paymentStatusIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_payment_warn
                    )
                )
                paymentStatus.text =
                    getString(R.string.payment_pending)
                paymentStatus.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.orangeWarning
                    )
                )
                /*documentGroup.visibility = View.GONE*/
                activity?.window?.statusBarColor =
                    ContextCompat.getColor(requireContext(), R.color.orangeWarning)

                retry.isVisible = false
                needHelp.isVisible = true
                getHelp.isVisible = true
            }
            345, 350 -> {
                statusColor = R.color.greenActive
                bgColor.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.greenActive
                    )
                )
                paymentStatusIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_payment_success
                    )
                )
                paymentStatus.text =
                    getString(R.string.payment_success)
                paymentStatus.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.greenActive
                    )
                )
                /*documentGroup.visibility = View.VISIBLE*/
                activity?.window?.statusBarColor =
                    ContextCompat.getColor(requireContext(), R.color.greenActive)
                retry.isVisible = false
                needHelp.isVisible = false
                getHelp.isInvisible = true
            }
            346, 355 -> {
                statusColor = R.color.orangeExpired
                bgColor.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.orangeExpired
                    )
                )
                paymentStatusIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_payment_error
                    )
                )
                paymentStatus.text =
                    getString(R.string.payment_error)
                paymentStatus.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.orangeExpired
                    )
                )
                /*documentGroup.visibility = View.GONE*/
                activity?.window?.statusBarColor =
                    ContextCompat.getColor(requireContext(), R.color.orangeExpired)
                retry.isVisible = false
                needHelp.isVisible = true
                getHelp.isVisible = true
            }

        }
    }


    @SuppressLint("SetJavaScriptEnabled")
    private fun showPurchaseBottomSheetDialog(itemList: PaymentResponse) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_purchase)

        var navigated = false

        val webView = bottomSheetDialog.findViewById<WebView>(R.id.webView)
        val title = bottomSheetDialog.findViewById<TextView>(R.id.title)
        title?.text = getString(R.string.netopia_secure)
        webView?.clearHistory()

        webView?.settings?.javaScriptEnabled = true
        webView?.settings?.javaScriptCanOpenWindowsAutomatically = true
        webView?.settings?.loadsImagesAutomatically = true

        webView?.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(webView: WebView, url: String): Boolean {
                return shouldOverrideUrlLoading(url)
            }

            @TargetApi(Build.VERSION_CODES.N)
            override fun shouldOverrideUrlLoading(
                webView: WebView,
                request: WebResourceRequest,
            ): Boolean {
                val uri = request.url
                return shouldOverrideUrlLoading(uri.toString())
            }

            private fun shouldOverrideUrlLoading(url: String): Boolean {
                return if (url.contains("payment-done")) {
                    if (!navigated) {
                        navigated = true
                        transactionOf?.let { it1 ->
                            viewModel.onTransactionGuid(
                                transactionGuid,
                                it1
                            )
                        }
//                        viewModel.onPaymentSuccessful(itemList)

                    }
                    bottomSheetDialog.dismiss()
                    true
                } else {
                    false
                }
            }

        }

        itemList.html?.replace("&#39;", "")
            ?.let { webView?.loadData(it, "text/html; charset=UTF-8", null) }

        bottomSheetDialog.findViewById<View>(R.id.dismiss)?.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        val parentLayout =
            bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        parentLayout?.let { it ->
            val behaviour = BottomSheetBehavior.from(it)
            ViewUtils.setViewHeightAsWindowPercent(requireContext(), it, 85)
            behaviour.state = BottomSheetBehavior.STATE_EXPANDED
        }

        bottomSheetDialog.show()

    }

    private var purchaseDialog: AlertDialog? = null
    private fun showPurchaseWarningsDialog(model: PaymentResponse, warning: String) {
        purchaseDialog?.dismiss()
        purchaseDialog = AlertDialog.Builder(requireContext(), R.style.DialogTheme).create()
        purchaseDialog?.setMessage(warning)
        purchaseDialog?.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        purchaseDialog?.setButton(AlertDialog.BUTTON_POSITIVE, "OK") { dialog, _ ->
            dialog.dismiss()
            showPurchaseBottomSheetDialog(model)
        }
        purchaseDialog?.show()
    }

}