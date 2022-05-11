package ro.westaco.carhome.presentation.screens.service.transaction_details

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.RequestOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_transaction_details.*
import ro.westaco.carhome.R
import ro.westaco.carhome.di.ApiModule
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.utils.Progressbar
import java.text.SimpleDateFormat
import java.util.*


@AndroidEntryPoint
class TransactionDetailsFragment : BaseFragment<TransactionDetailsViewModel>() {

    private var transactionGuid: String? = null

    var transactionOf: String? = null
    var progressbar: Progressbar? = null
    var statusColor: Int? = null

    companion object {
        const val ARG_TRANSACTION_GUID = "arg_transaction_guid"
        const val ARG_OF = "arg_of"
    }

    override fun getContentView() = R.layout.fragment_transaction_details

    override fun getStatusBarColor() =
        ContextCompat.getColor(requireContext(), statusColor ?: R.color.white)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        progressbar = Progressbar(requireContext())
        progressbar?.showPopup()

        arguments?.let {
            transactionGuid = it.getString(ARG_TRANSACTION_GUID)
            transactionOf = it.getString(ARG_OF)
            transactionOf?.let { it1 -> viewModel.onTransactionGuid(transactionGuid, it1) }
        }
    }


    override fun onResume() {
        super.onResume()
        requireView().isFocusableInTouchMode = true
        requireView().requestFocus()
        requireView().setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                onBackPress()
                true
            } else false
        }
    }


    private fun onBackPress() {
        viewModel.onMain()
    }

    override fun initUi() {
        back.setOnClickListener {
            onBackPress()
        }
    }

    override fun setObservers() {

        viewModel.transactionLiveData.observe(viewLifecycleOwner) { transaction ->

            val spf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            transaction?.let {
                it.status?.let { it1 -> changeTheme(it1) }

                when (transactionOf) {
                    "RO_VIGNETTE" -> type.text =
                        requireContext().resources.getString(R.string.transaction_type_ro)
                    "RO_PASS_TAX" -> type.text =
                        requireContext().resources.getString(R.string.transaction_type_br)
                    "RO_RCA" -> type.text =
                        requireContext().resources.getString(R.string.transaction_type_in)
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

                licensePlate.text = it.vehicleLpn ?: ""
                makeAndModel.text = "${it.vehicleBrandName ?: ""} ${it.vehicleModelName ?: ""}"
                transactionId.text = it.transactionNo
                val newDate: Date = spf.parse(it.availabilityStartDate)
                val spf1 = SimpleDateFormat("dd MMM yyyy")
                startDate.text = spf1.format(newDate)
                duration.text = it.durationDescription

                price.text = "${it.price} ${it.currency}"
                totalPayment.text = "${it.price} ${it.currency}"

                documentTitle.text = it.ticket?.name

                val uploadDate: Date = spf.parse(it.ticket?.uploadedDate)
                val spf2 = SimpleDateFormat("dd MMM yyyy")
                documentDate.text =
                    spf2.format(uploadDate)

                if (it.ticket?.name?.isNotEmpty() == true) {
                    documentGroup.visibility = View.VISIBLE
                } else {
                    documentGroup.visibility = View.GONE
                }

                viewDoc.setOnClickListener {
                    progressbar?.showPopup()
                    viewModel.fetchData()
                }
            }
            progressbar?.dismissPopup()
        }

        viewModel.attachmentData.observe(viewLifecycleOwner) { attachmentData ->
            progressbar?.dismissPopup()
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
            }
            346, 355 -> {
                statusColor = R.color.redExpired
                bgColor.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.redExpired
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
                        R.color.redExpired
                    )
                )
                /*documentGroup.visibility = View.GONE*/
                activity?.window?.statusBarColor =
                    ContextCompat.getColor(requireContext(), R.color.redExpired)
            }
        }
        progressbar?.dismissPopup()
    }


}