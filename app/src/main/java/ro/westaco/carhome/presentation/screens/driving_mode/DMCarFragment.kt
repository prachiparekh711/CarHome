package ro.westaco.carhome.presentation.screens.driving_mode

import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_d_m_car.*
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.Vehicle
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.screens.home.HomeViewModel
import ro.westaco.carhome.presentation.screens.settings.data.cars.CarsAdapter
import ro.westaco.carhome.utils.Progressbar

@AndroidEntryPoint
class DMCarFragment : BaseFragment<HomeViewModel>(),
    CarsAdapter.OnItemInteractionListener {
    private lateinit var carAdapter: CarsAdapter
    var progressbar: Progressbar? = null

    companion object {
        fun newInstance(): DMCarFragment {
            return DMCarFragment()
        }
    }

    override fun getContentView() = R.layout.fragment_d_m_car

    override fun getStatusBarColor() =
        ContextCompat.getColor(requireContext(), R.color.white)

    override fun initUi() {
        progressbar = Progressbar(requireContext())
        progressbar!!.showPopup()
        carRV.layoutManager = LinearLayoutManager(context)
        carAdapter = CarsAdapter(requireContext(), arrayListOf(), this)
        carRV.adapter = carAdapter
    }

    override fun setObservers() {
        viewModel.carsLivedata.observe(viewLifecycleOwner) { cars ->
            if (cars.isEmpty()) {
                noCarLL.isVisible = true
                carRV.isVisible = false
            } else {
                noCarLL.isVisible = false
                carRV.isVisible = true
                carAdapter.setItems(cars)
            }
            progressbar?.dismissPopup()
        }
    }

    override fun onItemClick(item: Vehicle) {
        item.id.let {
            if (it != null) {
                viewModel.onEditCar(it)
            }
        }
    }

}