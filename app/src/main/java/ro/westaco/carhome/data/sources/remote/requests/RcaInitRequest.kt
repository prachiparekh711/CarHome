package ro.westaco.carhome.data.sources.remote.requests

import com.google.gson.annotations.SerializedName
import ro.westaco.carhome.data.sources.remote.responses.models.OffersItem

data class RcaInitRequest(

    @field:SerializedName("offer")
    val offer: OffersItem? = null,

    @field:SerializedName("answer")
    val answer: Answer? = null,

    @field:SerializedName("ds")
    val ds: Boolean? = null

)

data class Answer(
    @field:SerializedName("questionUniqueRef")
    val questionUniqueRef: String? = null,

    @field:SerializedName("selectedOption")
    val selectedOption: String? = null,
)

data class OfferItems(

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

)
