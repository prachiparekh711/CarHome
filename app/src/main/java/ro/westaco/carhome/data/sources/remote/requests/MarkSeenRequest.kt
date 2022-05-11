package ro.westaco.carhome.data.sources.remote.requests

import com.google.gson.annotations.SerializedName

data class MarkSeenRequest(

    @field:SerializedName("ids")
    val ids: List<Int?>? = null
)
