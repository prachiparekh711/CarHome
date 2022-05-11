package ro.westaco.carhome.data.sources.remote.responses.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class LocationV2Item(

    @field:SerializedName("country")
    val country: String? = null,

    @field:SerializedName("distance")
    val distance: Double? = null,

    @field:SerializedName("postalCode")
    val postalCode: String? = null,

    @field:SerializedName("latitude")
    val latitude: Double? = null,

    @field:SerializedName("locality")
    val locality: String? = null,

    @field:SerializedName("services")
    val services: String? = null,

    @field:SerializedName("isActive")
    val isActive: Boolean? = null,

    @field:SerializedName("streetName")
    val streetName: String? = null,

    @field:SerializedName("serviceIds")
    val serviceIds: String? = null,

    @field:SerializedName("phone")
    val phone: Any? = null,

    @field:SerializedName("brandId")
    val brandId: Int? = null,

    @field:SerializedName("name")
    val name: String? = null,

    @field:SerializedName("fullAddress")
    val fullAddress: String? = null,

    @field:SerializedName("openNow")
    val openNow: Boolean? = null,

    @field:SerializedName("guid")
    val guid: String? = null,

    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("region")
    val region: String? = null,

    @field:SerializedName("email")
    val email: String? = null,

    @field:SerializedName("longitude")
    val longitude: Double? = null,

    @SerializedName("timetable")
    var timetable: Timetable? = null
) : Serializable {
    override fun toString(): String {
        return "LocationV2Item(country=$country, distance=$distance, postalCode=$postalCode, latitude=$latitude, locality=$locality, services=$services, isActive=$isActive, streetName=$streetName, serviceIds=$serviceIds, phone=$phone, brandId=$brandId, name=$name, fullAddress=$fullAddress, openNow=$openNow, guid=$guid, id=$id, region=$region, email=$email, longitude=$longitude)"
    }
}
