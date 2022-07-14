package ro.westaco.carhome.data.sources.remote.responses.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class SectionModel(
    @SerializedName("category") var category: String,
    @SerializedName("filters") var filters: ArrayList<LocationFilterItem>
) : Serializable {
    override fun toString(): String {
        var filterString = ""
        filters.forEach {
            filterString += it.name + ", "
        }
        return filterString
    }
}