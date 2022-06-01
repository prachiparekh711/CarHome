package ro.westaco.carhome.data.sources.remote.responses.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class RcaResponse(

    @field:SerializedName("vehicleOwnerLegalPerson")
    val vehicleOwnerLegalPerson: LegalPersonDetails? = null,

    @field:SerializedName("vehicleOwnerNaturalPerson")
    val vehicleOwnerNaturalPerson: NaturalPersonDetails? = null,

    @field:SerializedName("vehicleUserNaturalPerson")
    val vehicleUserNaturalPerson: NaturalPersonDetails? = null,

    @field:SerializedName("warnings")
    val warnings: List<Any?>? = null,

    @field:SerializedName("vehicleUserSameAsOwner")
    val vehicleUserSameAsOwner: Boolean? = null,

    @field:SerializedName("vehicleUserLegalPerson")
    val vehicleUserLegalPerson: LegalPersonDetails? = null,

    @field:SerializedName("drivers")
    val drivers: List<NaturalPersonDetails?>? = null,

    @field:SerializedName("driverSameAsOwner")
    val driverSameAsOwner: Boolean? = null,

    @field:SerializedName("vehicle")
    val vehicle: VehicleDetailsForOffer? = null
) : Serializable
