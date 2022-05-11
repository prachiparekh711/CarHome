package ro.westaco.carhome.data.sources.remote.requests

import com.google.gson.annotations.SerializedName
import ro.westaco.carhome.data.sources.remote.responses.models.CatalogItem
import java.io.Serializable

data class Address(
    @field:SerializedName("zipCode")
    val zipCode: String? = null,
    @field:SerializedName("streetType")
    val streetType: CatalogItem? = null,
    @field:SerializedName("sirutaCode")
    val sirutaCode: Int? = null,
    @field:SerializedName("locality")
    val locality: String? = null,
    @field:SerializedName("streetName")
    val streetName: String? = null,
    @field:SerializedName("addressDetail")
    val addressDetail: String? = null,
    @field:SerializedName("buildingNo")
    val buildingNo: String? = null,
    @field:SerializedName("countryCode")
    val countryCode: String? = null,
    @field:SerializedName("block")
    val block: String? = null,
    @field:SerializedName("region")
    val region: String? = null,
    @field:SerializedName("entrance")
    val entrance: String? = null,
    @field:SerializedName("floor")
    val floor: String? = null,
    @field:SerializedName("apartment")
    val apartment: String? = null
) : Serializable

