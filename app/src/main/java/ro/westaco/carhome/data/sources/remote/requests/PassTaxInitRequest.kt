package ro.westaco.carhome.data.sources.remote.requests

import com.google.gson.annotations.SerializedName
import ro.westaco.carhome.data.sources.remote.responses.models.BridgeTaxPrices

data class PassTaxInitRequest(

    @field:SerializedName("registrationCountryCode")
    val registrationCountryCode: String? = null,

    @field:SerializedName("licensePlate")
    val licensePlate: String? = null,

    @field:SerializedName("lowerCategoryReason")
    val lowerCategoryReason: Any? = null,

    @field:SerializedName("price")
    val price: BridgeTaxPrices? = null,

    @field:SerializedName("vin")
    val vin: Any? = null,

    @field:SerializedName("vehicleGuid")
    val vehicleGuid: String? = null,

    @field:SerializedName("startDate")
    val startDate: Any? = null
)

