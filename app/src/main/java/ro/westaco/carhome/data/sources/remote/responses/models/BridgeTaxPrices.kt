package ro.westaco.carhome.data.sources.remote.responses.models

import com.google.gson.annotations.SerializedName

data class BridgeTaxPrices(

    @field:SerializedName("passTaxCategoryCode")
    val passTaxCategoryCode: String? = null,

    @field:SerializedName("monthsOfAvailability")
    val monthsOfAvailability: Int? = null,

    @field:SerializedName("paymentValue")
    val paymentValue: Double? = null,

    @field:SerializedName("productCode")
    val productCode: String? = null,

    @field:SerializedName("objectiveCode")
    val objectiveCode: String? = null,

    @field:SerializedName("quantity")
    val quantity: Int? = null,

    @field:SerializedName("originalValue")
    val originalValue: Int? = null,

    @field:SerializedName("description")
    val description: String? = null,

    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("originalCurrency")
    val originalCurrency: String? = null,

    @field:SerializedName("paymentCurrency")
    val paymentCurrency: String? = null
)
