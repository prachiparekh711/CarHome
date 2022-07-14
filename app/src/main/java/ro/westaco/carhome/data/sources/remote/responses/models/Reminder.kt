package ro.westaco.carhome.data.sources.remote.responses.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.*

data class Reminder(
    @field:SerializedName("notes")
    val notes: String? = null,

    @field:SerializedName("dueDate")
    val dueDate: Date? = null,

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
    val tags: List<Long?>? = null,

    @field:SerializedName("completed")
    val completed: Boolean? = null
) : ListItem(null, null), Serializable {
    override fun toString(): String {
        return "Reminder(notes=$notes, dueDate=$dueDate, repeat=$repeat, location=$location, id=$id, title=$title, dueTime=$dueTime, notifications=$notifications, locationGuid=$locationGuid, tags=$tags, completed=$completed)"
    }
}


