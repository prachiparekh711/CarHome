package ro.westaco.carhome.presentation.screens.settings.data

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import ro.westaco.carhome.presentation.screens.settings.data.cars.CarsFragment
import ro.westaco.carhome.presentation.screens.settings.data.person_legal.LegalPersonsFragment
import ro.westaco.carhome.presentation.screens.settings.data.person_natural.NaturalPersonsFragment

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