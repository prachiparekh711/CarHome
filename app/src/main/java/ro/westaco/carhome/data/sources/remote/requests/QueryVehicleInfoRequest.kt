package ro.westaco.carhome.data.sources.remote.requests

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class QueryVehicleInfoRequest(
    @SerializedName("registrationCountryCode") val registrationCountryCode: String?,
    @SerializedName("vehicleIdentificationNumber") val vehicleIdentificationNumber: String?,
    @SerializedName("licensePlate") val licensePlate: String?,
) : Serializable