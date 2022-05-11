package ro.westaco.carhome.data.sources.remote.requests

import com.google.gson.annotations.SerializedName
import ro.westaco.carhome.data.sources.remote.responses.models.Caen
import ro.westaco.carhome.data.sources.remote.responses.models.CatalogItem

data class AddLegalPersonRequest(

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
    val logoHref: Any? = null,

    @field:SerializedName("guid")
    val guid: Any? = null,

    @field:SerializedName("coverHref")
    val coverHref: Any? = null,

    @field:SerializedName("phone")
    val phone: String? = null,

    @field:SerializedName("phoneCountryCode")
    val phoneCountryCode: String? = null,

    @field:SerializedName("email")
    val email: String? = null,

    )