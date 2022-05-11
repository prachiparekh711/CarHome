package ro.westaco.carhome.data.sources.remote.responses.models

import com.google.gson.annotations.SerializedName

data class Country(

    @SerializedName("code") val code: String,
    @SerializedName("name") val name: String,
    @SerializedName("twoLetterCode") val twoLetterCode: String,
    @SerializedName("numericCode") val numericCode: Int,

    ) {
    override fun toString() = name

    companion object {

        fun findPositionForCode(countries: List<Country>, code: String = "ROU"): Int {
            for (c in countries.withIndex()) {
                if (c.value.code == code) {
                    return c.index
                }
            }
            return -1
        }

        fun findPositionForTwoLetterCode(countries: List<Country>, code: String = "RO"): Int {
            for (c in countries.withIndex()) {
                if (c.value.twoLetterCode == code) {
                    return c.index
                }
            }
            return -1
        }
    }
}