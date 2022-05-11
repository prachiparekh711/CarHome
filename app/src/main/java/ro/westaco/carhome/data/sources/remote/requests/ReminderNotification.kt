package ro.westaco.carhome.data.sources.remote.requests

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ReminderNotification(
    @SerializedName("duration") val duration: Long?,
    @SerializedName("durationUnit") val durationUnit: Long?,
) : Serializable