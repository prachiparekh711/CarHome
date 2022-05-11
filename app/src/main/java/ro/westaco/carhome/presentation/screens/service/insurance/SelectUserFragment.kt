package ro.westaco.carhome.presentation.screens.service.insurance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayout
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.LegalPerson
import ro.westaco.carhome.data.sources.remote.responses.models.NaturalPerson
import ro.westaco.carhome.data.sources.remote.responses.models.VerifyRcaPerson
import ro.westaco.carhome.presentation.screens.service.insurance.adapter.MyViewPagerAdapter


class SelectUserFragment(
    var ownerListener: OnOwnerSelectionListener?,
    var userListener: OnUserSelectionListener?,
    var driverListner: OnDriverSelectionListener?,
    var addNewListner: AddNewUserView?,
    var newDriverListner: OnNewDriverSelectionListener?,
    var type: String,
) : BottomSheetDialogFragment() {

    lateinit var viewModel: InsuranceViewModel
    private lateinit var bottomSheet: ViewGroup
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>
    private lateinit var viewPager: ViewPager

    companion object {
        var ownerNaturalItem: NaturalPerson? = null
        var ownerLegalItem: LegalPerson? = null
        var userNaturalItem: NaturalPerson? = null
        var userLegalItem: LegalPerson? = null
        var driverNaturalItem: NaturalPerson? = null
        var driverNewNaturalItem: NaturalPerson? = null
        var verifyNaturalList: ArrayList<VerifyRcaPerson>? = null
        var verifyLegalList: ArrayList<VerifyRcaPerson>? = null
    }

    interface OnOwnerSelectionListener {
        fun onContinueOwner(ownerNaturalItem: NaturalPerson?, ownerLegalItem: LegalPerson?)
    }

    interface OnUserSelectionListener {
        fun onContinueUser(userNaturalItem: NaturalPerson?, userLegalItem: LegalPerson?)
    }

    interface OnDriverSelectionListener {
        fun onContinueDriver(driverNaturalItem: NaturalPerson?)
    }

    interface OnNewDriverSelectionListener {
        fun onContinueDriverNew(driverNewNaturalItem: NaturalPerson?)
    }

    interface AddNewUserView {
        fun openNewUser(type: String?)
    }

    override fun onResume() {
        super.onResume()
        if (type == "DRIVER_NEW")
            viewModel.verifyNaturalPerson("DRIVER")
        else
            viewModel.verifyNaturalPerson(type)
        viewModel.verifyLegalPerson(type)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(InsuranceViewModel::class.java)


    }

    override fun onStart() {
        super.onStart()
        bottomSheet =
            dialog?.findViewById(com.google.android.material.R.id.design_bottom_sheet) as ViewGroup // notice the R root package
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.peekHeight = BottomSheetBehavior.PEEK_HEIGHT_AUTO
        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(view: View, i: Int) {
                if (BottomSheetBehavior.STATE_HIDDEN == i) {
                    dismiss()
                }
            }

            override fun onSlide(view: View, v: Float) {}
        })

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        viewModel.verifyNaturalPerson.observe(viewLifecycleOwner) {
            verifyNaturalList = it
        }

        viewModel.verifyLegalPerson.observe(viewLifecycleOwner) {
            verifyLegalList = it
        }

    }

    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        val myview: View = inflater.inflate(R.layout.fragment_select_user, container, false)
        val tabLayout = myview.findViewById<TabLayout>(R.id.tabs)
        val mClose = myview.findViewById<ImageView>(R.id.close)
        val mDissmiss = myview.findViewById<TextView>(R.id.dissmiss)
        val cta = myview.findViewById<TextView>(R.id.cta)
        val title = myview.findViewById<TextView>(R.id.title)
        viewPager = myview.findViewById(R.id.newPage)
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.natural_person)))
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.legal_person)))
        tabLayout.tabGravity = TabLayout.GRAVITY_FILL

        val adapter = MyViewPagerAdapter(childFragmentManager, type, addNewListner)
        viewPager.adapter = adapter

        when (type) {
            "OWNER" -> {
                title.setText(R.string.select_owner)
            }
            "USER" -> {
                title.setText(R.string.select_user)
            }
            "DRIVER", "DRIVER_NEW" -> {
                title.setText(R.string.select_driver)
                tabLayout.visibility = View.GONE
                adapter.selectDriver()
                adapter.notifyDataSetChanged()
            }
        }

        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        mClose.setOnClickListener { dismiss() }
        mDissmiss.setOnClickListener { dismiss() }

        cta.setOnClickListener {
            when (type) {
                "OWNER" -> {
                    if (ownerNaturalItem != null || ownerLegalItem != null) {
                        ownerListener?.onContinueOwner(ownerNaturalItem, ownerLegalItem)
                        dismiss()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            requireContext().resources.getString(R.string.select_person),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                "USER" -> {
                    if (userNaturalItem != null || userLegalItem != null) {
                        userListener?.onContinueUser(userNaturalItem, userLegalItem)
                        dismiss()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            requireContext().resources.getString(R.string.select_person),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                "DRIVER" -> {
                    if (driverNaturalItem != null) {
                        driverListner?.onContinueDriver(driverNaturalItem)
                        dismiss()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            requireContext().resources.getString(R.string.select_person),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                "DRIVER_NEW" -> {
                    if (driverNewNaturalItem != null) {
                        newDriverListner?.onContinueDriverNew(driverNewNaturalItem)
                        dismiss()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            requireContext().resources.getString(R.string.select_person),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        return myview

    }


}


