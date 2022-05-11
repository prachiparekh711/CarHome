package ro.westaco.carhome.data.sources.remote.responses.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class NotificationPrefrences(

    @field:SerializedName("channels")
    val channels: List<String?>? = null,

    @field:SerializedName("type")
    val type: String? = null
) : Serializable {
    override fun toString(): String {
        return "NotificationPrefrences(channels=$channels, type=$type)"
    }
}
