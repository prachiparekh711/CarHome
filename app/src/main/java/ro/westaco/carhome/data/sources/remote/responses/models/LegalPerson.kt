package ro.westaco.carhome.data.sources.remote.responses.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class LegalPerson(

    @field:SerializedName("noRegistration")
    val noRegistration: String? = null,

    @field:SerializedName("country")
    val country: String? = null,

    @field:SerializedName("logoHref")
    val logoHref: String? = null,

    @field:SerializedName("addressDetail")
    val addressDetail: String? = null,

    @field:SerializedName("companyName")
    val companyName: String? = null,

    @field:SerializedName("locality")
    val locality: String? = null,

    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("fullAddress")
    var fullAddress: String? = null,

    @SerializedName("guid") var guid: String? = null,
) : Serializable {
    override fun toString(): String {
        return "LegalPerson(noRegistration=$noRegistration, country=$country, logoHref=$logoHref, addressDetail=$addressDetail, companyName=$companyName, locality=$locality, id=$id, fullAddress=$fullAddress, guid=$guid)"
    }
}
