package ro.westaco.carhome.data.sources.remote.responses.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Reminder(
    @field:SerializedName("notes")
    val notes: String? = null,

    @field:SerializedName("dueDate")
    val dueDate: String? = null,

    @field:SerializedName("repeat")
    val repeat: Int? = null,

    @field:SerializedName("location")
    val location: LocationV2Item? = null,

    @field:SerializedName("id")
    val id: Long? = null,

    @field:SerializedName("title")
    val title: String? = null,

    @field:SerializedName("dueTime")
    val dueTime: String? = null,

    @field:SerializedName("notifications")
    val notifications: List<Any?>? = null,

    @field:SerializedName("locationGuid")
    val locationGuid: String? = null,

    @field:SerializedName("tags")
    val tags: List<Long?>? = null
) : ListItem(), Serializable {
    override fun toString(): String {
        return "Reminder(notes=$notes, dueDate=$dueDate, repeat=$repeat, location=$location, id=$id, title=$title, dueTime=$dueTime, notifications=$notifications, locationGuid=$locationGuid, tags=$tags)"
    }
}


