package ro.westaco.carhome.data.sources.remote.requests

import com.google.gson.annotations.SerializedName
import ro.westaco.carhome.data.sources.remote.responses.models.CatalogItem


data class AddProfileDataRequest(

    @field:SerializedName("firstName")
    val firstName: String? = null,

    @field:SerializedName("lastName")
    val lastName: String? = null,

    @field:SerializedName("occupationCorIsco08")
    val occupationCorIsco08: CatalogItem? = null,

    @field:SerializedName("address")
    val address: Address? = null,

    @field:SerializedName("identityDocument")
    val identityDocument: IdentityDocument? = null,

    @field:SerializedName("cnp")
    val cnp: String? = null,

    @field:SerializedName("phone")
    val phone: String? = null,

    @field:SerializedName("drivingLicense")
    val drivingLicense: DrivingLicense? = null,

    @field:SerializedName("employerName")
    val employerName: String? = null,

    @field:SerializedName("dateOfBirth")
    val dateOfBirth: String? = null,

    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("email")
    val email: String? = null
)