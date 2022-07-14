package ro.westaco.carhome.presentation.screens.service.insurance.car_selection

import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_select_cars.*
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.Vehicle
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.screens.service.insurance.adapter.SelectCarsAdapter
import ro.westaco.carhome.views.Progressbar
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class SelectCarsFragment :
    BaseFragment<InsuranceCarViewModel>(),
    SelectCarsAdapter.OnSelectCarsInteractionListener {

    private lateinit var adapter: SelectCarsAdapter
    var vehicle: Vehicle? = null
    var progressbar: Progressbar? = null

    override fun getContentView(): Int {
        return R.layout.fragment_select_cars
    }

    override fun initUi() {
        progressbar = Progressbar(requireContext())

        back.setOnClickListener {
            viewModel.onBack()
        }

        addNew.setOnClickListener {
            viewModel.onAddNew()
        }
    }


    override fun setObservers() {
        viewModel.carsLivedata.observe(viewLifecycleOwner) { carList ->
            rv_cars_list.layoutManager = LinearLayoutManager(context)
            adapter = SelectCarsAdapter(
                requireContext(),
                arrayListOf(),
                this
            )
            rv_cars_list.adapter = adapter
            adapter.setItems(carList)
            addNew.isVisible = true
        }

        viewModel.actionStream.observe(viewLifecycleOwner) {
            when (it) {
                is InsuranceCarViewModel.ACTION.onSuccess -> {
                    progressbar?.dismissPopup()
                }
            }
        }

    }

    private fun purchasedActiveDialog(item: Vehicle) {

        MaterialAlertDialogBuilder(
            requireContext(),
            R.style.ThemeOverlay_App_MaterialAlertDialog
        )
            .setTitle("")
            .setMessage(getString(R.string.insurance_active_dialog))
            .setPositiveButton(getString(R.string.purchase)) { _, _ ->

                progressbar?.showPopup()
                vehicle = item
                viewModel.identifyVehicle(vehicle)
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ ->


            }
            .show()
    }

    override fun onItemClick(item: Vehicle) {

//                    Dialog to show info about purchase
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
                progressbar?.showPopup()
                vehicle = item
                viewModel.identifyVehicle(vehicle)
            } else {
                purchasedActiveDialog(item)
            }
        } else {
            progressbar?.showPopup()
            vehicle = item
            viewModel.identifyVehicle(vehicle)
        }
    }
}