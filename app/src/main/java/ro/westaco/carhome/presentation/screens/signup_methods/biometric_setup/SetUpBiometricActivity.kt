package ro.westaco.carhome.presentation.screens.signup_methods.biometric_setup

import androidx.core.view.isVisible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_set_up_biometric.*
import ro.westaco.carhome.R
import ro.westaco.carhome.prefrences.SharedPrefrences
import ro.westaco.carhome.presentation.base.BaseActivity

//C- SetUpBiometric Screen
@AndroidEntryPoint
class SetUpBiometricActivity : BaseActivity<BiometricSetupModel>() {

    fun initUI() {
        touchRL.isVisible = true
//        faceRL.isVisible = false

        setupTouch.setOnClickListener {
            SharedPrefrences.setBiometricsStatus(this, true)
            SharedPrefrences.setBiometricsMode(this, "TOUCH")
            touchRL.isVisible = false
           viewModel.navigateToProgress()
//            faceRL.isVisible = true
        }

        skipTouch.setOnClickListener {
//            touchRL.isVisible = false
//            faceRL.isVisible = true
            viewModel.navigateToProgress()
        }

//        setupFace.setOnClickListener {
//            SharedPrefrences.setBiometricsStatus(this, true)
//            SharedPrefrences.setBiometricsMode(this, "BOTH")
//            val intent = Intent(this, ProfileProgressActivity::class.java)
//            startActivity(intent)
//            finish()
//        }
//
//        skipFace.setOnClickListener {
//            val intent = Intent(this, ProfileProgressActivity::class.java)
//            startActivity(intent)
//            finish()
//        }
    }

    override fun getContentView() = R.layout.activity_set_up_biometric

    override fun setupUi() {
        initUI()

        back.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onBackPressed() {
        finishAffinity()
    }

    override fun setupObservers() {
    }
}