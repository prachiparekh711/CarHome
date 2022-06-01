package ro.westaco.carhome.data.sources.remote.requests

import com.google.gson.annotations.SerializedName
import ro.westaco.carhome.data.sources.remote.responses.models.LegalPersonDetails
import ro.westaco.carhome.data.sources.remote.responses.models.NaturalPersonForOffer
import ro.westaco.carhome.data.sources.remote.responses.models.VehicleDetailsForOffer
import java.io.Serializable

data class RcaOfferRequest(

    @field:SerializedName("leasing")
    val leasing: Boolean? = null,

    @field:SerializedName("vehicleOwnerLegalPerson")
    val vehicleOwnerLegalPerson: LegalPersonDetails? = null,

    @field:SerializedName("beginDate")
    var beginDate: String? = null,

    @field:SerializedName("vehicleOwnerNaturalPerson")
    val vehicleOwnerNaturalPerson: NaturalPersonForOffer? = null,

    @field:SerializedName("rcaDurationId")
    var rcaDurationId: Int? = null,

    @field:SerializedName("vehicleUserNaturalPerson")
    val vehicleUserNaturalPerson: NaturalPersonForOffer? = null,

    @field:SerializedName("vehicleRegistered")
    val vehicleRegistered: Boolean? = null,

    @field:SerializedName("vehicleUserSameAsOwner")
    val vehicleUserSameAsOwner: Boolean? = null,

    @field:SerializedName("vehicleUserLegalPerson")
    val vehicleUserLegalPerson: LegalPersonDetails? = null,

    @field:SerializedName("drivers")
    val drivers: List<NaturalPersonForOffer?>? = null,

    @field:SerializedName("driverSameAsOwner")
    val driverSameAsOwner: Boolean? = null,

    @field:SerializedName("vehicle")
    val vehicle: VehicleDetailsForOffer? = null

) : Serializable {

    override fun toString(): String {
        return "RcaOfferRequest(leasing=$leasing, vehicleOwnerLegalPerson=$vehicleOwnerLegalPerson, beginDate=$beginDate, vehicleOwnerNaturalPerson=$vehicleOwnerNaturalPerson, rcaDurationId=$rcaDurationId, vehicleUserNaturalPerson=$vehicleUserNaturalPerson, vehicleRegistered=$vehicleRegistered, vehicleUserSameAsOwner=$vehicleUserSameAsOwner, vehicleUserLegalPerson=$vehicleUserLegalPerson, drivers=$drivers, driverSameAsOwner=$driverSameAsOwner, vehicle=$vehicle)"
    }

}
