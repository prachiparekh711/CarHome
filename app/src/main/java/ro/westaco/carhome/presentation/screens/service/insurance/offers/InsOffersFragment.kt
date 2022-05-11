package ro.westaco.carhome.presentation.screens.service.insurance.offers

import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_ins_offers.*
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.requests.RcaOfferRequest
import ro.westaco.carhome.data.sources.remote.responses.models.OffersItem
import ro.westaco.carhome.data.sources.remote.responses.models.RcaDurationItem
import ro.westaco.carhome.data.sources.remote.responses.models.RcaOfferResponse
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.screens.service.insurance.adapter.DurationAdapter
import ro.westaco.carhome.utils.Progressbar

@AndroidEntryPoint
class InsOffersFragment : BaseFragment<InsOffersViewModel>(),
    OffersAdapter.OnItemInteractionListener,
    DurationAdapter.OnItemInteractionListener {

    override fun getContentView(): Int {
        return R.layout.fragment_ins_offers
    }

    var rcaOfferResponse: RcaOfferResponse? = null
    var rcaOfferRequest: RcaOfferRequest? = null
    var offerAdapter: OffersAdapter? = null
    var durationList: ArrayList<RcaDurationItem> = ArrayList()
    var progressbar: Progressbar? = null
    var dialogDuration: BottomSheetDialog? = null
    var durationAdapter: DurationAdapter? = null
    var durationRV: RecyclerView? = null
    var close: ImageView? = null
    var dismiss: TextView? = null
    var ctaDuration: TextView? = null
    var durationPosition: Int = 0

    companion object {
        const val ARG_RESPONSE = "arg_response"
        const val ARG_REQUEST = "arg_request"
    }

    override fun initUi() {
        progressbar = Progressbar(requireContext())


        arguments?.let {
            rcaOfferResponse = it.getSerializable(ARG_RESPONSE) as? RcaOfferResponse?
            rcaOfferRequest = it.getSerializable(ARG_REQUEST) as? RcaOfferRequest?
            carNo.text = requireContext().resources.getString(
                R.string.car_no_str,
                rcaOfferRequest?.vehicle?.licensePlate
            )
        }

        toolbar.setNavigationOnClickListener {
            viewModel.onBack()
        }

        offerRv.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        offerAdapter = OffersAdapter(requireContext(), this)
        offerRv.adapter = offerAdapter
        offerAdapter?.setItems(rcaOfferResponse?.offers as List<OffersItem>)

    }

    override fun setObservers() {

        viewModel.durationData.observe(viewLifecycleOwner) { durationList ->
            this.durationList = durationList

            if (durationList.isNotEmpty()) {

                val view = layoutInflater.inflate(R.layout.rca_duration_layout, null)
                dialogDuration = BottomSheetDialog(requireContext())
                dialogDuration?.setCancelable(true)
                dialogDuration?.setContentView(view)
                durationRV = view.findViewById(R.id.durationRV)
                close = view.findViewById(R.id.close)
                dismiss = view.findViewById(R.id.dismiss)
                ctaDuration = view.findViewById(R.id.cta)
                durationRV?.layoutManager =
                    LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

                if (rcaOfferRequest?.rcaDurationId != null) {
                    durationPosition = findPosById(durationList, rcaOfferRequest?.rcaDurationId!!)
                    durationTV.text = durationList[durationPosition].name
                }
                durationAdapter =
                    DurationAdapter(requireContext(), durationPosition, this)
                durationRV?.adapter = durationAdapter
                durationAdapter?.setItems(durationList)

                close?.setOnClickListener {
                    dialogDuration?.dismiss()
                }

                dismiss?.setOnClickListener {
                    dialogDuration?.dismiss()
                }

                ctaDuration?.setOnClickListener {
                    dialogDuration?.dismiss()
                    progressbar?.showPopup()
                    durationTV.text = durationList[durationPosition].name
                    rcaOfferRequest?.rcaDurationId = durationList[durationPosition].id
                    rcaOfferRequest?.let { viewModel.onChangeDuration(it) }
                }

                durationTV.setOnClickListener {
                    dialogDuration?.show()
                }
            }

        }

        viewModel.rcaOfferResponseData.observe(viewLifecycleOwner) { rcaOfferResponseData ->
            progressbar?.dismissPopup()
            offerAdapter?.setItems(rcaOfferResponseData.offers as List<OffersItem>)
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

    override fun onOfferClick(item: OffersItem) {
        item.code?.let { item.insurerCode?.let { it1 -> viewModel.onViewOffer(it, it1) } }
    }

    override fun onPIDClick(item: OffersItem) {
        item.insurerCode?.let { viewModel.onViewPID(it, "RCA") }
    }

    override fun onRCAClick(item: OffersItem) {
        if (durationList.isNotEmpty())
            item.code?.let {
                item.insurerCode?.let { it1 ->
                    viewModel.onViewSummary(
                        it,
                        it1,
                        false,
//                        durationList[durationPosition]
                    )
                }
            }
    }

    override fun onRCADSClick(item: OffersItem) {
        if (durationList.isNotEmpty())
            item.code?.let {
                item.insurerCode?.let { it1 ->
                    viewModel.onViewSummary(
                        it,
                        it1,
                        true,
//                        durationList[durationPosition]
                    )
                }
            }
    }


    override fun onItemClick(item: RcaDurationItem, position: Int) {
        durationPosition = position
    }

}