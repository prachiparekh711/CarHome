package ro.westaco.carhome.presentation.screens.signup_methods.profile_progress

import android.annotation.SuppressLint
import android.content.Intent
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_profile_progress.*
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.local.prefs.AppPreferencesDelegates
import ro.westaco.carhome.data.sources.remote.responses.models.VehicleProgressItem
import ro.westaco.carhome.presentation.base.BaseActivity
import ro.westaco.carhome.presentation.screens.home.adapter.CarProgressAdapter
import ro.westaco.carhome.presentation.screens.main.MainActivity
import ro.westaco.carhome.views.MarginItemDecoration

//C- Profile Progress Section
@AndroidEntryPoint
class ProfileProgressActivity : BaseActivity<ProfileProgressModel>(),
    CarProgressAdapter.OnCarItemInteractionListener {

    override fun getContentView() = R.layout.activity_profile_progress
    private lateinit var carAdapter: CarProgressAdapter

    override fun setupUi() {

        AppPreferencesDelegates.get().biometricSetUp = true

        mProfileRL.setOnClickListener {
            val intent = Intent(this@ProfileProgressActivity, MainActivity::class.java)
            intent.putExtra("navigate", "profile")
            startActivity(intent)
            finish()
        }

        noCarRL.setOnClickListener {
            val intent = Intent(this@ProfileProgressActivity, MainActivity::class.java)
            intent.putExtra("navigate", "car_add")
            startActivity(intent)
            finish()
        }

        mSkipRL.setOnClickListener {
            navigateToMain()
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this@ProfileProgressActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    @SuppressLint("SetTextI18n")
    override fun setupObservers() {
        viewModel.progressData.observe(this) { progressItem ->
            if (progressItem != null) {
                val profileItem = progressItem.profileProgress
                if (profileItem != null) {
                    firstNameHint.text = profileItem.description.toString()
                        .ifBlank { resources.getString(R.string.no_name) }

                    if (profileItem.completionPercent != null) {
                        if (profileItem.completionPercent == 100) {
                            mProfileRL.isVisible = false
                        } else {
                            mProfileRL.isVisible = true
                            profileProgress.max = 100
                            profileProgress.progress = profileItem.completionPercent
                            profileProgressLbl.text = "${profileItem.completionPercent}% Finished"
                        }
                    } else {
                        profileProgressLbl.text = "0% Finished"
                    }
                } else {
                    firstNameHint.isInvisible = true
                    profileProgressLbl.text = "0% Finished"
                }

                val vehicleList = progressItem.vehicleProgress

                val car100: ArrayList<VehicleProgressItem> = ArrayList()
                if (vehicleList.isNullOrEmpty()) {
                    noCarRL.isVisible = true
                    carProgressRV.isVisible = false
                } else {
                    for (i in vehicleList.indices) {
                        if (vehicleList[i].id != null) {
                            car100.add(vehicleList[i])
                        }
                    }

                    carProgressRV.isVisible = car100.isNotEmpty()
                    noCarRL.isVisible = car100.isEmpty()

                    carProgressRV.layoutManager =
                        LinearLayoutManager(baseContext, RecyclerView.HORIZONTAL, false)
                    carAdapter = CarProgressAdapter(baseContext, car100, this)
                    carProgressRV.adapter = carAdapter
                    carProgressRV.addItemDecoration(
                        MarginItemDecoration(20, orientation = LinearLayoutManager.HORIZONTAL)
                    )
                    carAdapter.setItems(car100)
                }
                if (profileItem?.completionPercent == 100 && car100.isEmpty()) {
                    indicator1.isVisible = true
                    navigateToMain()
                } else {
                    indicator1.isVisible = false
                }
            } else {
                navigateToMain()
            }
        }
    }

    override fun onBackPressed() {
        finishAffinity()
    }

    override fun onItemClick(item: VehicleProgressItem) {
        val intent = Intent(this@ProfileProgressActivity, MainActivity::class.java)
        intent.putExtra("navigate", "car_id")
        intent.putExtra("cid", item.id)
        startActivity(intent)
        finish()
    }
}