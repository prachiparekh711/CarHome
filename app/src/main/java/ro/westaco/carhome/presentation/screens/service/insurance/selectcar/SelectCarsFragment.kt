package ro.westaco.carhome.presentation.screens.service.insurance.selectcar

import android.os.Bundle
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_select_cars.*
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.Vehicle
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.screens.service.insurance.InsuranceViewModel


@AndroidEntryPoint
class SelectCarsFragment(val vehicleList: ArrayList<Vehicle>) : BaseFragment<InsuranceViewModel>() {

    private lateinit var adapter: SelectCarsAdapter
    var mOnPlayerSelectionSetListener: OnCarSelectionSetListener? = null

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

                    mOnPlayerSelectionSetListener?.onCarSelectionSet(item)

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
}