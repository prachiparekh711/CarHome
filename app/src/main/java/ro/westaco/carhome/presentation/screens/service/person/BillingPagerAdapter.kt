package ro.westaco.carhome.presentation.screens.service.person

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import ro.westaco.carhome.presentation.screens.service.person.legal.BillLegalFragment
import ro.westaco.carhome.presentation.screens.service.person.natural.BillNaturalFragment

class BillingPagerAdapter(
    f: FragmentManager,
    var listener: BillingInformationFragment.OnServicePersonListener,
    var type: String?,
    var newListener: BillingInformationFragment.AddNewPersonList
) : FragmentPagerAdapter(f) {

    companion object {
        const val NUM_PAGES = 2
    }


    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> BillNaturalFragment(type, null, listener, newListener)
            else -> BillLegalFragment(type, null, listener, newListener)
        }
    }

    override fun getCount(): Int {
        return NUM_PAGES
    }
}
