package ro.westaco.carhome.data.sources.remote.responses.models

import com.google.gson.annotations.SerializedName

data class ObjectiveItem(

    @field:SerializedName("code")
    val code: String? = null,

    @field:SerializedName("description")
    val description: String? = null,

    @field:SerializedName("active")
    val active: Boolean? = null
)
