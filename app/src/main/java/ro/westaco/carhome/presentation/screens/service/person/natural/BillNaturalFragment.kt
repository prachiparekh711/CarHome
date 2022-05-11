package ro.westaco.carhome.presentation.screens.service.person.natural

import android.app.Dialog
import android.view.Window
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_bill_natural.*
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.NaturalPerson
import ro.westaco.carhome.data.sources.remote.responses.models.WarningsItem
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.screens.service.insurance.SelectUserFragment
import ro.westaco.carhome.presentation.screens.service.insurance.SelectUserFragment.Companion.driverNaturalItem
import ro.westaco.carhome.presentation.screens.service.insurance.SelectUserFragment.Companion.driverNewNaturalItem
import ro.westaco.carhome.presentation.screens.service.insurance.SelectUserFragment.Companion.ownerNaturalItem
import ro.westaco.carhome.presentation.screens.service.insurance.SelectUserFragment.Companion.userNaturalItem
import ro.westaco.carhome.presentation.screens.service.insurance.SelectUserFragment.Companion.verifyNaturalList
import ro.westaco.carhome.utils.Progressbar


@AndroidEntryPoint
class BillNaturalFragment(
    var type: String?,
    var addNewListner: SelectUserFragment.AddNewUserView?,
) : BaseFragment<BillingNaturalViewModel>(),
    NaturalAdapter.OnItemSelectListView {

    private lateinit var adapter: NaturalAdapter
    private var progressbar: Progressbar? = null

    override fun getContentView() = R.layout.fragment_bill_natural

    override fun initUi() {

        li_add.setOnClickListener {
            if (type != null)
                addNewListner?.openNewUser(type)
            viewModel.onAddNew()
        }

        progressbar = Progressbar(requireContext())
        progressbar?.showPopup()

        rv_bill.layoutManager = LinearLayoutManager(context)
        adapter = NaturalAdapter(requireContext(), arrayListOf(), this)
        rv_bill.adapter = adapter

    }

    override fun setObservers() {

        viewModel.naturalPersonsLiveData.observe(viewLifecycleOwner) { naturalPersons ->
            adapter.Items(naturalPersons)
            progressbar?.dismissPopup()
        }

    }

    override fun onItemListUsers(newItems: NaturalPerson) {
        viewModel.onNaturalPerson(newItems.id?.toInt())
        if (type != null) {
            if (verifyNaturalList?.isNullOrEmpty() == false) {
                verifyNaturalList?.indices?.forEach { i ->
                    val verifyItem = verifyNaturalList!![i]
                    if (verifyItem.id?.toLong() == newItems.id) {

                        if (verifyItem.validationResult?.warnings?.size == 0) {
                            when (type) {
                                "OWNER" -> ownerNaturalItem = newItems
                                "USER" -> userNaturalItem = newItems
                                "DRIVER" -> driverNaturalItem = newItems
                                "DRIVER_NEW" -> driverNewNaturalItem = newItems
                            }
                        } else {
                            showDialog(verifyItem.validationResult?.warnings as ArrayList<WarningsItem>)
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
        val mEdit = dialog.findViewById(R.id.mEdit) as TextView
        val mText = dialog.findViewById(R.id.mText) as TextView

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
            if (type != null)
                addNewListner?.openNewUser(type)
            dialog.dismiss()
        }

        dialog.show()

    }

}