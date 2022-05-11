package ro.westaco.carhome.data.sources.remote.responses.models

import com.google.gson.annotations.SerializedName
import ro.westaco.carhome.data.sources.remote.requests.Address
import java.io.Serializable

data class LegalPersonDetails(
    @field:SerializedName("noRegistration")
    val noRegistration: String? = null,

    @field:SerializedName("vatPayer")
    val vatPayer: Boolean? = null,


    @field:SerializedName("address")
    val address: Address? = null,

    @field:SerializedName("cui")
    val cui: String? = null,

    @field:SerializedName("companyName")
    val companyName: String? = null,

    @field:SerializedName("caen")
    val caen: Caen? = null,

    @field:SerializedName("id")
    val id: Long? = null,

    @field:SerializedName("activityType")
    val activityType: CatalogItem? = null,

    @field:SerializedName("logoHref")
    val logoHref: String? = null,

    @field:SerializedName("guid")
    val guid: String? = null,

    @field:SerializedName("coverHref")
    val coverHref: Any? = null,

    @field:SerializedName("phone")
    val phone: String? = null,

    @field:SerializedName("phoneCountryCode")
    val phoneCountryCode: String? = null,

    @field:SerializedName("email")
    val email: String? = null,


    ) : Serializable {
    override fun toString(): String {
        return "LegalPersonDetails(noRegistration=$noRegistration, vatPayer=$vatPayer, address=$address, cui=$cui, companyName=$companyName, caen=$caen, id=$id, activityType=$activityType)"
    }
}

