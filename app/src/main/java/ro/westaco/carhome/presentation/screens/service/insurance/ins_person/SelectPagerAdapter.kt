package ro.westaco.carhome.presentation.screens.service.insurance.ins_person

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import ro.westaco.carhome.presentation.screens.service.insurance.ins_person.legal.InsLegalFragment
import ro.westaco.carhome.presentation.screens.service.insurance.ins_person.natural.InsNaturalFragment

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
            return InsNaturalFragment(type, addNewListner)
        } else {
            when (position) {
                0 -> return InsNaturalFragment(type, addNewListner)
                1 -> return InsLegalFragment(type, addNewListner)
            }
            return InsNaturalFragment(type, addNewListner)
        }
    }

    override fun getCount(): Int {
        return if (type == "DRIVER")
            1
        else
            NUM_ITEMS
    }

    fun selectDriver(): Fragment {
        return InsNaturalFragment(type, addNewListner)
    }

}