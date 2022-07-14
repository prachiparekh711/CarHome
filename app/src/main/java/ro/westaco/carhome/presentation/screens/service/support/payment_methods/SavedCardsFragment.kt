package ro.westaco.carhome.presentation.screens.service.support.payment_methods

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_profile.back
import kotlinx.android.synthetic.main.fragment_saved_cards.*
import ro.westaco.carhome.R
import ro.westaco.carhome.presentation.base.BaseFragment

@AndroidEntryPoint
class SavedCardsFragment : BaseFragment<SavedCardsViewModel>() {
    private lateinit var adapter: SavedCardsAdapter

    override fun getContentView() = R.layout.fragment_saved_cards

    override fun getStatusBarColor() = ContextCompat.getColor(requireContext(), R.color.white)

    override fun initUi() {
        back.setOnClickListener {
            viewModel.onBack()
        }

        home.setOnClickListener {
            viewModel.onMain()
        }

        fab.setOnClickListener {
            viewModel.onAddCard()
        }

        cardsList.layoutManager = LinearLayoutManager(context)
        adapter = SavedCardsAdapter(requireContext(), arrayListOf())
        cardsList.adapter = adapter
    }

    override fun setObservers() {
        viewModel.cardsLiveData.observe(viewLifecycleOwner) { cards ->
            adapter.setItems(cards)
        }
    }
}