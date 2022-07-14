package ro.westaco.carhome.data.sources.remote.responses.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Notification(

    @field:SerializedName("scheduleAt")
    val scheduleAt: String? = null,

    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("notificationType")
    val notificationType: String? = null,

    @field:SerializedName("title")
    val title: String? = null,

    @field:SerializedName("body")
    val body: String? = null,

    @field:SerializedName("seenAt")
    val seenAt: String? = null

) : ListItem(null, null), Serializable
