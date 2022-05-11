package ro.westaco.carhome.presentation.screens.onboarding

import android.content.Context
import android.view.View
import androidx.viewpager2.widget.ViewPager2
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_onboarding.*
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.local.prefs.AppPreferencesDelegates
import ro.westaco.carhome.presentation.base.BaseActivity
import ro.westaco.carhome.presentation.base.ContextWrapper
import java.util.*

@AndroidEntryPoint
class OnboardingActivity : BaseActivity<OnboardingViewModel>() {

    private lateinit var adapter: OnboardingAdapter

    override fun getContentView() = R.layout.activity_onboarding


    override fun attachBaseContext(newBase: Context) {
        val newLocale: Locale = if (AppPreferencesDelegates.get().language == "en-US") {
            Locale("en")
        } else {
            Locale("ro")
        }
        val context: Context = ContextWrapper.wrap(newBase, newLocale)
        super.attachBaseContext(context)
    }


    override fun setupUi() {
        adapter = OnboardingAdapter()
        pager.adapter = adapter
        pagerIndicator.setViewPager2(pager)
        pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        mSkip.visibility = View.INVISIBLE
                        skip.visibility = View.VISIBLE
                        cta.setText(R.string.onb_get_started)
                        cta.setOnClickListener {
                            viewModel.onSkip()
                        }
                    }

                    1 -> {
                        mSkip.visibility = View.VISIBLE
                        skip.visibility = View.INVISIBLE
                        cta.setText(R.string.new_car_details_cta_next)
                        cta.setOnClickListener {
                            pager.currentItem = 2
                        }
                    }

                    2 -> {
                        mSkip.visibility = View.INVISIBLE
                        skip.visibility = View.VISIBLE
                        cta.setText(R.string.onb_get_started)
                        cta.setOnClickListener {
                            viewModel.onSkip()
                        }
                    }
                }

            }

        })

        mSkip.setOnClickListener {
            viewModel.onSkip()
        }

        skip.setOnClickListener {
            viewModel.onSkip()
        }


    }


    override fun setupObservers() {
        viewModel.pagerItems.observe(this) { items ->
            adapter.setData(items)
        }
    }

}