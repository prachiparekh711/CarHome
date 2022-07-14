package ro.westaco.carhome.presentation.screens.service.bridgetax_rovignette.bill_user.natural

import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_bill_natural.*
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.NaturalPerson
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.screens.service.bridgetax_rovignette.bill_user.BillingInformationFragment


@AndroidEntryPoint
class BillNaturalFragment(
    var type: String?,
    var servicePersonListner: BillingInformationFragment.OnServicePersonListener?,
    var newListener: BillingInformationFragment.AddNewPersonList?,
) : BaseFragment<BillingNaturalViewModel>(),
    NaturalAdapter.OnItemSelectListView {

    private lateinit var adapter: NaturalAdapter
    override fun getContentView() = R.layout.fragment_bill_natural

    override fun initUi() {

        li_add.setOnClickListener {
            newListener?.openNewPerson(type)
            viewModel.onAddNew()
        }


        rv_bill.layoutManager = LinearLayoutManager(context)
        adapter = NaturalAdapter(requireContext(), arrayListOf(), this)
        rv_bill.adapter = adapter

    }

    override fun setObservers() {

        viewModel.naturalPersonsLiveData.observe(viewLifecycleOwner) { naturalPersons ->
            adapter.Items(naturalPersons)
        }
    }

    override fun onItemListUsers(newItems: NaturalPerson, isView: Boolean) {

        if (type != null) {
            if (isView) {
                newListener?.openNewPerson(type)
                viewModel.onItemClick(newItems)
            } else {
                servicePersonListner?.onPersonChange(newItems, null)
            }
        }
    }

}