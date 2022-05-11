package ro.westaco.carhome.data.sources.remote.requests

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class AddVehicleRequest(

    @field:SerializedName("vehicleIdentityCard")
    val vehicleIdentityCard: String? = null,

    @field:SerializedName("manufacturingYear")
    val manufacturingYear: Int? = null,

    @field:SerializedName("vehicleIdentificationNumber")
    val vehicleIdentificationNumber: String? = null,

    @field:SerializedName("leasingCompany")
    val leasingCompany: Int? = null,

    @field:SerializedName("vehicleUsageType")
    val vehicleUsageType: Int? = null,

    @field:SerializedName("enginePower")
    val enginePower: Int? = null,

    @field:SerializedName("vehicleCategory")
    val vehicleCategory: Int? = null,

    @field:SerializedName("registrationCountryCode")
    val registrationCountryCode: String? = null,

    @field:SerializedName("vehicleBrand")
    val vehicleBrand: Int? = null,

    @field:SerializedName("maxAllowableMass")
    val maxAllowableMass: Int? = null,

    @field:SerializedName("licensePlate")
    val licensePlate: String? = null,

    @field:SerializedName("noOfSeats")
    val noOfSeats: Int? = null,

    @field:SerializedName("fuelType")
    val fuelType: Int? = null,

    @field:SerializedName("engineSize")
    val engineSize: Int? = null,

    @field:SerializedName("vehicleSubCategory")
    val vehicleSubCategory: Int? = null,

    @field:SerializedName("model")
    val model: String? = null,

    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("vehicleEvents")
    val vehicleEvents: List<VehicleEvent?>? = null
) : Serializable {
    override fun toString(): String {
        return "AddVehicleRequest(vehicleIdentityCard=$vehicleIdentityCard, manufacturingYear=$manufacturingYear, vehicleIdentificationNumber=$vehicleIdentificationNumber, leasingCompany=$leasingCompany, vehicleUsageType=$vehicleUsageType, enginePower=$enginePower, vehicleCategory=$vehicleCategory, registrationCountryCode=$registrationCountryCode, vehicleBrand=$vehicleBrand, maxAllowableMass=$maxAllowableMass, licensePlate=$licensePlate, noOfSeats=$noOfSeats, fuelType=$fuelType, engineSize=$engineSize, vehicleSubCategory=$vehicleSubCategory, model=$model, id=$id, vehicleEvents=$vehicleEvents)"
    }
}
