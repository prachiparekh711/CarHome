package ro.westaco.carhome.data.sources.remote.responses.models

import com.google.gson.annotations.SerializedName

data class ServiceCategory(
    @SerializedName("code") val code: String,
    @SerializedName("logo") val logoBase64: String,
    @SerializedName("description") val description: String,
    @SerializedName("shortDescription") val shortDescription: String,
) {
    override fun toString(): String {
        return shortDescription
    }
}