package ro.westaco.carhome.data.sources.remote.responses.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Attachments(

    @field:SerializedName("name")
    var name: String? = null,

    @field:SerializedName("id")
    var id: Int? = null,

    @field:SerializedName("mimeType")
    var mimeType: String? = null,

    @field:SerializedName("href")
    var href: String? = null,

    @field:SerializedName("uploadedDate")
    var uploadedDate: String? = null,

    @field:SerializedName("customId")
    var customId: String? = null
) : Serializable
