package ro.westaco.carhome.presentation.screens.settings.security

import android.widget.Toast
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_security.*
import ro.westaco.carhome.R
import ro.westaco.carhome.prefrences.SharedPrefrences
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.utils.BiometricUtil
import ro.westaco.carhome.utils.SwitchButton

//C- Redesign
@AndroidEntryPoint
class SecurityFragment : BaseFragment<SecurityViewModel>() {
    override fun getContentView() = R.layout.fragment_security

    override fun getStatusBarColor() = ContextCompat.getColor(requireContext(), R.color.white)

    override fun initUi() {
        biometricCheck.isChecked = SharedPrefrences.getBiometricsStatus(requireActivity())

        back.setOnClickListener {
            viewModel.onBack()
        }

        home.setOnClickListener {
            viewModel.onMain()
        }

        changePassword.setOnClickListener {
            viewModel.onChangePassword()
        }

        biometricCheck.setOnCheckedChangeListener(object : SwitchButton.OnCheckedChangeListener {
            override fun onCheckedChanged(view: SwitchButton?, isChecked: Boolean) {
                biometricSetting(isChecked)
            }
        })

    }

    override fun setObservers() {

    }

    fun biometricSetting(isChecked: Boolean) {
        if (isChecked) {
            if (BiometricUtil.isHardwareAvailable(requireContext())) {
                if (BiometricUtil.hasBiometricEnrolled(requireContext())) {
                    biometricCheck.isChecked = true
                    SharedPrefrences.setBiometricsStatus(requireActivity(), true)
                    Toast.makeText(
                        requireContext(),
                        requireContext().getString(R.string.bio_enable),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        requireContext().getString(R.string.bio_enroll_fail),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    requireContext().getString(R.string.bio_device_fail),
                    Toast.LENGTH_SHORT
                ).show()
            }

        } else {
            Toast.makeText(
                requireContext(),
                requireContext().getString(R.string.bio_disable),
                Toast.LENGTH_SHORT
            ).show()
            biometricCheck.isChecked = false
            SharedPrefrences.setBiometricsStatus(requireActivity(), false)
        }
    }
}