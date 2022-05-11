package ro.westaco.carhome.data.sources.remote.responses.models

import com.google.gson.annotations.SerializedName
import ro.westaco.carhome.data.sources.remote.requests.Address
import java.io.Serializable

data class LeasingCompany(

    @field:SerializedName("logoHref")
    val logoHref: String? = null,

    @field:SerializedName("address")
    val address: String? = null,

    @field:SerializedName("address2")
    val address2: Address? = null,

    @field:SerializedName("pin")
    val pin: String? = null,

    @field:SerializedName("name")
    val name: String,

    @field:SerializedName("id")
    var id: Int? = null
) : Serializable {
    override fun toString(): String {
        return name
    }
}
