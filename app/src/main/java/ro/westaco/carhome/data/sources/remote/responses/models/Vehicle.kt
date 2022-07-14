package ro.westaco.carhome.data.sources.remote.responses.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Vehicle(

    @field:SerializedName("rcaDocumentHref")
    val rcaDocumentHref: String? = null,

    @field:SerializedName("guid")
    val guid: String? = null,

    @field:SerializedName("vignetteTicketHref")
    val vignetteTicketHref: String? = null,

    @field:SerializedName("policyExpirationDate")
    val policyExpirationDate: String? = null,

    @field:SerializedName("vehicleBrandLogo")
    val vehicleBrandLogo: String? = null,

    @field:SerializedName("passTaxTransactionGuid")
    val passTaxTransactionGuid: String? = null,

    @field:SerializedName("policyStartDate")
    val policyStartDate: String? = null,

    @field:SerializedName("passTaxDocumentHref")
    val passTaxDocumentHref: String? = null,

    @field:SerializedName("vehicleBrand")
    val vehicleBrand: String? = null,

    @field:SerializedName("licensePlate")
    val licensePlate: String? = null,

    @field:SerializedName("passTaxLastPurchase")
    val passTaxLastPurchase: String? = null,

    @field:SerializedName("vignetteTransactionGuid")
    val vignetteTransactionGuid: String? = null,

    @field:SerializedName("vignetteStartDate")
    val vignetteStartDate: String? = null,

    @field:SerializedName("rcaTransactionGuid")
    val rcaTransactionGuid: String? = null,

    @field:SerializedName("model")
    val model: String? = null,

    @field:SerializedName("vignetteExpirationDate")
    val vignetteExpirationDate: String? = null,

    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("notifications")
    val notifications: Any? = null,

    @field:SerializedName("registrationCountryName")
    val registrationCountryName: String? = null,

    @field:SerializedName("registrationCountryCode")
    val registrationCountryCode: String? = null,


    ) : Serializable {
    override fun toString(): String {
        return "Vehicle(rcaDocumentHref=$rcaDocumentHref, vignetteTicketHref=$vignetteTicketHref, policyExpirationDate=$policyExpirationDate, vehicleBrandLogo=$vehicleBrandLogo, passTaxTransactionGuid=$passTaxTransactionGuid, policyStartDate=$policyStartDate, passTaxDocumentHref=$passTaxDocumentHref, vehicleBrand=$vehicleBrand, licensePlate=$licensePlate, passTaxLastPurchase=$passTaxLastPurchase, vignetteTransactionGuid=$vignetteTransactionGuid, vignetteStartDate=$vignetteStartDate, rcaTransactionGuid=$rcaTransactionGuid, model=$model, vignetteExpirationDate=$vignetteExpirationDate, id=$id, notifications=$notifications)"
    }
}