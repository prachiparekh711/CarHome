package ro.westaco.carhome.data.sources.remote.requests

import com.google.gson.annotations.SerializedName

data class PaymentRequest(

    @field:SerializedName("invoicePersonGuid")
    val invoicePersonGuid: String,

    @field:SerializedName("transactionGuid")
    val transactionGuid: String
) {
    override fun toString(): String {
        return "PaymentRequest(invoicePersonGuid=$invoicePersonGuid, transactionGuid='$transactionGuid')"
    }
}
