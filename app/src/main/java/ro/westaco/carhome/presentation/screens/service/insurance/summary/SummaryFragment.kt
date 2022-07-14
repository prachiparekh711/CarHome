package ro.westaco.carhome.presentation.screens.service.insurance.summary

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.AlertDialog
import android.os.Build
import android.view.KeyEvent
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
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
import ro.westaco.carhome.dialog.DialogUtils
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.screens.main.MainActivity.Companion.profileItem
import ro.westaco.carhome.presentation.screens.service.insurance.adapter.SummaryDriverAdapter
import ro.westaco.carhome.presentation.screens.service.insurance.init.InsuranceFragment
import ro.westaco.carhome.presentation.screens.service.insurance.ins_person.SelectUserFragment
import ro.westaco.carhome.utils.ViewUtils
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class SummaryFragment : BaseFragment<SummaryViewModel>(),
    SelectUserFragment.OnOwnerSelectionListener,
    SelectUserFragment.AddNewUserView {

    var rcaOfferDetail: RcaOfferDetails? = null
    var ds = false
    private lateinit var adapterDriver: SummaryDriverAdapter
    lateinit var invoicePersonGuid: String
    var bottomSheet: SelectUserFragment? = null
    var firstPaymentDone: Boolean = false
    var rcaInitResponse: RcaInitResponse? = null

    companion object {
        const val ARG_OFFERDETAIL = "arg_offerdetail"
        const val ARG_DS = "arg_ds"
        var addNew = false
    }

    override fun getContentView(): Int {
        return R.layout.fragment_summary
    }

    @SuppressLint("SetTextI18n")
    override fun initUi() {
        InsuranceFragment.IS_PERSON_EDITABLE = false
        requireView().isFocusableInTouchMode = true
        requireView().requestFocus()
        requireView().setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                addNew = false
                viewModel.onBack()
                true
            } else false
        }

        arguments?.let {

            rcaOfferDetail = it.getSerializable(ARG_OFFERDETAIL) as? RcaOfferDetails?

            ds = it.getBoolean(ARG_DS)

            rcaOfferDetail?.vehicle?.brandLogo?.let { it1 ->
                setImage(
                    logoHref = it1,
                    view = carLogo,
                    tv = null,
                    singlechar = null
                )
            }

            carName.text = rcaOfferDetail?.vehicle?.brandName
            et_start_date.setText(viewModel.convertFromServerDate(rcaOfferDetail?.beginDate))

            if (rcaOfferDetail?.leasing == true) {
                invoicePersonGuid = if (rcaOfferDetail?.vehicleUserLegalPerson != null) {
                    mOwnerName.setText(rcaOfferDetail?.vehicleUserLegalPerson?.companyName)

                    setImage(
                        rcaOfferDetail?.vehicleUserLegalPerson?.logoHref,
                        "${rcaOfferDetail?.vehicleUserLegalPerson?.companyName}",
                        mSelectOwnerImage,
                        textLogoOwner
                    )

                    rcaOfferDetail?.vehicleUserLegalPerson?.guid.toString()
                } else {
                    mOwnerName.setText("${rcaOfferDetail?.vehicleUserNaturalPerson?.firstName} ${rcaOfferDetail?.vehicleUserNaturalPerson?.lastName}")

                    setImage(
                        rcaOfferDetail?.vehicleUserNaturalPerson?.logoHref,
                        "${rcaOfferDetail?.vehicleUserNaturalPerson?.firstName} ${rcaOfferDetail?.vehicleUserNaturalPerson?.lastName}",
                        mSelectOwnerImage,
                        textLogoOwner
                    )
                    rcaOfferDetail?.vehicleUserNaturalPerson?.guid.toString()
                }
            } else {
                invoicePersonGuid = if (rcaOfferDetail?.vehicleOwnerLegalPerson != null) {
                    mOwnerName.setText(
                        rcaOfferDetail?.vehicleOwnerLegalPerson?.companyName.toString()
                            .ifBlank { "" })

                    setImage(
                        rcaOfferDetail?.vehicleOwnerLegalPerson?.logoHref,
                        "${rcaOfferDetail?.vehicleOwnerLegalPerson?.companyName}",
                        mSelectOwnerImage,
                        textLogoOwner
                    )

                    rcaOfferDetail?.vehicleOwnerLegalPerson?.guid.toString()
                } else {
                    mOwnerName.setText("${
                        rcaOfferDetail?.vehicleOwnerNaturalPerson?.firstName.toString()
                            .ifBlank { "" }
                    } " +
                            rcaOfferDetail?.vehicleOwnerNaturalPerson?.lastName.toString()
                                .ifBlank { "" })


                    setImage(
                        rcaOfferDetail?.vehicleOwnerNaturalPerson?.logoHref,
                        "${rcaOfferDetail?.vehicleOwnerNaturalPerson?.firstName} ${rcaOfferDetail?.vehicleOwnerNaturalPerson?.lastName}",
                        mSelectOwnerImage,
                        textLogoOwner
                    )

                    rcaOfferDetail?.vehicleOwnerNaturalPerson?.guid.toString()
                }
            }

            mBillToName.setText(
                "${
                    profileItem?.firstName.toString().ifBlank { "N/A" }
                } ${profileItem?.lastName.toString().ifBlank { "N/A" }}"
            )

            setImage(
                profileItem?.logoHref,
                "${
                    profileItem?.firstName.toString()
                } ${profileItem?.lastName.toString()}",
                mSelect_bill_Image,
                textLogoBill
            )

            profileItem?.id?.toLong()?.let { it1 -> viewModel.getNaturalPerson(it1) }

            mRecycle.layoutManager = LinearLayoutManager(context)
            adapterDriver = SummaryDriverAdapter(
                requireActivity(),
                rcaOfferDetail?.drivers as ArrayList<NaturalPersonDetails>
            )
            mRecycle.adapter = adapterDriver

            usageType.setText(rcaOfferDetail?.vehicle?.vehicleUsageTypeName)

            mChange.setOnClickListener {
                bottomSheet = SelectUserFragment(this, null, null, this, null, "OWNER")
                bottomSheet?.show(requireActivity().supportFragmentManager, null)
            }

            rcaOfferDetail?.offer?.insurerLogoHref?.let { it1 ->
                setImage(
                    it1,
                    null,
                    insurerImage,
                    null
                )
            }

            mOfferTitle.text = rcaOfferDetail?.offer?.insurerNameLong
            description.text = rcaOfferDetail?.offer?.description

            if (ds) {
                typeTitle.text = requireContext().resources.getString(R.string.rca_plus_ds)
                price.text =
                    "${rcaOfferDetail?.offer?.priceDs ?: ""} ${rcaOfferDetail?.offer?.currency}"
                mRcaPrice.text =
                    "${rcaOfferDetail?.offer?.priceDs ?: ""} ${rcaOfferDetail?.offer?.currency}"
            } else {
                typeTitle.text = requireContext().resources.getString(R.string.rca)
                price.text =
                    "${rcaOfferDetail?.offer?.price ?: ""} ${rcaOfferDetail?.offer?.currency}"
                mRcaPrice.text =
                    "${rcaOfferDetail?.offer?.price ?: ""} ${rcaOfferDetail?.offer?.currency}"
            }

            val spf = SimpleDateFormat("yyyy-MM-dd")
            val mBeginStr: Date = spf.parse(rcaOfferDetail?.beginDate)
            val mEndStr: Date = spf.parse(rcaOfferDetail?.endDate)
            val spf1 = SimpleDateFormat("dd MMM, yyyy")
            mBeginDate.text = spf1.format(mBeginStr)
            mEndDate.text = spf1.format(mEndStr)
        }

        toolbar.setNavigationOnClickListener {
            addNew = false
            viewModel.onBack()
        }


        cta.setOnClickListener {


            if (check.isChecked) {
                addNew = false

                if (firstPaymentDone) {
                    rcaInitResponse?.guid?.let {
                        viewModel.paymentRetry(it)
                    }
                } else {
                    rcaOfferDetail?.offer?.let { it1 ->
                        viewModel.onCtaItems(
                            it1,
                            ds
                        )
                    }
                }
            } else {
                DialogUtils.showErrorInfo(requireContext(), getString(R.string.check_info))
            }
        }

        cancel.setOnClickListener {
            addNew = false
            viewModel.onBack()
        }
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
        { rcaInitresponse ->

            this.rcaInitResponse = rcaInitresponse
            rcaInitresponse.guid?.let {
                firstPaymentDone = true
                viewModel.paymentStart(it, invoicePersonGuid)
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

        viewModel.profileLogoData?.observe(viewLifecycleOwner) { profileLogo ->
            if (profileLogo != null) {

                val options = RequestOptions()
                mSelect_bill_Image.clipToOutline = true
                Glide.with(requireContext())
                    .load(profileLogo)
                    .apply(
                        options.fitCenter()
                            .skipMemoryCache(true)
                            .priority(Priority.HIGH)
                            .format(DecodeFormat.PREFER_ARGB_8888)
                    )
                    .into(mSelect_bill_Image)
            } else {
                viewModel.userLiveData.observe(viewLifecycleOwner) { user ->
                    if (user != null) {
                        val imageUrl = viewModel.getProfileImage(requireContext(), user)

                        val options = RequestOptions()
                        mSelect_bill_Image.clipToOutline = true
                        context?.let {
                            Glide.with(it)
                                .load(imageUrl)
                                .apply(
                                    options.fitCenter()
                                        .skipMemoryCache(true)
                                        .priority(Priority.HIGH)
                                        .format(DecodeFormat.PREFER_ARGB_8888)
                                )
                                .into(mSelect_bill_Image)

                        }
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

    private fun setImage(logoHref: String?, singlechar: String?, view: ImageView, tv: TextView?) {

        if (logoHref != null) {

            val url = "${ApiModule.BASE_URL_RESOURCES}${logoHref}"
            val glideUrl = GlideUrl(
                url,
                LazyHeaders.Builder()
                    .addHeader("Authorization", "Bearer ${AppPreferencesDelegates.get().token}")
                    .build()
            )

            tv?.isVisible = false
            view.isVisible = true
            val options = RequestOptions()
            view.clipToOutline = true

            Glide.with(requireContext())
                .load(glideUrl)
                .apply(
                    options.centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .priority(Priority.HIGH)
                        .format(DecodeFormat.PREFER_ARGB_8888)
                )
                .into(view)
        } else {
            tv?.isVisible = true
            view.isVisible = false

            tv?.text = singlechar?.replace(
                "^\\s*([a-zA-Z]).*\\s+([a-zA-Z])\\S+$".toRegex(),
                "$1$2"
            )?.uppercase(Locale.getDefault())
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onContinueOwner(ownerNaturalItem: NaturalPerson?, ownerLegalItem: LegalPerson?) {
        when {
            ownerNaturalItem != null -> {
                setImage(
                    ownerNaturalItem.logoHref,
                    "${ownerNaturalItem.firstName} ${ownerNaturalItem.lastName}",
                    mSelect_bill_Image,
                    textLogoBill
                )
                mBillToName.setText(
                    "${
                        ownerNaturalItem.firstName.toString().ifBlank { "N/A" }
                    } ${ownerNaturalItem.lastName.toString().ifBlank { "N/A" }}"
                )
                invoicePersonGuid = ownerNaturalItem.guid.toString()
            }
            ownerLegalItem != null -> {
                setImage(
                    ownerLegalItem.logoHref,
                    ownerLegalItem.companyName,
                    mSelect_bill_Image,
                    textLogoBill
                )
                mBillToName.setText(ownerLegalItem.companyName.toString().ifBlank { "N/A" })
                invoicePersonGuid = ownerLegalItem.guid.toString()
            }
        }
    }

    override fun openNewUser(type: String?) {
        if (type != null) {
            addNew = true
            bottomSheet?.dismiss()
        }
    }

    override fun onResume() {

        super.onResume()
        if (addNew) {
            bottomSheet = SelectUserFragment(
                this, null, null, this, null,
                "OWNER"
            )
            bottomSheet?.show(requireActivity().supportFragmentManager, null)
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