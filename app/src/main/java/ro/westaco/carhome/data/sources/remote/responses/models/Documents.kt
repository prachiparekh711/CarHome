package ro.westaco.carhome.data.sources.remote.responses.models

import com.google.gson.annotations.SerializedName

data class Documents(

    @field:SerializedName("total")
    val total: Int? = null,

    @field:SerializedName("records")
    val records: Int? = null,

    @field:SerializedName("page")
    val page: Int? = null,

    @field:SerializedName("rows")
    val rows: List<RowsItem>? = null
)

data class RowsItem(

    @field:SerializedName("thumbnailHref")
    val thumbnailHref: Any? = null,

    @field:SerializedName("fileSize")
    val fileSize: Int? = null,

    @field:SerializedName("name")
    val name: String? = null,

    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("mimeType")
    val mimeType: String? = null,

    @field:SerializedName("href")
    val href: String? = null,

    @field:SerializedName("uploadedDate")
    val uploadedDate: String? = null,

    @field:SerializedName("customId")
    val customId: String? = null,

    @field:SerializedName("categoryId")
    val categoryId: Int? = null,

    @field:SerializedName("tagNames")
    val tagNames: List<Any?>? = null
)
