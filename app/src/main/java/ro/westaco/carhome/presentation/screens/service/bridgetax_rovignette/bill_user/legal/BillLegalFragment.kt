package ro.westaco.carhome.presentation.screens.service.bridgetax_rovignette.bill_user.legal

import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_billlegal.*
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.LegalPerson
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.screens.service.bridgetax_rovignette.bill_user.BillingInformationFragment


@AndroidEntryPoint
class BillLegalFragment(
    var type: String?,
    var servicePersonListner: BillingInformationFragment.OnServicePersonListener?,
    var newListener: BillingInformationFragment.AddNewPersonList?,
) :
    BaseFragment<BillingLegalViewModel>(),
    LegalAdapter.OnItemSelectListViewUser {

    private lateinit var adapter: LegalAdapter

    override fun getContentView() =
        R.layout.fragment_billlegal


    override fun initUi() {

        li_add_legal.setOnClickListener {
            newListener?.openNewPerson(type)
            viewModel.onAddNew()
        }

        rv_bill_legal.layoutManager = LinearLayoutManager(context)
        adapter = LegalAdapter(requireContext(), arrayListOf(), this)
        rv_bill_legal.adapter = adapter

    }

    override fun setObservers() {

        viewModel.legalPersonsLiveData.observe(viewLifecycleOwner) { legalPersons ->
            adapter.setItems(legalPersons)
        }
    }

    override fun onListenerUsers(newItems: LegalPerson, isView: Boolean) {
        if (type != null) {
            if (isView) {
                newListener?.openNewPerson(type)
                viewModel.onItemClick(newItems)
            } else {
                servicePersonListner?.onPersonChange(null, newItems)
            }
        }
    }

}