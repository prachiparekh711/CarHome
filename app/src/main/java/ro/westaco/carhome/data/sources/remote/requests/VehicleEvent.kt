package ro.westaco.carhome.data.sources.remote.requests

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class VehicleEvent(
    @SerializedName("eventType") val eventType: Long?,
    @SerializedName("lastDate") val lastDate: String?,
    @SerializedName("nextDate") val nextDate: String?,
    @SerializedName("reminder") var reminder: Boolean,
    @SerializedName("notifications") val notifications: List<ReminderNotification>?,
    @SerializedName("nextTime") val nextTime: String?,
) : Serializable {
    override fun toString(): String {
        return "VehicleEvent(eventType=$eventType, lastDate=$lastDate, nextDate=$nextDate, reminder=$reminder, notifications=$notifications, nextTime=$nextTime)"
    }
}
