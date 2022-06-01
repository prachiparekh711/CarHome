package ro.westaco.carhome.data.sources.remote.requests

import com.google.gson.annotations.SerializedName
import ro.westaco.carhome.data.sources.remote.responses.models.BridgeTaxPrices
import java.io.Serializable

data class PassTaxInitRequest(

    @SerializedName("registrationCountryCode")
    val registrationCountryCode: String? = null,

    @SerializedName("licensePlate")
    val licensePlate: String? = null,

    @SerializedName("lowerCategoryReason")
    val lowerCategoryReason: Any? = null,

    @SerializedName("price")
    val price: BridgeTaxPrices? = null,

    @SerializedName("vin")
    val vin: String? = null,

    @SerializedName("vehicleGuid")
    val vehicleGuid: String? = null,

    /* @field:SerializedName("vehicleId")
     val vehicleId: Int? = null,*/

    @SerializedName("startDate")
    val startDate: String? = null
) : Serializable

