package ro.westaco.carhome.presentation.screens.home.purchases

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_purchases.*
import ro.westaco.carhome.R
import ro.westaco.carhome.presentation.base.BaseFragment

@AndroidEntryPoint
class PurchasesFragment : BaseFragment<PurchasesViewModel>() {
    private lateinit var adapter: PurchaseCategoriesAdapter

    override fun getContentView() = R.layout.fragment_purchases

    override fun getStatusBarColor() = ContextCompat.getColor(requireContext(), R.color.appPrimary)

    override fun initUi() {
        list.layoutManager = LinearLayoutManager(context)
        adapter = PurchaseCategoriesAdapter(requireContext(), arrayListOf())
        list.adapter = adapter
    }

    override fun setObservers() {
        viewModel.purchasesWithCategoriesLiveData.observe(
            viewLifecycleOwner
        ) { purchasesWithCategories ->
            adapter.setItems(purchasesWithCategories)
        }
    }
}