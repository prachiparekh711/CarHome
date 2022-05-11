package ro.westaco.carhome.data.sources.remote.responses.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class EventType(

    @field:SerializedName("name")
    val name: String? = null,

    @field:SerializedName("collectLastDate")
    val collectLastDate: Boolean? = null,

    @field:SerializedName("id")
    val id: Int? = null
) : Serializable
