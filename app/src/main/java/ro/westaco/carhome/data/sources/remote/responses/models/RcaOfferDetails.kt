package ro.westaco.carhome.data.sources.remote.responses.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class RcaOfferDetails(

    @field:SerializedName("vehicleUserNaturalPerson")
    val vehicleUserNaturalPerson: NaturalPersonDetails? = null,

    @field:SerializedName("endDate")
    val endDate: String? = null,

    @field:SerializedName("vehicleRegistered")
    val vehicleRegistered: Boolean? = null,

    @field:SerializedName("vehicle")
    val vehicle: VehicleDetails? = null,

    @field:SerializedName("leasing")
    val leasing: Boolean? = null,

    @field:SerializedName("offer")
    val offer: OffersItem? = null,

    @field:SerializedName("vehicleOwnerLegalPerson")
    val vehicleOwnerLegalPerson: LegalPersonDetails? = null,

    @field:SerializedName("beginDate")
    val beginDate: String? = null,

    @field:SerializedName("vehicleOwnerNaturalPerson")
    val vehicleOwnerNaturalPerson: NaturalPersonDetails? = null,

    @field:SerializedName("directSettlementHtml")
    val directSettlementHtml: String? = null,

    @field:SerializedName("rcaDurationId")
    val rcaDurationId: Int? = null,

    @field:SerializedName("vehicleUserSameAsOwner")
    val vehicleUserSameAsOwner: Boolean? = null,

    @field:SerializedName("vehicleUserLegalPerson")
    val vehicleUserLegalPerson: LegalPersonDetails? = null,

    @field:SerializedName("drivers")
    val drivers: List<NaturalPersonDetails?>? = null,

    @field:SerializedName("driverSameAsOwner")
    val driverSameAsOwner: Boolean? = null,

    @field:SerializedName("brokenCommissionHtml")
    val brokenCommissionHtml: String? = null

) : Serializable


