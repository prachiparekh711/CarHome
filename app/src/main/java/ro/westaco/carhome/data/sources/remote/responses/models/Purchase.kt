package ro.westaco.carhome.data.sources.remote.responses.models

import com.google.gson.annotations.SerializedName

data class Purchase(
    @SerializedName("name") val name: String?,
    @SerializedName("logo") val logoUrl: String?,
    @SerializedName("expires") val expires: String?,
    @SerializedName("count") val count: Int,
    @SerializedName("countLeft") val countLeft: Int,
)