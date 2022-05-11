package ro.westaco.carhome.data.sources.remote.responses.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class RcaOfferResponse(

    @field:SerializedName("offers")
    val offers: List<OffersItem?>? = null,

    @field:SerializedName("warning")
    val warning: String? = null

) : Serializable

data class OffersItem(


    @field:SerializedName("priceDs")
    val priceDs: Double? = null,

    @field:SerializedName("insurerNameShort")
    val insurerNameShort: String? = null,

    @field:SerializedName("insurerNameLong")
    val insurerNameLong: String? = null,

    @field:SerializedName("code")
    val code: String? = null,

    @field:SerializedName("messageFromProvider")
    val messageFromProvider: String? = null,

    @field:SerializedName("price")
    val price: Double? = null,

    @field:SerializedName("bmClass")
    val bmClass: String? = null,

    @field:SerializedName("description")
    val description: String? = null,

    @field:SerializedName("currency")
    val currency: String? = null,

    @field:SerializedName("insurerCode")
    val insurerCode: String? = null,

    @field:SerializedName("enabled")
    val enabled: Boolean? = null,

    @field:SerializedName("insurerLogoHref")
    val insurerLogoHref: String? = null

) : Serializable
