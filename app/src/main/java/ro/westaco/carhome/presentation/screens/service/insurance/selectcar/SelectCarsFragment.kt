package ro.westaco.carhome.presentation.screens.service.insurance.selectcar

import android.os.Bundle
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_select_cars.*
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.Vehicle
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.screens.service.insurance.init.InsuranceViewModel
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


@AndroidEntryPoint
class SelectCarsFragment(private val vehicleList: ArrayList<Vehicle>) :
    BaseFragment<InsuranceViewModel>() {

    private lateinit var adapter: SelectCarsAdapter
    var mOnPlayerSelectionSetListener: OnCarSelectionSetListener? = null
    var vehicle: Vehicle? = null

    override fun getContentView(): Int {
        return R.layout.fragment_select_cars
    }

    private fun onAttachToParentFragment(fragment: Fragment) {
        try {
            mOnPlayerSelectionSetListener = fragment as OnCarSelectionSetListener
        } catch (e: ClassCastException) {
            throw ClassCastException(
                fragment.toString().toString() + " must implement OnCarSelectionSetListener"
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parentFragment?.let { onAttachToParentFragment(it) }
    }

    override fun initUi() {

        back.setOnClickListener {
            mOnPlayerSelectionSetListener?.onBackFromCarList()
        }

        rv_cars_list.layoutManager = LinearLayoutManager(context)
        adapter = SelectCarsAdapter(
            requireContext(),
            arrayListOf(),
            object : SelectCarsAdapter.OnSelectCarsInteractionListener {
                override fun onItemClick(item: Vehicle) {

                    vehicle = item

                    if (item.policyExpirationDate?.isNotEmpty() == true) {

                        val dateFormat: DateFormat =
                            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                        val date: Date? = dateFormat.parse(item.policyExpirationDate.toString())
                        val formatter: DateFormat =
                            SimpleDateFormat("dd-MM-yyyy")
                        val dateStr: String =
                            formatter.format(date)

                        val sdf = SimpleDateFormat("dd-MM-yyyy")
                        val strDate = sdf.parse(dateStr)

                        if (System.currentTimeMillis() > strDate.time) {

                            mOnPlayerSelectionSetListener?.onCarSelectionSet(item)
                        } else {
                            bridgeTaxActiveDialog()
                        }
                    } else {
                        mOnPlayerSelectionSetListener?.onCarSelectionSet(item)

                    }
                }
            })
        rv_cars_list.adapter = adapter
        adapter.setItems(vehicleList)
        requireView().isFocusableInTouchMode = true
        requireView().requestFocus()
        requireView().setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                mOnPlayerSelectionSetListener?.onBackFromCarList()
                true
            } else false
        }
    }

    override fun setObservers() {


    }

    interface OnCarSelectionSetListener {

        fun onCarSelectionSet(item: Vehicle)
        fun onBackFromCarList()
    }

    private fun bridgeTaxActiveDialog() {

        MaterialAlertDialogBuilder(
            requireContext(),
            R.style.ThemeOverlay_App_MaterialAlertDialog
        )
            .setTitle("")
            .setMessage(getString(R.string.bridge_tax_active_dialog))
            .setPositiveButton(getString(R.string.purchase)) { _, _ ->


                vehicle?.let { mOnPlayerSelectionSetListener?.onCarSelectionSet(it) }

            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ ->


            }
            .show()
    }
}