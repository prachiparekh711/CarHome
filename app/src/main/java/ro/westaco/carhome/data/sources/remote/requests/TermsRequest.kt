package ro.westaco.carhome.data.sources.remote.requests

import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class TermsRequestItem(

    @field:SerializedName("versionId")
    val versionId: Int? = null,

    @field:SerializedName("accepted")
    val accepted: Boolean? = null
) : Serializable
