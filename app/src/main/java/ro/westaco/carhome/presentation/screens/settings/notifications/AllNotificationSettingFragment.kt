package ro.westaco.carhome.presentation.screens.settings.notifications

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_all_notification_setting.*
import ro.westaco.carhome.R
import ro.westaco.carhome.presentation.base.BaseFragment
import ro.westaco.carhome.views.SwitchButton

@AndroidEntryPoint
class AllNotificationSettingFragment : BaseFragment<NotificationSettingModel>() {
    override fun getContentView() = R.layout.fragment_all_notification_setting

    override fun initUi() {
        back.setOnClickListener {
            viewModel.onBack()
        }

        home.setOnClickListener {
            viewModel.onMain()
        }

        sms_switch_button.setOnCheckedChangeListener(multiListener)
        email_switch_button.setOnCheckedChangeListener(multiListener)
        app_switch_button.setOnCheckedChangeListener(multiListener)

    }

    private val multiListener = object : SwitchButton.OnCheckedChangeListener {
        override fun onCheckedChanged(view: SwitchButton?, isChecked: Boolean) {
            var channel: String? = null
            var type: String? = null
            when (view?.id) {
                R.id.sms_switch_button -> {
                    channel = requireContext().resources.getString(R.string.sms)
                }
                R.id.email_switch_button -> {
                    channel = requireContext().resources.getString(R.string.noti_email)
                }
                R.id.app_switch_button -> {
                    channel = requireContext().resources.getString(R.string.app)
                }
            }

            type = "ALL"
            if (channel != null) {
                viewModel.onChange(type, channel, isChecked)
            }
        }
    }

    override fun setObservers() {

        viewModel.notificationPrefLivedata.observe(viewLifecycleOwner) { notificationPrefList ->

            for (i in notificationPrefList.indices) {
                val pref = notificationPrefList[i]
                if (pref.type == "ALL") {
                    val channels = pref.channels
                    if (!channels.isNullOrEmpty()) {
                        for (j in channels.indices) {
                            when (channels[j]) {
                                "SMS" -> {
                                    sms_switch_button.isChecked = true
                                }
                                "EMAIL" -> {
                                    email_switch_button.isChecked = true
                                }
                                "APP" -> {
                                    app_switch_button.isChecked = true
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}