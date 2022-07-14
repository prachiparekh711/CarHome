package ro.westaco.carhome.data.sources.remote.requests

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ReminderNotification(
    @Expose @SerializedName("duration") val duration: Long?,
    @Expose @SerializedName("durationUnit") val durationUnit: Long?,
) : Serializable