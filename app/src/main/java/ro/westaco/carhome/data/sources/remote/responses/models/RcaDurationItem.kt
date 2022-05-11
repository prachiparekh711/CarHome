package ro.westaco.carhome.data.sources.remote.responses.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class RcaDurationItem(

    @field:SerializedName("unit")
    val unit: String? = null,

    @field:SerializedName("unitCount")
    val unitCount: Int? = null,

    @field:SerializedName("name")
    val name: String? = null,

    @field:SerializedName("id")
    val id: Int
) : Serializable {
    override fun toString(): String {
        return "$name"
    }
}
