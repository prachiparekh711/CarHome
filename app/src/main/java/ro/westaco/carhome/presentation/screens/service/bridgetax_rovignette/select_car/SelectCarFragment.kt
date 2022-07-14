package ro.westaco.carhome.presentation.screens.service.bridgetax_rovignette.select_car

import android.annotation.SuppressLint
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_service_select_car.*
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.Vehicle
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.screens.dashboard.DashboardViewModel.Companion.serviceExpanded
import ro.westaco.carhome.presentation.screens.service.bridgetax_rovignette.adapter.CarsAdapter
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


@AndroidEntryPoint
class SelectCarFragment : BaseFragment<SelectCarViewModel>(),
    CarsAdapter.OnSelectCarListner {

    private lateinit var adapter: CarsAdapter
    var selectedVehicle: Vehicle? = null
    var activeService: String = ""

    override fun getContentView() = R.layout.fragment_service_select_car

    companion object {
        const val ARG_ENTER_VALUE = "arg_enter_value"
    }

    override fun getStatusBarColor() = ContextCompat.getColor(requireContext(), R.color.white)

    @SuppressLint("SimpleDateFormat")
    override fun initUi() {


        arguments?.let {

            activeService = it.getString(ARG_ENTER_VALUE).toString()

            when (activeService) {
                "RO_PASS_TAX" -> {
                    titleItems.text = getString(R.string.bridge_tax)
                }
                "RO_VIGNETTE" -> {
                    titleItems.text = getString(R.string.buy_rovinieta)
                }
            }
        }


        back.setOnClickListener {
            viewModel.onBack()
        }

        home.setOnClickListener {
            viewModel.onMain()
        }


        list.layoutManager = LinearLayoutManager(context)
        adapter = CarsAdapter(
            requireContext(),
            arrayListOf(),
            this,
            activeService
        )
        list.adapter = adapter

        mDismiss.setOnClickListener {
            serviceExpanded = false
            viewModel.onBack()
        }

        cta.setOnClickListener {

            if (selectedVehicle != null) {
                viewModel.onCta(selectedVehicle, activeService)
                selectedVehicle = null
            } else
                alertDialog()
        }

        li_add.setOnClickListener {
            viewModel.onStartWithNew(activeService)
        }

    }

    override fun setObservers() {

        viewModel.carsLivedata.observe(viewLifecycleOwner) { cars ->


            if (cars?.isNotEmpty() == true) {
                cta.visibility = if (cars.size > 0) View.VISIBLE else View.GONE
                adapter.setItems(cars)

            }
            li_add.isVisible = true
        }

    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onItemClick(item: Vehicle) {
        if (activeService == "RO_VIGNETTE") {
            if (item.vignetteExpirationDate?.isNotEmpty() == true) {
                var date: Date
                try {
                    val dateFormat: DateFormat =
                        SimpleDateFormat(requireContext().getString(R.string.server_standard_datetime_format_template))
                    date = dateFormat.parse(item.vignetteExpirationDate)
                } catch (e: Exception) {
                    val dateFormat: DateFormat =
                        SimpleDateFormat(requireContext().getString(R.string.server_standard_datetime_format_template1))
                    date = dateFormat.parse(item.vignetteExpirationDate)
                }

                val formatter: DateFormat =
                    SimpleDateFormat("dd-MM-yyyy")
                val dateStr: String =
                    formatter.format(date)

                val sdf = SimpleDateFormat("dd-MM-yyyy")
                val strDate = sdf.parse(dateStr)

                if (System.currentTimeMillis() > strDate.time) {
                    cta.background =
                        requireContext().resources.getDrawable(R.drawable.save_background)
                    cta.isClickable = true
                    selectedVehicle = item
                } else {
                    selectedVehicle = item
                    activeDialog()
                }

            } else {
                cta.background =
                    requireContext().resources.getDrawable(R.drawable.save_background)
                cta.isClickable = true
                selectedVehicle = item
            }
        } else {
            cta.background = requireContext().resources.getDrawable(R.drawable.save_background)
            cta.isClickable = true
            selectedVehicle = item
        }
    }

    private fun alertDialog() {

        MaterialAlertDialogBuilder(
            requireContext(),
            R.style.ThemeOverlay_App_MaterialAlertDialog
        )
            .setTitle(getString(R.string.information_items_))
            .setMessage(getString(R.string.select_car_leasinginfo))
            .setPositiveButton(getString(R.string.ok)) { _, _ -> }
            .show()
    }


    private fun activeDialog() {


        val msg = if (activeService == "RO_PASS_TAX") {
            requireContext().resources.getString(R.string.bridge_tax_active_dialog)
        } else {
            requireContext().resources.getString(R.string.ro_active_dialog)
        }

        MaterialAlertDialogBuilder(
            requireContext(),
            R.style.ThemeOverlay_App_MaterialAlertDialog
        )
            .setTitle("")
            .setMessage(msg)
            .setPositiveButton(getString(R.string.purchase)) { _, _ ->

                if (selectedVehicle != null)
                    viewModel.onCta(selectedVehicle, activeService)
                else
                    alertDialog()

            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ ->
                adapter.setPosition(-1)
            }
            .show()
    }

}