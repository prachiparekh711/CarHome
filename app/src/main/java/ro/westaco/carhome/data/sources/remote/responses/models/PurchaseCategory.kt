package ro.westaco.carhome.data.sources.remote.responses.models

import com.google.gson.annotations.SerializedName

data class PurchaseCategory(
    @SerializedName("category") val category: String?,
    @SerializedName("purchases") val purchases: ArrayList<Purchase>?,
)