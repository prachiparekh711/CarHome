package ro.westaco.carhome.data.sources.remote.responses.models

import com.google.gson.annotations.SerializedName

data class ProgressItem(

    @field:SerializedName("profileProgress")
    val profileProgress: ProfileProgress? = null,

    @field:SerializedName("vehicleProgress")
    val vehicleProgress: List<VehicleProgressItem>? = null
)

data class ProfileProgress(

    @field:SerializedName("progressStepsDone")
    val progressStepsDone: Int? = null,

    @field:SerializedName("description")
    val description: String? = null,

    @field:SerializedName("completionPercent")
    val completionPercent: Int? = null,

    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("progressStepsTotal")
    val progressStepsTotal: Int? = null
)

data class VehicleProgressItem(

    @field:SerializedName("progressStepsDone")
    val progressStepsDone: Int? = null,

    @field:SerializedName("description")
    val description: String? = null,

    @field:SerializedName("completionPercent")
    val completionPercent: Int? = null,

    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("progressStepsTotal")
    val progressStepsTotal: Int? = null
) {
    override fun toString(): String {
        return "VehicleProgressItem(progressStepsDone=$progressStepsDone, description=$description, completionPercent=$completionPercent, id=$id, progressStepsTotal=$progressStepsTotal)"
    }
}
