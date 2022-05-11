package ro.westaco.carhome.presentation.screens.settings.data

import androidx.core.content.ContextCompat
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayout.TabLayoutOnPageChangeListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_data.*
import ro.westaco.carhome.R
import ro.westaco.carhome.presentation.base.BaseFragment

@AndroidEntryPoint
class DataFragment : BaseFragment<DataViewModel>() {

    override fun getContentView() = R.layout.fragment_data

    override fun getStatusBarColor() = ContextCompat.getColor(requireContext(), R.color.white)
    var adapter: DataPagerAdapter? = null

    companion object {

        const val TAG = "DATA"
        const val INDEX = "arg_index"
        var index = 0
    }

    override fun onResume() {
        super.onResume()
        adapter?.notifyDataSetChanged()
    }

    override fun initUi() {

        back.setOnClickListener {
            viewModel.onBack()
        }

        home.setOnClickListener {
            viewModel.onMain()
        }

        tabs.addTab(tabs.newTab().setText(getString(R.string.data_tab_cars)))
        tabs.addTab(tabs.newTab().setText(getString(R.string.natural_person)))
        tabs.addTab(tabs.newTab().setText(getString(R.string.legal_person)))

        adapter = DataPagerAdapter(childFragmentManager)
        pager.adapter = adapter
        setTitle(index)
        pager.addOnPageChangeListener(TabLayoutOnPageChangeListener(tabs))

        tabs.addOnTabSelectedListener(object : OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab) {
                pager.currentItem = tab.position
                setTitle(tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        arguments?.let {
            index = it.getInt(INDEX)
            pager.setCurrentItem(index, true)
            tabs.getTabAt(index)?.select()
        }

    }

    fun setTitle(position: Int) {
        when (position) {
            0 -> title.text = getString(R.string.user_details)
            1 -> title.text = getString(R.string.user_details)
            else -> title.text = getString(R.string.user_details)
        }
    }

    override fun setObservers() {

    }
}