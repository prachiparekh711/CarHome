package ro.westaco.carhome.presentation.screens.data.person_natural

import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_natural_persons.*
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.NaturalPerson
import ro.westaco.carhome.presentation.base.BaseFragment

//C- Redesign & Logo
@AndroidEntryPoint
class NaturalPersonsFragment : BaseFragment<NaturalPersonsViewModel>(),
    NaturalPersonsAdapter.OnItemInteractionListener {

    private lateinit var adapter: NaturalPersonsAdapter
    override fun getContentView() = R.layout.fragment_natural_persons

    override fun getStatusBarColor() = ContextCompat.getColor(requireContext(), R.color.white)

    override fun initUi() {


        fab.setOnClickListener {
            viewModel.onAddNew()
        }

        cta.setOnClickListener {
            viewModel.onAddNew()
        }

        list.layoutManager = LinearLayoutManager(context)
        adapter = NaturalPersonsAdapter(requireContext(), arrayListOf(), this)
        list.adapter = adapter
    }

    override fun setObservers() {

        viewModel.naturalPersonsLiveData.observe(viewLifecycleOwner) { naturalPersons ->

            if (naturalPersons.isNullOrEmpty()) {

                normalState.visibility = View.GONE
                emptyState.visibility = View.VISIBLE

            } else {

                normalState.visibility = View.VISIBLE
                emptyState.visibility = View.GONE

                adapter.setItems(naturalPersons)

            }
        }

    }

    override fun onClick(item: NaturalPerson) {
        viewModel.onItemClick(item)
    }

    override fun onEdit(item: NaturalPerson) {
        viewModel.onItemEdit(item)
    }

    override fun onDial(item: NaturalPerson) {
        viewModel.openDialPad(item)
    }

    override fun onMail(item: NaturalPerson) {
        viewModel.openComposedMail(item)
    }
}