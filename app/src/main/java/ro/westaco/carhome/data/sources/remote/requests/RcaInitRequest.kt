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
