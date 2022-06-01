package ro.westaco.carhome.presentation.screens.service.insurance.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import ro.westaco.carhome.presentation.screens.service.insurance.SelectUserFragment
import ro.westaco.carhome.presentation.screens.service.person.legal.BillLegalFragment
import ro.westaco.carhome.presentation.screens.service.person.natural.BillNaturalFragment

class MyViewPagerAdapter(
    f: FragmentManager,
    var type: String,
    var addNewListner: SelectUserFragment.AddNewUserView?,
) : FragmentPagerAdapter(f) {

    companion object {
        const val NUM_ITEMS = 2
    }

    override fun getItem(position: Int): Fragment {
        if (type == "DRIVER") {
            return BillNaturalFragment(type, addNewListner, null, null)
        } else {
            when (position) {
                0 -> return BillNaturalFragment(type, addNewListner, null, null)
                1 -> return BillLegalFragment(type, addNewListner, null, null)
            }
            return BillNaturalFragment(type, addNewListner, null, null)
        }
    }

    override fun getCount(): Int {
        return if (type == "DRIVER")
            1
        else
            NUM_ITEMS
    }

    fun selectDriver(): Fragment {
        return BillNaturalFragment(type, addNewListner, null, null)
    }

}