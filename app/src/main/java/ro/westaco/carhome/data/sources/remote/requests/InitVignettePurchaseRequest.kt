package ro.westaco.carhome.data.sources.remote.requests

import com.google.gson.annotations.SerializedName
import ro.westaco.carhome.data.sources.remote.responses.models.VignettePrice

data class InitVignettePurchaseRequest(
    @SerializedName("vehicleGuid") val vehicleGuid: String?,
    @SerializedName("registrationCountryCode") val registrationCountryCode: String?,
    @SerializedName("licensePlate") val licensePlate: String?,
    @SerializedName("vin") val vin: String?,
    @SerializedName("price") val price: VignettePrice?,
    @SerializedName("startDate") val startDate: String?,
)