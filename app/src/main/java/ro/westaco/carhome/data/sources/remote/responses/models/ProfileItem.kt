package ro.westaco.carhome.data.sources.remote.responses.models

import android.graphics.Bitmap
import com.google.gson.annotations.SerializedName
import ro.westaco.carhome.data.sources.remote.requests.Address
import ro.westaco.carhome.data.sources.remote.requests.DrivingLicense
import ro.westaco.carhome.data.sources.remote.requests.IdentityDocument
import java.io.Serializable

data class ProfileItem(
    @SerializedName("lastName") val lastName: String? = null,
    @SerializedName("address") val address: Address? = null,
    @SerializedName("identityDocument") val identityDocument: IdentityDocument? = null,
    @SerializedName("cnp") val cnp: String? = null,
    @SerializedName("identityDocumentAttachment") val identityDocumentAttachment: Attachments? = null,
    @SerializedName("employerName") val employerName: String? = null,
    @SerializedName("dateOfBirth") val dateOfBirth: String? = null,
    @SerializedName("drivingLicenseAttachment") val drivingLicenseAttachment: Attachments? = null,
    @SerializedName("firstName") val firstName: String? = null,
    @SerializedName("occupationCorIsco08") val occupationCorIsco08: CatalogItem? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("drivingLicense") val drivingLicense: DrivingLicense? = null,
    @SerializedName("id") val id: Int? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("logo") var logo: Bitmap? = null,
    @SerializedName("progressStepsDone") val stepDone: Int? = null,
    @SerializedName("progressStepsTotal") val stepTotal: Int? = null,

    ) : Serializable {
    override fun toString(): String {
        return "ProfileItem(lastName=$lastName, address=$address, identityDocument=$identityDocument, cnp=$cnp, identityDocumentAttachment=$identityDocumentAttachment, employerName=$employerName, dateOfBirth=$dateOfBirth, drivingLicenseAttachment=$drivingLicenseAttachment, firstName=$firstName, occupationCorIsco08=$occupationCorIsco08, phone=$phone, drivingLicense=$drivingLicense, id=$id, email=$email, logo=$logo, stepDone=$stepDone, stepTotal=$stepTotal)"
    }
}

