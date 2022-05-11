package ro.westaco.carhome.data.sources.remote.responses.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Categories(

    @field:SerializedName("name")
    val name: String,

    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("parentId")
    val parentId: Int? = null,

    @field:SerializedName("createdDate")
    val createdDate: String? = null,

    @field:SerializedName("size")
    val size: Int? = null
) : Serializable {
    override fun toString(): String {
        return name
    }
}
