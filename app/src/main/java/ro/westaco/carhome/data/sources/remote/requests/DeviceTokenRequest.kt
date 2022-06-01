package ro.westaco.carhome.data.sources.remote.requests

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class DeviceTokenRequest(
    @field:SerializedName("deviceToken")
    val deviceToken: String? = null

) : Serializable