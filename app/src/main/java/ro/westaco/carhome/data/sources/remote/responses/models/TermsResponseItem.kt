package ro.westaco.carhome.data.sources.remote.responses.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class TermsResponseItem(

    @field:SerializedName("versionId")
    val versionId: Int? = null,

    @field:SerializedName("subDocuments")
    val subDocuments: List<TermsResponseItem?>? = null,

    @field:SerializedName("title")
    val title: String? = null,

    @field:SerializedName("preferredCaption")
    val preferredCaption: String? = null,

    @field:SerializedName("version")
    val version: Double? = null,

    @field:SerializedName("mandatory")
    val mandatory: Boolean? = null,

    @field:SerializedName("allowed")
    var allowed: Boolean = false
) : Serializable
