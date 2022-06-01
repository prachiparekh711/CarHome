package ro.westaco.carhome.data.sources.remote.responses.models

import com.google.gson.annotations.SerializedName

data class SectionModel(
    @SerializedName("category") var category: String,
    @SerializedName("filters") var filters: ArrayList<LocationFilterItem>
)