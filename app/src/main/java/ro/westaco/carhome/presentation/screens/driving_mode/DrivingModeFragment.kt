package ro.westaco.carhome.presentation.screens.driving_mode


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.Menu
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.RequestOptions

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_dashboard.*
import kotlinx.android.synthetic.main.fragment_driving_mode.*
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.local.prefs.AppPreferencesDelegates
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.presentation.screens.dashboard.DashboardFragment


//C- Driving mode section
@AndroidEntryPoint
class DrivingModeFragment : BaseFragment<DrivingModeModel>() {
    companion object {
        const val TAG = "DrivingModeFragment"
        var parentFrag: DashboardFragment? = null
        var DMINDEX = 0
    }


    private lateinit var aReceiver: ReminderViewReceiver
    var adapterViewPager: FragmentPagerAdapter? = null
    override fun getContentView() = R.layout.fragment_driving_mode

    override fun getStatusBarColor() =
        ContextCompat.getColor(requireContext(), R.color.white)

    override fun initUi() {

        parentFrag = this@DrivingModeFragment.parentFragment as DashboardFragment
        aReceiver = ReminderViewReceiver()
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            aReceiver,
            IntentFilter("DASHBOARD_VIEW")
        )

        adapterViewPager = MyPagerAdapter(childFragmentManager)
        viewPager.adapter = adapterViewPager
        viewPager.setCurrentItem(DMINDEX, true)
        changeProgress(DMINDEX)

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                DMINDEX = position
                changeProgress(DMINDEX)
            }

            override fun onPageScrollStateChanged(state: Int) {
            }

        })

        exit.setOnClickListener {
            DashboardFragment.CAR_MODE = requireContext().resources.getString(R.string.normal)
            AppPreferencesDelegates.get().carMode =
                requireContext().resources.getString(R.string.normal)

            val parentFrag: DashboardFragment =
                this@DrivingModeFragment.parentFragment as DashboardFragment
            val menu: Menu = parentFrag.bottomNavigationView.menu
            parentFrag.bottomNavigationView.menu.findItem(R.id.home).isChecked = true
            parentFrag.viewModel.onItemSelected(menu.findItem(R.id.home))
        }

        avatar.setOnClickListener {
            viewModel.onAvatarClicked()
        }
    }

    fun changeProgress(position: Int) {
        when (position) {
            0 -> dmImg.setImageDrawable(requireContext().resources.getDrawable(R.drawable.ic_dm_img1))
            1 -> dmImg.setImageDrawable(requireContext().resources.getDrawable(R.drawable.ic_dm_img2))
            2 -> dmImg.setImageDrawable(requireContext().resources.getDrawable(R.drawable.ic_dm_img3))
        }
    }

    override fun setObservers() {

        viewModel.userLiveData.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                val imageUrl = viewModel.getProfileImage(requireContext(), user)

                val options = RequestOptions()
                avatar.clipToOutline = true
                context?.let {
                    Glide.with(it)
                        .load(imageUrl)
                        .apply(
                            options.fitCenter()
                                .skipMemoryCache(true)
                                .priority(Priority.HIGH)
                                .format(DecodeFormat.PREFER_ARGB_8888)
                        )
                        .into(avatar)
                }
            }
        }
    }

    class MyPagerAdapter(fragmentManager: FragmentManager) :
        FragmentPagerAdapter(fragmentManager) {
        override fun getCount(): Int {
            return NUM_ITEMS
        }

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> DMCarFragment.newInstance()
                1 -> DMServiceFragment.newInstance()
                2 -> DMDataFragment.newInstance()
                else -> DMCarFragment.newInstance()
            }
        }

        companion object {
            private const val NUM_ITEMS = 3
        }
    }

    private class ReminderViewReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            val open = intent?.getStringExtra("open")
            val menu: Menu? = parentFrag?.bottomNavigationView?.menu
            when (open) {
                "REMINDER" -> {
                    parentFrag?.bottomNavigationView?.menu?.findItem(R.id.reminder)?.isChecked =
                        true
                    menu?.findItem(R.id.reminder)?.let { parentFrag?.viewModel?.onItemSelected(it) }
                }

            }
        }
    }
}