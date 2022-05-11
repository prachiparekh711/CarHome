package ro.westaco.carhome.data.sources.remote.requests

import com.google.gson.annotations.SerializedName

data class AddReminderRequest(
    @SerializedName("title") val title: String?,
    @SerializedName("notes") val notes: String?,
    @SerializedName("dueDate") val dueDate: String?,
    @SerializedName("dueTime") val dueTime: String?,
    @SerializedName("notifications") val notifications: List<ReminderNotification>?,
    @SerializedName("tags") val tagIds: List<Long>?,
    @SerializedName("repeat") val repeat: Long?,
    @SerializedName("locationGuid") val locationGuid: String? = null,
) {
    override fun toString(): String {
        return "AddReminderRequest(title=$title, notes=$notes, dueDate=$dueDate, dueTime=$dueTime, notifications=$notifications, tagIds=$tagIds, repeat=$repeat, locationGuid=$locationGuid)"
    }
}