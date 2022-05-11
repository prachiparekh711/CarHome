package ro.westaco.carhome.data.sources.remote.responses.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class NaturalPerson(

    @field:SerializedName("firstName")
    val firstName: String? = null,

    @field:SerializedName("lastName")
    val lastName: String? = null,

    @field:SerializedName("logoHref")
    val logoHref: String? = null,

    @field:SerializedName("phone")
    val phone: String? = null,

    @field:SerializedName("drivingLicenseId")
    val drivingLicenseId: String? = null,

    @field:SerializedName("id")
    val id: Long? = null,

    @field:SerializedName("email")
    val email: String? = null,

    @field:SerializedName("fullAddress")
    var fullAddress: String? = null,

    @SerializedName("guid") var guid: String? = null,

    ) : Serializable {
    override fun toString(): String {
        return "NaturalPerson(firstName=$firstName, lastName=$lastName, logoHref=$logoHref, phone=$phone, drivingLicenseId=$drivingLicenseId, id=$id, email=$email, fullAddress=$fullAddress)"
    }
}