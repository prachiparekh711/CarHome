package ro.westaco.carhome.data.sources.remote.responses.models

import com.google.gson.annotations.SerializedName

class LocationFilterItem(
    @SerializedName("id") var nomLSId: Int,
    @SerializedName("name") var name: String,
    @SerializedName("iconHref") var iconHref: String?,
    @SerializedName("logoHref") var logoHref: String?
) {
    override fun toString(): String {
        return "LocationDataItem{" +
                "id=" + nomLSId +
                ", name='" + name + '\'' +
                '}'
    }
}