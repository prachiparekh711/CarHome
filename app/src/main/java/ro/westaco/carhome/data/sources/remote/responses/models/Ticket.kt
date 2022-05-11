package ro.westaco.carhome.data.sources.remote.responses.models

import com.google.gson.annotations.SerializedName

data class Ticket(
    @SerializedName("id") val id: Int?,
    @SerializedName("name") val name: String?,
    @SerializedName("mimeType") val mimeType: String?,
    @SerializedName("href") val href: String?,
    @SerializedName("uploadedDate") val uploadedDate: String?,
)