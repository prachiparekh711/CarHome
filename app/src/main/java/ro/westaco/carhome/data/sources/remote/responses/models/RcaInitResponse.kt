package ro.westaco.carhome.data.sources.remote.responses.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class RcaInitResponse(

    @field:SerializedName("question")
    val question: Question? = null,

    @field:SerializedName("guid")
    val guid: String? = null

) : Serializable

data class Question(

    @field:SerializedName("uniqueRef")
    val uniqueRef: String? = null,

    @field:SerializedName("messageCode")
    val messageCode: String? = null,

    @field:SerializedName("messageArgs")
    val messageArgs: ArrayList<String>? = null,

    @field:SerializedName("messageText")
    val messageText: String? = null,

    @field:SerializedName("options")
    val options: ArrayList<Options>? = null,

    @field:SerializedName("defaultOption")
    val defaultOption: String? = null,

    ) : Serializable

data class Options(

    @field:SerializedName("type")
    val type: Question? = null,

    @field:SerializedName("text")
    val text: String? = null
) : Serializable
