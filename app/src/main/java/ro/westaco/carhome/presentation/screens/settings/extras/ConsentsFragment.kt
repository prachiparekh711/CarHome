package ro.westaco.carhome.presentation.screens.settings.extras

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_consents.*
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.TermsResponseItem
import ro.westaco.carhome.presentation.base.BaseFragment

@AndroidEntryPoint
class ConsentsFragment : BaseFragment<CommenViewModel>(),
    ConsentsAdapter.ConsentListener {

    override fun getContentView() = R.layout.fragment_consents

    override fun initUi() {
        viewModel.getAPPTerms()

        back.setOnClickListener {
            viewModel.onBack()
        }
    }

    override fun setObservers() {
        viewModel.termsLiveData.observe(this) { termsList ->
            val consentsAdapter = ConsentsAdapter(requireContext(), this)
            consentsRV.adapter = consentsAdapter
            consentsRV.layoutManager =
                LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            if (termsList != null) {
                consentsAdapter.setItems(termsList)
            }
        }
    }

    override fun onConsentClick(item: TermsResponseItem) {
        viewModel.openTerm(item)
    }
}