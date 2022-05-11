package ro.westaco.carhome.presentation.screens.service.person

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import ro.westaco.carhome.presentation.screens.service.person.legal.BilllegalFragment
import ro.westaco.carhome.presentation.screens.service.person.natural.BillNaturalFragment

class BillingPagerAdapter(f: FragmentManager) : FragmentPagerAdapter(f) {

    companion object {
        const val NUM_PAGES = 2
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> BillNaturalFragment(type = null, null)
            else -> BilllegalFragment(type = null, null)
        }
    }

    override fun getCount(): Int {
        return NUM_PAGES
    }
}
