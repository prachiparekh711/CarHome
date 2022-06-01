package ro.westaco.carhome.data.sources.remote.requests

import com.google.gson.annotations.SerializedName
import ro.westaco.carhome.data.sources.remote.responses.models.VignettePrice
import java.io.Serializable

data class InitVignettePurchaseRequest(
    @SerializedName("vehicleGuid") val vehicleGuid: String?,
//    @SerializedName("vehicleId") val vehicleId: Int?,
    @SerializedName("registrationCountryCode") val registrationCountryCode: String?,
    @SerializedName("licensePlate") val licensePlate: String?,
    @SerializedName("vin") val vin: String?,
    @SerializedName("price") val price: VignettePrice?,
    @SerializedName("startDate") val startDate: String?,
) : Serializable