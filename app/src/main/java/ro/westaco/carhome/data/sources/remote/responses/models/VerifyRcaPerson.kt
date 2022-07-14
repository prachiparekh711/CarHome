package ro.westaco.carhome.data.sources.remote.responses.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class VerifyRcaPerson(

    @field:SerializedName("firstName")
    val firstName: String? = null,

    @field:SerializedName("lastName")
    val lastName: String? = null,

    @field:SerializedName("logoHref")
    val logoHref: String? = null,

    @field:SerializedName("validationResult")
    val validationResult: ValidationResult? = null,

    @field:SerializedName("phone")
    val phone: String? = null,

    @field:SerializedName("fullAddress")
    val fullAddress: String? = null,

    @field:SerializedName("drivingLicenseId")
    val drivingLicenseId: String? = null,

    @field:SerializedName("guid")
    val guid: String? = null,

    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("email")
    val email: String? = null,

    @field:SerializedName("companyName")
    val companyName: String? = null,

    @field:SerializedName("noRegistration")
    val noRegistration: String? = null,


    ) : Serializable {
    override fun toString(): String {
        return "VerifyRcaPerson(firstName=$firstName, validationResult=$validationResult)"
    }
}

data class ValidationResult(

    @field:SerializedName("valid")
    val valid: Boolean? = null,

    @field:SerializedName("warnings")
    val warnings: List<WarningsItem?>? = null
) : Serializable {
    override fun toString(): String {
        return "$warnings"
    }
}

data class WarningsItem(

    @field:SerializedName("field")
    val field: String? = null,

    @field:SerializedName("warning")
    val warning: String? = null
) : Serializable {
    override fun toString(): String {
        return "$field: $warning\n"
    }
}