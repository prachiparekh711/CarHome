package ro.westaco.carhome.data.sources.remote.responses.models

import com.google.gson.annotations.SerializedName

class LocationFilterItem(
    @SerializedName("nomLSId") var nomLSId: Int,
    @SerializedName("name") var name: String
) {
    override fun toString(): String {
        return "LocationDataItem{" +
                "nomLSId=" + nomLSId +
                ", name='" + name + '\'' +
                '}'
    }
}