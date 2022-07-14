package ro.westaco.carhome.presentation.screens.data.person_legal

import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_legal_persons.*
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.LegalPerson
import ro.westaco.carhome.presentation.base.BaseFragment

//C- Designing & Logo
@AndroidEntryPoint
class LegalPersonsFragment : BaseFragment<LegalPersonsViewModel>(),
    LegalPersonsAdapter.OnItemInteractionListener {

    private lateinit var adapter: LegalPersonsAdapter

    override fun getContentView() = R.layout.fragment_legal_persons

    override fun getStatusBarColor() = ContextCompat.getColor(requireContext(), R.color.white)

    override fun initUi() {

        fab.setOnClickListener {
            viewModel.onAddNew()
        }

        cta.setOnClickListener {
            viewModel.onAddNew()
        }

        list.layoutManager = LinearLayoutManager(context)
        adapter = LegalPersonsAdapter(requireContext(), arrayListOf(), this)
        list.adapter = adapter
    }

    override fun setObservers() {

        viewModel.legalPersonsLiveData.observe(viewLifecycleOwner) { legalPersons ->

            if (legalPersons.isNullOrEmpty()) {
                normalState.visibility = View.GONE
                emptyState.visibility = View.VISIBLE
            } else {
                normalState.visibility = View.VISIBLE
                emptyState.visibility = View.GONE

                adapter.setItems(legalPersons)
            }
        }

    }

    override fun onClick(item: LegalPerson) {
        viewModel.onItemClick(item)
    }


}