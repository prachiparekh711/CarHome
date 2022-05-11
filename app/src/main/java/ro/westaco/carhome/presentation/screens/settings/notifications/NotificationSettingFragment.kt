package ro.westaco.carhome.presentation.screens.settings.notifications

import androidx.core.view.isVisible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_notification_setting.*
import ro.westaco.carhome.R
import ro.westaco.carhome.data.sources.remote.responses.models.Notification
import ro.westaco.carhome.data.sources.remote.responses.models.NotificationPrefrences
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.utils.SwitchButton

//C- Notification Settings
@AndroidEntryPoint
class NotificationSettingFragment : BaseFragment<NotificationSettingModel>() {

    var notificationItem: Notification? = null

    companion object {
        const val ARG_NOTIFICATION = "arg_notification"
    }

    override fun onResume() {
        super.onResume()
        arguments?.let {
            notificationItem = it.getSerializable(ARG_NOTIFICATION) as? Notification?
        }
    }

    override fun getContentView() = R.layout.fragment_notification_setting

    override fun initUi() {

        back.setOnClickListener {
            viewModel.onBack()
        }

        home.setOnClickListener {
            viewModel.onMain()
        }

        rca_switch_button.setOnCheckedChangeListener(multiListener)
        rca_sms_switch_button.setOnCheckedChangeListener(multiListener)
        rca_email_switch_button.setOnCheckedChangeListener(multiListener)
        rca_app_switch_button.setOnCheckedChangeListener(multiListener)

        vignette_switch_button.setOnCheckedChangeListener(multiListener)
        vignette_sms_switch_button.setOnCheckedChangeListener(multiListener)
        vignette_email_switch_button.setOnCheckedChangeListener(multiListener)
        vignette_app_switch_button.setOnCheckedChangeListener(multiListener)

        maintenance_switch_button.setOnCheckedChangeListener(multiListener)
        maintenance_sms_switch_button.setOnCheckedChangeListener(multiListener)
        maintenance_email_switch_button.setOnCheckedChangeListener(multiListener)
        maintenance_app_switch_button.setOnCheckedChangeListener(multiListener)

        oil_switch_button.setOnCheckedChangeListener(multiListener)
        oil_sms_switch_button.setOnCheckedChangeListener(multiListener)
        oil_email_switch_button.setOnCheckedChangeListener(multiListener)
        oil_app_switch_button.setOnCheckedChangeListener(multiListener)
    }

    val multiListener = object : SwitchButton.OnCheckedChangeListener {
        override fun onCheckedChanged(view: SwitchButton?, isChecked: Boolean) {
            var channel: String? = null
            var type: String? = null
            var checkedAll = false
            when (view?.id) {
                R.id.rca_sms_switch_button -> {
                    type = requireContext().resources.getString(R.string.type_rca)
                    channel = requireContext().resources.getString(R.string.sms)
                }
                R.id.rca_email_switch_button -> {
                    type = requireContext().resources.getString(R.string.type_rca)
                    channel = requireContext().resources.getString(R.string.email)
                }
                R.id.rca_app_switch_button -> {
                    type = requireContext().resources.getString(R.string.type_rca)
                    channel = requireContext().resources.getString(R.string.app)
                }
                R.id.vignette_sms_switch_button -> {
                    type = requireContext().resources.getString(R.string.type_vignette)
                    channel = requireContext().resources.getString(R.string.sms)
                }
                R.id.vignette_email_switch_button -> {
                    type = requireContext().resources.getString(R.string.type_vignette)
                    channel = requireContext().resources.getString(R.string.email)
                }
                R.id.vignette_app_switch_button -> {
                    type = requireContext().resources.getString(R.string.type_vignette)
                    channel = requireContext().resources.getString(R.string.app)
                }
                R.id.maintenance_sms_switch_button -> {
                    type = requireContext().resources.getString(R.string.type_maintanance)
                    channel = requireContext().resources.getString(R.string.sms)
                }
                R.id.maintenance_email_switch_button -> {
                    type = requireContext().resources.getString(R.string.type_maintanance)
                    channel = requireContext().resources.getString(R.string.email)
                }
                R.id.maintenance_app_switch_button -> {
                    type = requireContext().resources.getString(R.string.type_maintanance)
                    channel = requireContext().resources.getString(R.string.app)
                }
                R.id.oil_sms_switch_button -> {
                    type = requireContext().resources.getString(R.string.type_oil)
                    channel = requireContext().resources.getString(R.string.sms)
                }
                R.id.oil_email_switch_button -> {
                    type = requireContext().resources.getString(R.string.type_oil)
                    channel = requireContext().resources.getString(R.string.email)
                }
                R.id.oil_app_switch_button -> {
                    type = requireContext().resources.getString(R.string.type_oil)
                    channel = requireContext().resources.getString(R.string.app)
                }
                R.id.rca_switch_button -> {
                    checkedAll = true
                    type = requireContext().resources.getString(R.string.type_rca)
                    if (isChecked) {
                        rca_sms_switch_button.isChecked = true
                        rca_email_switch_button.isChecked = true
                        rca_app_switch_button.isChecked = true
                    } else {
                        rca_sms_switch_button.isChecked = false
                        rca_email_switch_button.isChecked = false
                        rca_app_switch_button.isChecked = false
                    }

                }
                R.id.vignette_switch_button -> {
                    checkedAll = true
                    type = requireContext().resources.getString(R.string.type_vignette)
                    if (isChecked) {
                        vignette_sms_switch_button.isChecked = true
                        vignette_email_switch_button.isChecked = true
                        vignette_app_switch_button.isChecked = true
                    } else {
                        vignette_sms_switch_button.isChecked = false
                        vignette_email_switch_button.isChecked = false
                        vignette_app_switch_button.isChecked = false
                    }

                }
                R.id.maintenance_switch_button -> {
                    checkedAll = true
                    type = requireContext().resources.getString(R.string.type_maintanance)
                    if (isChecked) {
                        maintenance_sms_switch_button.isChecked = true
                        maintenance_email_switch_button.isChecked = true
                        maintenance_app_switch_button.isChecked = true
                    } else {
                        maintenance_sms_switch_button.isChecked = false
                        maintenance_email_switch_button.isChecked = false
                        maintenance_app_switch_button.isChecked = false
                    }

                }
                R.id.oil_switch_button -> {
                    checkedAll = true
                    type = requireContext().resources.getString(R.string.type_oil)
                    if (isChecked) {
                        oil_sms_switch_button.isChecked = true
                        oil_email_switch_button.isChecked = true
                        oil_app_switch_button.isChecked = true
                    } else {
                        oil_sms_switch_button.isChecked = false
                        oil_email_switch_button.isChecked = false
                        oil_app_switch_button.isChecked = false
                    }
                }
            }

            if (checkedAll) {
                checkedAll = false
                if (isChecked) {
                    rcaMainSwitch()
                    vignetteMainSwitch()
                    maintananceMainSwitch()
                    oilMainSwitch()
                    if (type != null) {
                        viewModel.onChange(
                            type,
                            requireContext().resources.getString(R.string.sms),
                            isChecked
                        )
                        viewModel.onChange(
                            type,
                            requireContext().resources.getString(R.string.email),
                            isChecked
                        )
                        viewModel.onChange(
                            type,
                            requireContext().resources.getString(R.string.app),
                            isChecked
                        )
                    }
                }
            } else {

                if (channel != null && type != null) {
                    viewModel.onChange(type, channel, isChecked)
                }
            }
        }
    }


    override fun setObservers() {
        viewModel.notificationPrefLivedata.observe(viewLifecycleOwner) { notificationPrefList ->


            for (i in notificationPrefList.indices) {
                val pref = notificationPrefList[i]
                when (pref.type) {
                    "EXPIRY_RCA" -> rcaData(pref)
                    "EXPIRY_VIGNETTE" -> vignetteData(pref)
                    "MAINTENANCE_REMINDER" -> maintananceData(pref)
                    "OIL_CHANGE_REMINDER" -> oilData(pref)
                }
            }
            rcaMainSwitch()
            vignetteMainSwitch()
            maintananceMainSwitch()
            oilMainSwitch()
        }
    }

    fun rcaData(pref: NotificationPrefrences) {
        val channels = pref.channels
        if (!channels.isNullOrEmpty()) {

            for (j in channels.indices) {
                when (channels[j]) {
                    "SMS" -> {
                        rca_sms_switch_button.isChecked = true
                    }
                    "EMAIL" -> {
                        rca_email_switch_button.isChecked = true
                    }
                    "APP" -> {
                        rca_app_switch_button.isChecked = true
                    }
                }
            }
        }
    }

    fun vignetteData(pref: NotificationPrefrences) {
        val channels = pref.channels
        if (!channels.isNullOrEmpty()) {

            for (j in channels.indices) {
                when (channels[j]) {
                    "SMS" -> {
                        vignette_sms_switch_button.isChecked = true
                    }
                    "EMAIL" -> {
                        vignette_email_switch_button.isChecked = true
                    }
                    "APP" -> {
                        vignette_app_switch_button.isChecked = true
                    }
                }
            }

        }

    }

    fun maintananceData(pref: NotificationPrefrences) {
        val channels = pref.channels
        if (!channels.isNullOrEmpty()) {

            for (j in channels.indices) {
                when (channels[j]) {
                    "SMS" -> {
                        maintenance_sms_switch_button.isChecked = true
                    }
                    "EMAIL" -> {
                        maintenance_email_switch_button.isChecked = true
                    }
                    "APP" -> {
                        maintenance_app_switch_button.isChecked = true
                    }
                }
            }
        }

    }

    fun oilData(pref: NotificationPrefrences) {
        val channels = pref.channels
        if (!channels.isNullOrEmpty()) {

            for (j in channels.indices) {
                when (channels[j]) {
                    "SMS" -> {
                        oil_sms_switch_button.isChecked = true
                    }
                    "EMAIL" -> {
                        oil_email_switch_button.isChecked = true
                    }
                    "APP" -> {
                        oil_app_switch_button.isChecked = true
                    }
                }
            }
        }

    }

    fun rcaMainSwitch() {
        if (!rca_sms_switch_button.isChecked && !rca_email_switch_button.isChecked && !rca_app_switch_button.isChecked) {
            rcaLL.isVisible = false
            rca_switch_button.isVisible = true
        } else {
            rcaLL.isVisible = true
            rca_switch_button.isVisible = false
        }
    }

    fun vignetteMainSwitch() {
        if (!vignette_sms_switch_button.isChecked && !vignette_email_switch_button.isChecked && !vignette_app_switch_button.isChecked) {
            vignetteLL.isVisible = false
            vignette_switch_button.isVisible = true
        } else {
            vignetteLL.isVisible = true
            vignette_switch_button.isVisible = false
        }
    }

    fun maintananceMainSwitch() {
        if (!maintenance_sms_switch_button.isChecked && !maintenance_email_switch_button.isChecked && !maintenance_app_switch_button.isChecked) {
            maintenanceLL.isVisible = false
            maintenance_switch_button.isVisible = true
        } else {
            maintenanceLL.isVisible = true
            maintenance_switch_button.isVisible = false
        }
    }

    fun oilMainSwitch() {
        if (!oil_sms_switch_button.isChecked && !oil_email_switch_button.isChecked && !oil_app_switch_button.isChecked) {
            oilLL.isVisible = false
            oil_switch_button.isVisible = true
        } else {
            oilLL.isVisible = true
            oil_switch_button.isVisible = false
        }
    }
}

