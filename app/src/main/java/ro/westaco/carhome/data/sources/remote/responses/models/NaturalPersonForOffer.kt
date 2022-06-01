package ro.westaco.carhome.data.sources.remote.responses.models

import com.google.gson.annotations.SerializedName
import ro.westaco.carhome.data.sources.remote.requests.Address
import ro.westaco.carhome.data.sources.remote.requests.DrivingLicense
import ro.westaco.carhome.data.sources.remote.requests.IdentityDocument
import java.io.Serializable

data class NaturalPersonForOffer(
    @SerializedName("lastName") val lastName: String? = null,
    @SerializedName("address") val address: Address? = null,
    @SerializedName("identityDocument") val identityDocument: IdentityDocument? = null,
    @SerializedName("cnp") val cnp: String? = null,
//    @SerializedName("identityDocumentAttachment") val identityDocumentAttachment: Attachments? = null,
    @SerializedName("employerName") val employerName: String? = null,
    @SerializedName("dateOfBirth") val dateOfBirth: String? = null,
    @SerializedName("firstName") val firstName: String? = null,
    @SerializedName("occupationCorIsco08") val occupationCorIsco08: CatalogItem? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("phoneCountryCode") val phoneCountryCode: String? = null,
    @SerializedName("drivingLicense") val drivingLicense: DrivingLicense? = null,
    @SerializedName("id") val id: Int? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("guid") var guid: String? = null
) : Serializable