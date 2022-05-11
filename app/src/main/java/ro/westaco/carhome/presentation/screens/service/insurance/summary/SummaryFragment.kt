package ro.westaco.carhome.presentation.screens.service.insurance.summary

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.AlertDialog
import android.os.Build
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_summary.*
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.local.prefs.AppPreferencesDelegates
import ro.westaco.carhome.data.sources.remote.responses.models.*
import ro.westaco.carhome.di.ApiModule
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.screens.service.insurance.SelectUserFragment
import ro.westaco.carhome.utils.ViewUtils

@AndroidEntryPoint
class SummaryFragment : BaseFragment<SummaryViewModel>(),
    SelectUserFragment.OnOwnerSelectionListener,
    SelectUserFragment.AddNewUserView {

    var rcaOfferDetail: RcaOfferDetails? = null
    var ds = false
    var dsList = false
    private lateinit var adapterDriver: DriverAdapter
    lateinit var invoicePersonGuid: String

    companion object {
        const val ARG_OFFERDETAIL = "arg_offerdetail"
        const val ARG_DS = "arg_ds"
    }

    override fun getContentView(): Int {
        return R.layout.fragment_summary
    }

    @SuppressLint("SetTextI18n")
    override fun initUi() {
        arguments?.let {

            rcaOfferDetail = it.getSerializable(ARG_OFFERDETAIL) as? RcaOfferDetails?

            ds = it.getBoolean(ARG_DS)

            rcaOfferDetail?.vehicle?.brandLogo?.let { it1 -> setImage(it1, carLogo) }


            carName.text = rcaOfferDetail?.vehicle?.brandName
            et_start_date.setText(rcaOfferDetail?.beginDate)

            if (rcaOfferDetail?.leasing == true) {
                invoicePersonGuid = if (rcaOfferDetail?.vehicleUserLegalPerson != null) {
                    mOwnerName.setText(rcaOfferDetail?.vehicleUserLegalPerson?.companyName)
                    mBillToName.setText(rcaOfferDetail?.vehicleUserLegalPerson?.companyName)
                    rcaOfferDetail?.vehicleUserLegalPerson?.logoHref?.let { it1 ->
                        setImage(
                            it1,
                            mSelectOwnerImage
                        )
                    }
                    rcaOfferDetail?.vehicleUserLegalPerson?.logoHref?.let { it1 ->
                        setImage(
                            it1,
                            mSelect_bill_Image
                        )
                    }
                    rcaOfferDetail?.vehicleUserLegalPerson?.guid.toString()
                } else {
                    mOwnerName.setText("${rcaOfferDetail?.vehicleUserNaturalPerson?.firstName} ${rcaOfferDetail?.vehicleUserNaturalPerson?.lastName}")
                    mBillToName.setText("${rcaOfferDetail?.vehicleUserNaturalPerson?.firstName} ${rcaOfferDetail?.vehicleUserNaturalPerson?.lastName}")
                    rcaOfferDetail?.vehicleUserNaturalPerson?.logoHref?.let { it1 ->
                        setImage(
                            it1,
                            mSelectOwnerImage
                        )
                    }
                    rcaOfferDetail?.vehicleUserNaturalPerson?.logoHref?.let { it1 ->
                        setImage(
                            it1,
                            mSelect_bill_Image
                        )
                    }
                    rcaOfferDetail?.vehicleUserNaturalPerson?.guid.toString()
                }
            } else {
                invoicePersonGuid = if (rcaOfferDetail?.vehicleOwnerLegalPerson != null) {
                    mOwnerName.setText(rcaOfferDetail?.vehicleOwnerLegalPerson?.companyName)
                    mBillToName.setText(rcaOfferDetail?.vehicleOwnerLegalPerson?.companyName)
                    rcaOfferDetail?.vehicleOwnerLegalPerson?.logoHref?.let { it1 ->
                        setImage(
                            it1,
                            mSelectOwnerImage
                        )
                    }
                    rcaOfferDetail?.vehicleOwnerLegalPerson?.logoHref?.let { it1 ->
                        setImage(
                            it1,
                            mSelect_bill_Image
                        )
                    }
                    rcaOfferDetail?.vehicleOwnerLegalPerson?.guid.toString()
                } else {
                    mOwnerName.setText("${rcaOfferDetail?.vehicleOwnerNaturalPerson?.firstName} ${rcaOfferDetail?.vehicleOwnerNaturalPerson?.lastName}")
                    mBillToName.setText("${rcaOfferDetail?.vehicleOwnerNaturalPerson?.firstName} ${rcaOfferDetail?.vehicleOwnerNaturalPerson?.lastName}")
                    rcaOfferDetail?.vehicleOwnerNaturalPerson?.logoHref?.let { it1 ->
                        setImage(
                            it1,
                            mSelectOwnerImage
                        )
                    }
                    rcaOfferDetail?.vehicleOwnerNaturalPerson?.logoHref?.let { it1 ->
                        setImage(
                            it1,
                            mSelect_bill_Image
                        )
                    }
                    rcaOfferDetail?.vehicleOwnerNaturalPerson?.guid.toString()
                }
            }

            mRecycle.layoutManager = LinearLayoutManager(context)
            adapterDriver = DriverAdapter(
                requireActivity(),
                rcaOfferDetail?.drivers as ArrayList<NaturalPersonDetails>
            )
            mRecycle.adapter = adapterDriver

            usageType.setText(rcaOfferDetail?.vehicle?.vehicleUsageTypeName)

            mChange.setOnClickListener {
                val bottomSheet = SelectUserFragment(this, null, null, this, null, "OWNER")
                bottomSheet.show(requireActivity().supportFragmentManager, null)
            }

            rcaOfferDetail?.offer?.insurerLogoHref?.let { it1 -> setImage(it1, insurerImage) }

            mOfferTitle.text = rcaOfferDetail?.offer?.insurerNameLong
            description.text = rcaOfferDetail?.offer?.description


            if (ds) {
                typeTitle.text = requireContext().resources.getString(R.string.rca_plus_ds)
                price.text =
                    "${rcaOfferDetail?.offer?.priceDs ?: ""} ${rcaOfferDetail?.offer?.currency}"
                mRcaPrice.text =
                    "${rcaOfferDetail?.offer?.priceDs ?: ""} ${rcaOfferDetail?.offer?.currency}"
                dsList = false
            } else {
                typeTitle.text = requireContext().resources.getString(R.string.rca)
                price.text =
                    "${rcaOfferDetail?.offer?.price ?: ""} ${rcaOfferDetail?.offer?.currency}"
                mRcaPrice.text =
                    "${rcaOfferDetail?.offer?.price ?: ""} ${rcaOfferDetail?.offer?.currency}"
                dsList = true
            }

            mBeginDate.text = rcaOfferDetail?.beginDate
            mEndDate.text = rcaOfferDetail?.endDate
        }

        toolbar.setNavigationOnClickListener {
            viewModel.onBack()
        }

        cta.setOnClickListener {

            rcaOfferDetail?.offer?.let { it1 -> viewModel.onCtaItems(it1, check.isChecked, dsList) }

        }

        cancel.setOnClickListener { viewModel.onBack() }
    }

    override fun setObservers() {

        viewModel.durationData.observe(viewLifecycleOwner)
        { durationList ->
            val pos = rcaOfferDetail?.rcaDurationId?.let { findPosById(durationList, it) }
            if (pos != null) {
                mDuration.text = durationList[pos].name
                et_end_date.setText(durationList[pos].name)
            }
        }

        viewModel.rcaInitData.observe(viewLifecycleOwner)
        { offerList ->
            offerList.guid?.let { viewModel.paymentStart(it, invoicePersonGuid) }
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
                    }
                } else {
                    var warning = ""
                    if (model?.warnings != null) {
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


    private fun findPosById(list: List<RcaDurationItem>?, id: Int): Int {

        if (list.isNullOrEmpty()) return -1
        for (i in list.withIndex()) {
            if (i.value.id == id) {
                return i.index
            }
        }
        return -1
    }

    private fun setImage(href: String, view: ImageView) {
        val url = "${ApiModule.BASE_URL_RESOURCES}${href}"
        val glideUrl = GlideUrl(
            url,
            LazyHeaders.Builder()
                .addHeader("Authorization", "Bearer ${AppPreferencesDelegates.get().token}")
                .build()
        )
        val options = RequestOptions()
        view.clipToOutline = true
        Glide.with(requireContext())
            .load(glideUrl)
            .error(requireContext().resources.getDrawable(R.drawable.ic_profile_picture))
            .apply(
                options.centerCrop()
                    .skipMemoryCache(true)
                    .priority(Priority.HIGH)
                    .format(DecodeFormat.PREFER_ARGB_8888)
            )
            .into(view)
    }

    override fun onContinueOwner(ownerNaturalItem: NaturalPerson?, ownerLegalItem: LegalPerson?) {
        when {
            ownerNaturalItem != null -> {
                ownerNaturalItem.logoHref?.let { it1 -> setImage(it1, mSelect_bill_Image) }
                mBillToName.setText("${ownerNaturalItem.firstName} ${ownerNaturalItem.lastName}")
                invoicePersonGuid = ownerNaturalItem.guid.toString()
            }
            ownerLegalItem != null -> {
                ownerLegalItem.logoHref?.let { it1 -> setImage(it1, mSelect_bill_Image) }
                mBillToName.setText(ownerLegalItem.companyName)
                invoicePersonGuid = ownerLegalItem.guid.toString()
            }
        }
    }

    override fun openNewUser(type: String?) {

    }

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

                        viewModel.onPaymentSuccessful(itemList)

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

}