package ro.westaco.carhome.data.sources.remote.responses.models

import com.google.gson.annotations.SerializedName
import ro.westaco.carhome.data.sources.remote.requests.VehicleEvent
import java.io.Serializable

data class VehicleDetails(

    @SerializedName("id") val id: Long,
    @SerializedName("registrationCountryCode") val registrationCountryCode: String?,
    @SerializedName("licensePlate") val licensePlate: String?,
    @SerializedName("policyExpirationDate") val policyExpirationDate: String? = null,
    @SerializedName("vehicleCategory") val vehicleCategory: Long?,
    @SerializedName("vehicleSubCategory") val vehicleSubCategoryId: Long? = null,
    @SerializedName("vehicleSubCategoryName") val vehicleSubCategoryName: String? = null,
    @SerializedName("vehicleUsageType") var vehicleUsageType: Long?,
    @SerializedName("vehicleUsageTypeName") val vehicleUsageTypeName: String?,
    @SerializedName("leasingCompany") val leasingCompany: Int? = null,
    @SerializedName("vehicleBrand") val vehicleBrand: Long?,
    @SerializedName("model") val model: String?,
    @SerializedName("brandLogo") val brandLogo: String? = null,
    @SerializedName("vehicleIdentificationNumber") val vehicleIdentificationNumber: String?,
    @SerializedName("manufacturingYear") val manufacturingYear: Int?,
    @SerializedName("maxAllowableMass") val maxAllowableMass: Int?,
    @SerializedName("engineSize") val engineSize: Int?,
    @SerializedName("enginePower") val enginePower: Int?,
    @SerializedName("fuelType") val fuelTypeId: Long?,
    @SerializedName("noOfSeats") val noOfSeats: Int?,
    @SerializedName("vehicleIdentityCard") val vehicleIdentityCard: String?,
    @SerializedName("vehicleEvents") var vehicleEvents: List<VehicleEvent>?,
    @SerializedName("certificateAttachment") val certificateAttachment: Attachments? = null,
    @SerializedName("otherAttachments") val otherAttachments: ArrayList<Attachments>? = null,
    @SerializedName("vignetteExpirationDate") val vignetteExpirationDate: String? = null,
    @SerializedName("vignetteAttachment") val vignetteAttachment: String? = null,
    @SerializedName("rcaAttachment") val rcaAttachment: String? = null,
    @SerializedName("brandName") val brandName: String? = null,
    @SerializedName("guid") val guid: String? = null

) : Serializable

