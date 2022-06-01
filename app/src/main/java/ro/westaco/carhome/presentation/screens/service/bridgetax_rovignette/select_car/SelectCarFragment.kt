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
import ro.westaco.carhome.presentation.screens.service.bridgetax_rovignette.adapter.CarsAdapter
import ro.westaco.carhome.utils.Progressbar
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


@AndroidEntryPoint
class SelectCarFragment : BaseFragment<SelectCarViewModel>(),
    CarsAdapter.OnSelectCarListner {

    private lateinit var adapter: CarsAdapter
    var selectedVehicle: Vehicle? = null
    var activeService: String = ""
    var progressbar: Progressbar? = null

    override fun getContentView() = R.layout.fragment_service_select_car

    companion object {
        const val ARG_ENTER_VALUE = "arg_enter_value"
    }

    override fun getStatusBarColor() = ContextCompat.getColor(requireContext(), R.color.white)

    @SuppressLint("SimpleDateFormat")
    override fun initUi() {

        progressbar = Progressbar(requireContext())
        progressbar?.showPopup()

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
            viewModel.onBack()
        }

        cta.setOnClickListener {

            if (activeService == "RO_VIGNETTE") {

                if (selectedVehicle?.vignetteExpirationDate?.isNotEmpty() == true) {

                    val dateFormat: DateFormat =
                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                    val date: Date? =
                        dateFormat.parse(selectedVehicle?.vignetteExpirationDate.toString())
                    val formatter: DateFormat =
                        SimpleDateFormat("dd-MM-yyyy")
                    val dateStr: String =
                        formatter.format(date)

                    val sdf = SimpleDateFormat("dd-MM-yyyy")
                    val strDate = sdf.parse(dateStr)

                    if (System.currentTimeMillis() > strDate.time) {

                        if (selectedVehicle != null)
                            viewModel.onCta(selectedVehicle, activeService)
                        else
                            alertDialog()

                    } else {
                        bridgetaxActiveDialog()
                    }

                } else {

                    if (selectedVehicle != null)
                        viewModel.onCta(selectedVehicle, activeService)
                    else
                        alertDialog()

                }
            }

            if (activeService == "RO_PASS_TAX") {

                /*if (selectedVehicle?.policyExpirationDate?.isNotEmpty() == true) {

                    val dateFormat: DateFormat =
                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                    val date: Date? = dateFormat.parse(selectedVehicle?.policyExpirationDate.toString())
                    val formatter: DateFormat =
                        SimpleDateFormat("dd-MM-yyyy")
                    val dateStr: String =
                        formatter.format(date!!)

                    val sdf = SimpleDateFormat("dd-MM-yyyy")
                    val strDate = sdf.parse(dateStr)

                    if (System.currentTimeMillis() > strDate.time) {

                    } else {


                        bridgetaxActiveDialog()

                    }

                } else {

                    if (selectedVehicle != null)
                        viewModel.onCta(selectedVehicle, activeService)
                    else
                        alertDialog()

                }*/

                if (selectedVehicle != null)
                    viewModel.onCta(selectedVehicle, activeService)
                else
                    alertDialog()
            }

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
                li_add.isVisible = true
                progressbar?.dismissPopup()
            }

        }

    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onItemClick(item: Vehicle) {
        cta.background = requireContext().resources.getDrawable(R.drawable.save_background)
        selectedVehicle = item
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


    private fun bridgetaxActiveDialog() {

        MaterialAlertDialogBuilder(
            requireContext(),
            R.style.ThemeOverlay_App_MaterialAlertDialog
        )
            .setTitle("")
            .setMessage(getString(R.string.bridge_tax_active_dialog))
            .setPositiveButton(getString(R.string.purchase)) { _, _ ->

                if (selectedVehicle != null)
                    viewModel.onCta(selectedVehicle, activeService)
                else
                    alertDialog()

            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ ->


            }
            .show()
    }

}