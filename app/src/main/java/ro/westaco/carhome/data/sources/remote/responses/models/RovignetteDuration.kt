package ro.westaco.carhome.data.sources.remote.responses.models

import com.google.gson.annotations.SerializedName

data class RovignetteDuration(
    @SerializedName("code") val code: String,
    @SerializedName("timeUnitCount") val timeUnitCount: Int,
    @SerializedName("timeUnit") val timeUnit: String,
    @SerializedName("description") val description: String,
    @SerializedName("vinRequired") val vinRequired: Boolean,
) {
    override fun toString(): String {
        return description
    }
}