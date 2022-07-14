package ro.westaco.carhome.data.sources.remote.responses.models

import com.google.gson.annotations.SerializedName

data class ValidateVehicle(

    @field:SerializedName("valid")
    val valid: Boolean? = null,

    @field:SerializedName("warnings")
    val warnings: List<WarningsItem?>? = null
)
