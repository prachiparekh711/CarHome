package ro.westaco.carhome.presentation.screens.data

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import ro.westaco.carhome.presentation.screens.data.cars.CarsFragment
import ro.westaco.carhome.presentation.screens.data.person_legal.LegalPersonsFragment
import ro.westaco.carhome.presentation.screens.data.person_natural.NaturalPersonsFragment

class DataPagerAdapter(f: FragmentManager) : FragmentPagerAdapter(f) {

    companion object {

        const val NUM_PAGES = 3
    }

    override fun getItem(position: Int): Fragment {

        return when (position) {
            0 -> CarsFragment()
            1 -> NaturalPersonsFragment()
            else -> LegalPersonsFragment()
        }
    }

    override fun getCount(): Int {
        return NUM_PAGES
    }
}