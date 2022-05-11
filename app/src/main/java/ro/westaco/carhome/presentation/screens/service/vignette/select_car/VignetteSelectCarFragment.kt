package ro.westaco.carhome.presentation.screens.service.vignette.select_car

import android.annotation.SuppressLint
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_vignette_select_car.*
import ro.westaco.carhome.R
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.screens.settings.data.cars.CarsAdapter

@AndroidEntryPoint
class VignetteSelectCarFragment : BaseFragment<VignetteSelectCarViewModel>() {

    private lateinit var adapter: CarsAdapter

    override fun getContentView() = R.layout.fragment_vignette_select_car

    companion object {
        @SuppressLint("StaticFieldLeak")
        var textView: TextView? = null
    }

    override fun getStatusBarColor() = ContextCompat.getColor(requireContext(), R.color.white)

    @SuppressLint("NotifyDataSetChanged")
    override fun initUi() {
        textView = cta
        back.setOnClickListener {
            viewModel.onBack()
        }

        home.setOnClickListener {
            viewModel.onMain()
        }

        cta.setOnClickListener {
            viewModel.onCta(adapter.getSelectedCar())
        }

        list.layoutManager = LinearLayoutManager(context)
        adapter = CarsAdapter(
            requireContext(),
            arrayListOf(),
            enableSelection = true,
            service = "Vignette"
        )
        list.adapter = adapter

        startNew.setOnClickListener {
            viewModel.onStartWithNew()
        }

    }

    override fun setObservers() {

        viewModel.carsLivedata.observe(viewLifecycleOwner) { cars ->

            cta.visibility = if (cars.size > 0) View.VISIBLE else View.GONE
            adapter.setItems(cars)

        }
    }

}