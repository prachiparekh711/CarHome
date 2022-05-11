package ro.westaco.carhome.data.sources.remote.responses.models

import com.google.gson.annotations.SerializedName

data class ValidateVehicle(

    @field:SerializedName("valid")
    val valid: Boolean? = null,

    @field:SerializedName("fieldsToBeCollected")
    val fieldsToBeCollected: List<String?>? = null
)
