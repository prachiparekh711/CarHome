package ro.westaco.carhome.data.sources.remote.responses.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class HistoryItem(

    @field:SerializedName("logoHref")
    val logoHref: String? = null,

    @field:SerializedName("amount")
    val amount: Double? = null,

    @field:SerializedName("transactionGuid")
    val transactionGuid: String? = null,

    @field:SerializedName("service")
    val service: String? = null,

    @field:SerializedName("vehicleBrandName")
    val vehicleBrandName: String? = null,

    @field:SerializedName("currency")
    val currency: String? = null,

    @field:SerializedName("serviceName")
    val serviceName: String? = null,

    @field:SerializedName("transactionDate")
    val transactionDate: String? = null,

    @field:SerializedName("vehicleLpn")
    val vehicleLpn: String? = null,

    @field:SerializedName("ticketSeries")
    val ticketSeries: String? = null,

    @field:SerializedName("vehicle")
    val vehicle: String? = null,

    @field:SerializedName("status")
    val status: Int? = null
) : Serializable
