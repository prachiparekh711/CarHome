package ro.westaco.carhome.data.sources.remote.requests

import com.google.gson.annotations.SerializedName

data class DocumentCategoryRequest(

    @field:SerializedName("sourceCategories")
    val sourceCategories: List<Int?>? = null,

    @field:SerializedName("destination")
    val destination: Any? = null,

    @field:SerializedName("sourceDocuments")
    val sourceDocuments: List<Int?>? = null
) {
    override fun toString(): String {
        return "DocumentCategoryRequest(sourceCategories=$sourceCategories, destination=$destination, sourceDocuments=$sourceDocuments)"
    }
}
