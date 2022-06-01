package ro.westaco.carhome.presentation.screens.service.person.legal

import android.app.Dialog
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_billlegal.*
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.LegalPerson
import ro.westaco.carhome.data.sources.remote.responses.models.WarningsItem
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.screens.service.insurance.SelectUserFragment
import ro.westaco.carhome.presentation.screens.service.insurance.SelectUserFragment.Companion.ownerLegalItem
import ro.westaco.carhome.presentation.screens.service.insurance.SelectUserFragment.Companion.userLegalItem
import ro.westaco.carhome.presentation.screens.service.person.BillingInformationFragment
import ro.westaco.carhome.utils.Progressbar


@AndroidEntryPoint
class BillLegalFragment(
    var type: String?,
    var addNewListner: SelectUserFragment.AddNewUserView?,
    var servicePersonListner: BillingInformationFragment.OnServicePersonListener?,
    var newListener: BillingInformationFragment.AddNewPersonList?,
) :
    BaseFragment<BillingLegalViewModel>(),
    LegalAdapter.OnItemSelectListViewUser {

    private var progressbar: Progressbar? = null
    private lateinit var adapter: LegalAdapter


    override fun getContentView() =
        R.layout.fragment_billlegal


    override fun initUi() {

        li_add_legal.setOnClickListener {
            if (type != null)
                addNewListner?.openNewUser(type)
            newListener?.openNewPerson(type)
            viewModel.onAddNew()
        }

        progressbar = Progressbar(requireContext())
        progressbar?.showPopup()

        rv_bill_legal.layoutManager = LinearLayoutManager(context)
        adapter = LegalAdapter(requireContext(), arrayListOf(), this)
        rv_bill_legal.adapter = adapter

    }

    override fun setObservers() {

        viewModel.legalPersonsLiveData.observe(viewLifecycleOwner) { legalPersons ->

            adapter.setItems(legalPersons)
            progressbar?.dismissPopup()

        }

        viewModel.legalPersonsDetailsLiveData.observe(viewLifecycleOwner) { legalPersons ->

            if (legalPersons != null) {
                viewModel.onEdit(legalPersons)
            }
            addNewListner?.openNewUser(type)

        }

    }

    override fun onListenerUsers(newItems: LegalPerson, imageView: ImageView) {
        if (type != null) {
//            newItems.id?.toLong()?.let  { viewModel.fetchLegalDetails(it) }
            if (!SelectUserFragment.verifyLegalList.isNullOrEmpty()) {
                SelectUserFragment.verifyLegalList?.indices?.forEach { i ->
                    val verifyItem = SelectUserFragment.verifyLegalList!![i]
                    if (verifyItem.id == newItems.id) {
                        if (verifyItem.validationResult?.warnings?.size == 0) {
                            when (type) {
                                "OWNER" -> ownerLegalItem = newItems
                                "USER" -> userLegalItem = newItems
                            }
                            imageView.isVisible = true
                        } else {
                            imageView.isInvisible = true
                            newItems.id?.toLong()?.let { viewModel.fetchLegalDetails(it) }
                        }
                    }
                }
            }

            servicePersonListner?.onPersonChange(null, newItems)

        }
    }

    private fun showDialog(warningList: ArrayList<WarningsItem>, item: LegalPerson) {

        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.info_layout)
        val mOK = dialog.findViewById(R.id.mOK) as TextView
        val mText = dialog.findViewById(R.id.mText) as TextView
        val mEdit = dialog.findViewById(R.id.mEdit) as TextView
        var warningStr = " "
        for (i in warningList.indices) {
            warningStr = "$warningStr ${warningList[i].field} ${warningList[i].warning} \n"
        }
        mText.text = warningStr

        mOK.setOnClickListener {
            dialog.dismiss()
        }

//        mEdit.setOnClickListener {
//            viewModel.onEdit()
//            addNewListner?.openNewUser(type)
//            dialog.dismiss()
//        }

        dialog.show()

    }

}