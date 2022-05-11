package ro.westaco.carhome.presentation.screens.service.bridgetax.select_bridge_tax

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_bridge_tax_select.*
import ro.westaco.carhome.R
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.screens.settings.data.cars.CarsAdapter


@AndroidEntryPoint
class BridgeTaxSelectFragment : BaseFragment<BridgeTaxSelectViewModel>() {

    override fun getContentView() = R.layout.fragment_bridge_tax_select

    private lateinit var adapter: CarsAdapter

    companion object {
        var textView: TextView? = null
    }

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
        adapter =
            CarsAdapter(requireContext(), arrayListOf(), enableSelection = true, service = "Bridge")
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