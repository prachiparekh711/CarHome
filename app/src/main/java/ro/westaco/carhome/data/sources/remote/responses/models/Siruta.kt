package ro.westaco.carhome.data.sources.remote.responses.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Siruta(

    @field:SerializedName("code")
    val code: Int? = null,

    @field:SerializedName("parentCode")
    val parentCode: Int? = null,

    @field:SerializedName("level")
    val level: Int? = null,

    @field:SerializedName("name")
    val name: String,

    @field:SerializedName("fullName")
    val fullName: String? = null,

    var selected: Boolean? = null

) : Serializable {

    fun ischecked(): Boolean? {

        return selected
    }

    fun setchecked(checked: Boolean) {

        selected = checked
    }

    override fun toString(): String {
        return name
    }
}