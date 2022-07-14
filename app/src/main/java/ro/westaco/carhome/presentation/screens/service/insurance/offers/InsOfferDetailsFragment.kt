package ro.westaco.carhome.presentation.screens.service.insurance.offers

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Intent
import android.os.Build
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.request.RequestOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_ins_offer_details.*
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.local.prefs.AppPreferencesDelegates
import ro.westaco.carhome.data.sources.remote.responses.models.NaturalPersonDetails
import ro.westaco.carhome.data.sources.remote.responses.models.RcaDurationItem
import ro.westaco.carhome.data.sources.remote.responses.models.RcaOfferDetails
import ro.westaco.carhome.di.ApiModule
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.screens.pdf_viewer.PdfActivity
import ro.westaco.carhome.utils.CatalogUtils
import ro.westaco.carhome.utils.ViewUtils
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class InsOfferDetailsFragment : BaseFragment<InsOffersDetailsViewModel>() {

    var rcaOfferDetail: RcaOfferDetails? = null
    var driverListAdapter: InsOfferDetailsAdapter? = null
    var durationListItems: ArrayList<RcaDurationItem> = ArrayList()
    var priceDuration: Int = 0
    private var menuOpen1 = false
    private var menuOpen2 = false
    private var menuOpen3 = false
    private var menuOpen4 = false

    companion object {
        const val ARG_OFFERDETAIL = "arg_offerDetail"
    }

    override fun getContentView(): Int {

        return R.layout.fragment_ins_offer_details

    }

    @SuppressLint("SetTextI18n", "SetJavaScriptEnabled")
    override fun initUi() {

        arguments?.let {

            /**
             * OfferView
             */
            rcaOfferDetail = it.getSerializable(ARG_OFFERDETAIL) as? RcaOfferDetails?

            if (rcaOfferDetail != null) {

                rcaOfferDetail?.offer?.insurerLogoHref?.let { it1 ->
                    setImage(
                        it1,
                        insurerImageView
                    )
                }

                tv_offer_title.text = rcaOfferDetail?.offer?.insurerNameLong

                if (rcaOfferDetail?.leasing == true) {
                    leasingView.text = getString(R.string.yes)
                } else {
                    leasingView.text = getString(R.string.no)
                }

                seePID.setOnClickListener {
                    val intent = Intent(requireContext(), PdfActivity::class.java)
                    intent.putExtra(PdfActivity.ARG_INSURER, rcaOfferDetail?.offer?.insurerCode)
                    intent.putExtra(PdfActivity.ARG_SERVICE_TYPE, "RCA")
                    intent.putExtra(PdfActivity.ARG_FROM, "SERVICE")
                    requireContext().startActivity(intent)
                }

                /**
                 * RCA Insurance Date
                 */

                val sdf = SimpleDateFormat("dd/MM/yy, hh:mm a", Locale.getDefault())
                val currentDateandTime: String = sdf.format(Date())

                offerCode.text =
                    getString(R.string.rca_insurance_offer) + " " + rcaOfferDetail?.offer?.code
                offerDate.text = getString(R.string.of) + " " + currentDateandTime

                /**
                 * CarInformation
                 */
                rcaOfferDetail?.vehicle?.brandLogo?.let { it1 -> setImage(it1, mVehicleLogo) }

                mCarRoadStar.text = rcaOfferDetail?.vehicle?.brandName.toString().ifBlank { "N/A" }
                mCarCountry.text = rcaOfferDetail?.vehicle?.registrationCountryCode
                mCarNumber.text = rcaOfferDetail?.vehicle?.licensePlate.toString().ifBlank { "N/A" }
                mCarModel.text = rcaOfferDetail?.vehicle?.model.toString().ifBlank { "N/A" }
                mCarVIN.text = rcaOfferDetail?.vehicle?.vehicleIdentificationNumber.toString()
                    .ifBlank { "N/A" }
                mCarSerialNumber.text =
                    rcaOfferDetail?.vehicle?.vehicleIdentityCard.toString().ifBlank { "N/A" }
                mCarPower.text =
                    rcaOfferDetail?.vehicle?.enginePower.toString() + " " + getString(R.string.hp)
                mCarSeat.text =
                    rcaOfferDetail?.vehicle?.noOfSeats.toString() + " " + getString(R.string.seat)
                mCarMatw.text =
                    rcaOfferDetail?.vehicle?.maxAllowableMass.toString().ifBlank { "N/A" }

                /**
                 * Offer Validity
                 */

                val bDate = rcaOfferDetail?.beginDate
                val eDate = rcaOfferDetail?.endDate
                var spf =
                    SimpleDateFormat(requireContext().resources.getString(R.string.server_date_format_template))
                val newBDate = spf.parse(bDate)
                val newEDate = spf.parse(eDate)
                spf =
                    SimpleDateFormat(requireContext().resources.getString(R.string.date_format_template))

                et_start_date.setText(spf.format(newBDate))
                et_end_date.setText(spf.format(newEDate))

                /**
                 * OwnerData
                 */

                if (rcaOfferDetail?.vehicleOwnerNaturalPerson != null) {
                    mOwnerName.text =
                        rcaOfferDetail?.vehicleOwnerNaturalPerson?.firstName + " " + rcaOfferDetail?.vehicleOwnerNaturalPerson?.lastName
                    mOwnerFirstName.text = rcaOfferDetail?.vehicleOwnerNaturalPerson?.firstName
                    mOwnerLastName.text = rcaOfferDetail?.vehicleOwnerNaturalPerson?.lastName
                    mOwnerPIN.text = rcaOfferDetail?.vehicleOwnerNaturalPerson?.cnp
                    mOwnerPerson.text = getString(R.string.natural_person)
                    mOwnerAddress.text =
                        rcaOfferDetail?.vehicleOwnerNaturalPerson?.address?.countryCode
                    rcaOfferDetail?.vehicleOwnerNaturalPerson?.logoHref?.let { it1 ->
                        setImage(
                            it1,
                            mSelectOwnerImage
                        )
                    }
                }
                if (rcaOfferDetail?.vehicleOwnerLegalPerson != null) {
                    mOwnerName.text =
                        rcaOfferDetail?.vehicleOwnerLegalPerson?.companyName
                    mOwnerPIN.text = rcaOfferDetail?.vehicleOwnerLegalPerson?.noRegistration
                    mOwnerPerson.text = getString(R.string.legal_person)
                    mOwnerAddress.text =
                        rcaOfferDetail?.vehicleOwnerLegalPerson?.address?.countryCode
                    rcaOfferDetail?.vehicleOwnerLegalPerson?.logoHref?.let { it1 ->
                        setImage(
                            it1,
                            mSelectOwnerImage
                        )
                    }
                }
                /**
                 * DriverData
                 */

                mRecycler.layoutManager =
                    LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
                driverListAdapter = InsOfferDetailsAdapter(requireContext())
                mRecycler.adapter = driverListAdapter
                driverListAdapter?.setItems(rcaOfferDetail?.drivers as List<NaturalPersonDetails>)

                /**
                 * Bonus - Malus Class
                 */

                malusClassValue.text = rcaOfferDetail?.offer?.bmClass

                /**
                 * Broker Commission
                 */

                brokerCommission?.settings?.javaScriptEnabled = true
                brokerCommission?.settings?.javaScriptCanOpenWindowsAutomatically = true
                brokerCommission?.settings?.loadsImagesAutomatically = true

                var navigated = false

                brokerCommission?.webViewClient = object : WebViewClient() {
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
                            }
                            true
                        } else {
                            false
                        }
                    }
                }
                rcaOfferDetail?.brokenCommissionHtml?.replace("&#39;", "")
                    ?.let { brokerCommission?.loadData(it, "text/html; charset=UTF-8", null) }

                /**
                 * Direct Settlement
                 */

                directSettlement?.settings?.javaScriptEnabled = true
                directSettlement?.settings?.javaScriptCanOpenWindowsAutomatically = true
                directSettlement?.settings?.loadsImagesAutomatically = true

                directSettlement?.webViewClient = object : WebViewClient() {
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
                            }
                            true
                        } else {
                            false
                        }
                    }
                }
                rcaOfferDetail?.directSettlementHtml?.replace("&#39;", "")
                    ?.let { directSettlement?.loadData(it, "text/html; charset=UTF-8", null) }

                /**
                 * Price List
                 */

                mPrice.text = "${rcaOfferDetail?.offer?.price} ${rcaOfferDetail?.offer?.currency}"
                mPricePL.text =
                    "${rcaOfferDetail?.offer?.priceDs} ${rcaOfferDetail?.offer?.currency}"

            }

            mPricelinearLayout.setOnClickListener {

                rcaOfferDetail?.offer?.code?.let {
                    rcaOfferDetail?.offer?.insurerCode?.let { it1 ->
                        viewModel.onViewSummaryFragment(
                            it, it1, false,
                        )
                    }
                }

            }

            mPricePLLayout.setOnClickListener {

                rcaOfferDetail?.offer?.code?.let {
                    rcaOfferDetail?.offer?.insurerCode?.let { it1 ->
                        viewModel.onViewSummaryFragment(
                            it, it1, true,
                        )
                    }
                }
            }

        }


        car_info_fix_lay.setOnClickListener {
            if (menuOpen1) {
                menuOpen1 = !menuOpen1
                ViewUtils.expand(car_info_hidden_view)
                car_info_fix_lay.setBackgroundResource(R.color.expande_colore)
                id_arrow.setImageResource(R.drawable.ic_arrow_circle_up)
            } else {
                menuOpen1 = !menuOpen1
                ViewUtils.collapse(car_info_hidden_view)
                car_info_fix_lay.setBackgroundResource(R.color.white)
                id_arrow.setImageResource(R.drawable.ic_arrow_circle_down)
            }
        }

        Owner_fix_lay.setOnClickListener {
            if (menuOpen2) {
                menuOpen2 = !menuOpen2
                ViewUtils.expand(Owner_hidden_view)
                Owner_fix_lay.setBackgroundResource(R.color.expande_colore)
                Owner_arrow.setImageResource(R.drawable.ic_arrow_circle_up)
            } else {
                menuOpen2 = !menuOpen2
                ViewUtils.collapse(Owner_hidden_view)
                Owner_fix_lay.setBackgroundResource(R.color.white)
                Owner_arrow.setImageResource(R.drawable.ic_arrow_circle_down)
            }
        }

        Driver_fix_lay.setOnClickListener {
            if (menuOpen3) {
                menuOpen3 = !menuOpen3
                ViewUtils.expand(Driver_hidden_view)
                Driver_fix_lay.setBackgroundResource(R.color.expande_colore)
                Driver_arrow.setImageResource(R.drawable.ic_arrow_circle_up)
            } else {
                menuOpen3 = !menuOpen3
                ViewUtils.collapse(Driver_hidden_view)
                Driver_fix_lay.setBackgroundResource(R.color.white)
                Driver_arrow.setImageResource(R.drawable.ic_arrow_circle_down)
            }
        }

        Offer_fix_lay.setOnClickListener {
            if (menuOpen4) {
                menuOpen4 = !menuOpen4
                ViewUtils.expand(Offer_hidden_view)
                Offer_fix_lay.setBackgroundResource(R.color.expande_colore)
                Offer_arrow.setImageResource(R.drawable.ic_arrow_circle_up)
            } else {
                menuOpen4 = !menuOpen4
                ViewUtils.collapse(Offer_hidden_view)
                Offer_fix_lay.setBackgroundResource(R.color.white)
                Offer_arrow.setImageResource(R.drawable.ic_arrow_circle_down)
            }
        }

        toolbar.setNavigationOnClickListener {
            viewModel.onBack()
        }


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
            .apply(
                options.fitCenter()
                    .skipMemoryCache(true)
                    .priority(Priority.HIGH)
                    .format(DecodeFormat.PREFER_ARGB_8888)
            )
            .into(view)
    }

    override fun setObservers() {

        viewModel.durationData.observe(viewLifecycleOwner) { durationList ->
            if (durationList != null) {
                this.durationListItems = durationList

                val pos = rcaOfferDetail?.rcaDurationId?.let { findPosById(durationList, it) }
                if (pos != null) {
                    priceDuration = pos
                    mDurationItem.setText("${durationList[pos].unitCount}  ${durationList[pos].unit}")

                }
            }
        }

        viewModel.fuelTypeData.observe(viewLifecycleOwner) { fuelTypeData ->

            val fuelTypeStr = rcaOfferDetail?.vehicle?.fuelTypeId?.let {
                CatalogUtils.findById(fuelTypeData, it)?.name
            } ?: ""
            mCarFuelType.text = fuelTypeStr

            mCarFuelType.text = fuelTypeStr
        }

        viewModel.usageTypeData.observe(viewLifecycleOwner) { fuelTypeData ->

            val usageTypeStr = rcaOfferDetail?.vehicle?.vehicleUsageType?.let {
                CatalogUtils.findById(fuelTypeData, it)?.name
            } ?: ""
            mCarUserType.text = usageTypeStr

            mCarUserType.text = usageTypeStr
        }

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

}