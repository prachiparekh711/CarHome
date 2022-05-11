package ro.westaco.carhome.presentation.screens.service.person.legal

import android.app.Dialog
import android.util.Log
import android.view.Window
import android.widget.TextView
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
import ro.westaco.carhome.utils.Progressbar


@AndroidEntryPoint
class BilllegalFragment(var type: String?, var addNewListner: SelectUserFragment.AddNewUserView?) :
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

            adapter.Items(legalPersons)
            progressbar?.dismissPopup()

        }
    }

    override fun onListenerUsers(newItems: LegalPerson) {
        if (type != null) {
            newItems.id?.toLong()?.let { viewModel.fetchLegalDetails(it) }
            if (SelectUserFragment.verifyLegalList?.isNullOrEmpty() == false) {
                SelectUserFragment.verifyLegalList?.indices?.forEach { i ->
                    val verifyItem = SelectUserFragment.verifyLegalList!![i]
                    if (verifyItem.id == newItems.id) {
                        Log.e("Warnings:", verifyItem.validationResult?.warnings.toString())
                        if (verifyItem.validationResult?.warnings?.size == 0) {
                            when (type) {
                                "OWNER" -> ownerLegalItem = newItems
                                "USER" -> userLegalItem = newItems
                            }
                        } else {
//                       Warning Dialog here
                            showDialog(
                                verifyItem.validationResult?.warnings as ArrayList<WarningsItem>
                            )
                        }
                    }
                }
            }
        }

    }

    private fun showDialog(warningList: ArrayList<WarningsItem>) {
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

        mEdit.setOnClickListener {
            viewModel.onEdit()
            addNewListner?.openNewUser(type)
            dialog.dismiss()
        }

        dialog.show()

    }

}