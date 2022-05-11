package ro.westaco.carhome.data.sources.remote.requests

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class IdentityDocument(

    @SerializedName("number")
    val number: String? = null,

    @SerializedName("documentType")
    val documentType: DocumentType? = null,

    @SerializedName("series")
    val series: String? = null,

    @SerializedName("expirationDate")
    val expirationDate: String? = null
) : Serializable

data class DocumentType(

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("id")
    val id: Int? = null
) : Serializable
