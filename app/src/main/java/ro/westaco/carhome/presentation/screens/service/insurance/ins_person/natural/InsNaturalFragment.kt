package ro.westaco.carhome.presentation.screens.service.insurance.ins_person.natural

import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_ins_natural.*
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.NaturalPerson
import ro.westaco.carhome.data.sources.remote.responses.models.VerifyRcaPerson
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.screens.service.insurance.init.InsuranceFragment
import ro.westaco.carhome.presentation.screens.service.insurance.ins_person.SelectUserFragment


@AndroidEntryPoint
class InsNaturalFragment(
    var type: String?,
    var addNewListner: SelectUserFragment.AddNewUserView?
) : BaseFragment<InsNaturalViewModel>(),
    InsNaturalAdapter.OnItemSelectListView {

    private lateinit var adapter: InsNaturalAdapter
    override fun getContentView() = R.layout.fragment_ins_natural
    var verifyNaturalItem: VerifyRcaPerson? = null

    override fun initUi() {

        li_add.setOnClickListener {
            if (type != null)
                addNewListner?.openNewUser(type)
            viewModel.onAddNew()
        }


        rv_bill.layoutManager = LinearLayoutManager(context)
        adapter = InsNaturalAdapter(requireContext(), arrayListOf(), this)
        rv_bill.adapter = adapter

    }

    override fun setObservers() {

        viewModel.naturalPersonsLiveData.observe(viewLifecycleOwner) { naturalPersons ->
            adapter.setItems(naturalPersons)
        }

        viewModel.naturalPersonsDetailLiveData.observe(viewLifecycleOwner) { naturalPerson ->
            if (InsuranceFragment.IS_PERSON_EDITABLE && naturalPerson != null) {
                verifyNaturalItem?.let { viewModel.onEdit(naturalPerson, it) }
            }
            addNewListner?.openNewUser(type)
        }
    }

    override fun onItemListUsers(
        newItems: NaturalPerson, isEdit: Boolean, isView: Boolean, verifyItem: VerifyRcaPerson?
    ) {
        verifyNaturalItem = verifyItem
        if (isEdit) {
            newItems.id?.let { viewModel.fetchPersonData(it) }
        } else if (isView) {
            viewModel.onItemClick(newItems)
            addNewListner?.openNewUser(type)
        } else {
            when (type) {
                "OWNER" -> {
                    SelectUserFragment.ownerLegalItem = null
                    SelectUserFragment.ownerNaturalItem = newItems
                }
                "USER" -> {
                    SelectUserFragment.userLegalItem = null
                    SelectUserFragment.userNaturalItem = newItems
                }
                "DRIVER" -> {
                    SelectUserFragment.driverNaturalItem = newItems
                }
                "DRIVER_NEW" -> SelectUserFragment.driverNewNaturalItem =
                    newItems
            }
        }
    }
}
