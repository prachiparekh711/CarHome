package ro.westaco.carhome.data.sources.remote.responses.models

import com.google.gson.annotations.SerializedName

class Timetable {
    @SerializedName("timeEnd")
    var timeEnd: String? = null

    @SerializedName("locationStatus")
    var locationStatus: String? = null

    @SerializedName("timeStart")
    var timeStart: String? = null

    @SerializedName("isHoliday")
    var isHoliday = false

    @SerializedName("isNonstop")
    var isNonstop = false

    @SerializedName("id")
    var id = 0

    @SerializedName("dayOfWeekTo")
    var dayOfWeekTo = 0

    @SerializedName("dayOfWeekFrom")
    var dayOfWeekFrom = 0
    override fun toString(): String {
        return "Timetable{" +
                "timeEnd = '" + timeEnd + '\'' +
                ",locationStatus = '" + locationStatus + '\'' +
                ",timeStart = '" + timeStart + '\'' +
                ",isHoliday = '" + isHoliday + '\'' +
                ",isNonstop = '" + isNonstop + '\'' +
                ",id = '" + id + '\'' +
                ",dayOfWeekTo = '" + dayOfWeekTo + '\'' +
                ",dayOfWeekFrom = '" + dayOfWeekFrom + '\'' +
                "}"
    }
}