package ro.westaco.carhome.data.sources.remote.requests

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class DrivingLicense(
    @SerializedName("licenseId") val licenseId: String?,
    @SerializedName("issueDate") val issueDate: String?,
    @SerializedName("expirationDate") val expirationDate: String?,
    @SerializedName("vehicleCategories") val vehicleCategories: List<Int>?,
) : Serializable
