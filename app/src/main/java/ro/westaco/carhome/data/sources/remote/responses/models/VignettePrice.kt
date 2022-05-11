package ro.westaco.carhome.data.sources.remote.responses.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class VignettePrice(
    @SerializedName("id") val id: Long,
    @SerializedName("vignetteCategoryCode") val vignetteCategoryCode: String?,
    @SerializedName("vignetteDurationCode") val vignetteDurationCode: String?,
    @SerializedName("originalValue") val originalValue: Double?,
    @SerializedName("originalCurrency") val originalCurrency: String?,
    @SerializedName("paymentValue") val paymentValue: Double?,
    @SerializedName("paymentCurrency") val paymentCurrency: String?,
) : Serializable