package ro.westaco.carhome.data.sources.remote.responses.models

import com.google.gson.annotations.SerializedName

data class LocationV2Data(

    @field:SerializedName("data")
    val data: Data? = null,

    @field:SerializedName("success")
    val success: Boolean? = null
) {
    override fun toString(): String {
        return "LocationV2Data(data=$data, success=$success)"
    }
}

data class Data(

    @field:SerializedName("locations")
    val locations: List<LocationV2Item?>? = null,

    @field:SerializedName("groupedLocations")
    val groupedLocations: Any? = null
) {
    override fun toString(): String {
        return "Data(locations=$locations, groupedLocations=$groupedLocations)"
    }
}
