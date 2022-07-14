package ro.westaco.carhome.presentation.screens.reminder.add_new

import ro.westaco.carhome.data.sources.remote.requests.ReminderNotification

data class NotificationWithUnit(
    val reminderNotification: ReminderNotification,
    val unitName: String
)
