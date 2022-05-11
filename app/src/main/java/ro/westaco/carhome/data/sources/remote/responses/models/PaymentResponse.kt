package ro.westaco.carhome.data.sources.remote.responses.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class PaymentResponse(
    @SerializedName("guid") val guid: String?,
    @SerializedName("html") val html: String?,
    @SerializedName("warnings") val warnings: List<String>?,
) : Serializable {
    override fun toString(): String {
        return "PaymentResponse(guid=$guid, html=$html, warnings=$warnings)"
    }
}