package ro.westaco.carhome.data.sources.remote.responses.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Caen(

    @field:SerializedName("code")
    val code: String? = null,

    @field:SerializedName("name")
    val name: String
) : Serializable {
    override fun toString(): String {
        return name
    }
}
