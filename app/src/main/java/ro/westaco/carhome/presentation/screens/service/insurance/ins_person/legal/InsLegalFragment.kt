package ro.westaco.carhome.presentation.screens.service.insurance.ins_person.legal

import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_ins_legal.*
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.LegalPerson
import ro.westaco.carhome.data.sources.remote.responses.models.VerifyRcaPerson
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.screens.service.insurance.init.InsuranceFragment
import ro.westaco.carhome.presentation.screens.service.insurance.ins_person.SelectUserFragment

@AndroidEntryPoint
class InsLegalFragment(
    var type: String?,
    var addNewListner: SelectUserFragment.AddNewUserView?
) :
    BaseFragment<InsLegalViewModel>(),
    InsLegalAdapter.OnItemSelectListViewUser {

    private lateinit var adapter: InsLegalAdapter
    var verifyLegalItem: VerifyRcaPerson? = null

    override fun getContentView() =
        R.layout.fragment_ins_legal


    override fun initUi() {

        li_add_legal.setOnClickListener {
            if (type != null)
                addNewListner?.openNewUser(type)
            viewModel.onAddNew()
        }

        rv_bill_legal.layoutManager = LinearLayoutManager(context)
        adapter = InsLegalAdapter(requireContext(), arrayListOf(), this)
        rv_bill_legal.adapter = adapter

    }

    override fun setObservers() {

        viewModel.legalPersonsLiveData.observe(viewLifecycleOwner) { legalPersons ->
            adapter.setItems(legalPersons)
        }

        viewModel.legalPersonsDetailsLiveData.observe(viewLifecycleOwner) { legalPersons ->
            if (InsuranceFragment.IS_PERSON_EDITABLE && legalPersons != null) {
                verifyLegalItem?.let { viewModel.onEdit(legalPersons, it) }
            }
            addNewListner?.openNewUser(type)

        }

    }

    override fun onListenerUsers(
        newItems: LegalPerson,
        isEdit: Boolean, isView: Boolean,
        verifyItem: VerifyRcaPerson?
    ) {
        verifyLegalItem = verifyItem

        if (isEdit) {
            newItems.id?.toLong()?.let { viewModel.fetchLegalDetails(it) }
        } else if (isView) {
            viewModel.onItemClick(newItems)
            addNewListner?.openNewUser(type)
        } else {
            when (type) {
                "OWNER" -> {
                    SelectUserFragment.ownerNaturalItem = null
                    SelectUserFragment.ownerLegalItem = newItems
                }
                "USER" -> {
                    SelectUserFragment.userNaturalItem = null
                    SelectUserFragment.userLegalItem = newItems
                }
            }
        }
    }

}