package ro.westaco.carhome.data.sources.remote.responses.models

import com.google.gson.annotations.SerializedName

data class TransactionData(

    @field:SerializedName("availabilityEndDate")
    val availabilityEndDate: String? = null,

    @field:SerializedName("durationDescription")
    val durationDescription: String? = null,

    @field:SerializedName("quantityDescription")
    val quantityDescription: String? = null,

    @field:SerializedName("ticket")
    val ticket: Ticket? = null,

    @field:SerializedName("availabilityStartDate")
    val availabilityStartDate: String? = null,

    @field:SerializedName("transactionNo")
    val transactionNo: String? = null,

    @field:SerializedName("vehicleBrandName")
    val vehicleBrandName: Any? = null,

    @field:SerializedName("transactionDate")
    val transactionDate: String? = null,

    @field:SerializedName("vehicleModelName")
    val vehicleModelName: Any? = null,

    @field:SerializedName("vehicleLpn")
    val vehicleLpn: String? = null,

    @field:SerializedName("statusDescription")
    val statusDescription: String? = null,

    @field:SerializedName("vehicleLogoHref")
    val vehicleLogoHref: String? = null,

    @field:SerializedName("price")
    val price: Double? = null,

    @field:SerializedName("guid")
    val guid: String? = null,

    @field:SerializedName("currency")
    val currency: String? = null,

    @field:SerializedName("invoice")
    val invoice: Invoice? = null,

    @field:SerializedName("status")
    val status: Int? = null
) {
    override fun toString(): String {
        return "TransactionData(availabilityEndDate=$availabilityEndDate, durationDescription=$durationDescription, ticket=$ticket, availabilityStartDate=$availabilityStartDate, transactionNo=$transactionNo, vehicleBrandName=$vehicleBrandName, transactionDate=$transactionDate, vehicleModelName=$vehicleModelName, vehicleLpn=$vehicleLpn, statusDescription=$statusDescription, vehicleLogoHref=$vehicleLogoHref, price=$price, guid=$guid, currency=$currency, invoice=$invoice, status=$status)"
    }
}

data class Invoice(

    @field:SerializedName("name")
    val name: String? = null,

    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("mimeType")
    val mimeType: String? = null,

    @field:SerializedName("href")
    val href: String? = null,

    @field:SerializedName("uploadedDate")
    val uploadedDate: String? = null
)
