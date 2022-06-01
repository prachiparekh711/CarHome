package ro.westaco.carhome.utils

import android.content.Context
import android.text.format.DateFormat
import ro.westaco.carhome.R
import java.text.SimpleDateFormat
import java.util.*

object DateTimeUtils {

    fun convertToServerDate(context: Context, dateStr: String?): String? {
        if (dateStr.isNullOrEmpty()) return null
        val originalFormat =
            SimpleDateFormat(context.getString(R.string.date_format_template), Locale.US)
        val targetFormat =
            SimpleDateFormat(context.getString(R.string.server_date_format_template), Locale.US)
        val date = originalFormat.parse(dateStr)
        return targetFormat.format(date)
    }

    fun convertFromServerDate(context: Context, dateStr: String?): String? {
        if (dateStr == null) return null
        val originalFormat =
            SimpleDateFormat(context.getString(R.string.server_date_format_template), Locale.US)
        val targetFormat =
            SimpleDateFormat(context.getString(R.string.date_format_template), Locale.US)
        val date = originalFormat.parse(dateStr)
        return targetFormat.format(date)
    }

    fun convertDate(
        dateStr: String?,
        originalFormat: SimpleDateFormat,
        targetFormat: SimpleDateFormat,
    ): String? {
        if (dateStr == null) return null
        val date = originalFormat.parse(dateStr)
        return targetFormat.format(date)
    }

    fun isSameDay(date1: Date?, date2: Date?): Boolean {
        require(!(date1 == null || date2 == null)) { "The dates must not be null" }
        val cal1 = Calendar.getInstance()
        cal1.time = date1
        val cal2 = Calendar.getInstance()
        cal2.time = date2
        return isSameDay(cal1, cal2)
    }

    fun isSameDay(cal1: Calendar?, cal2: Calendar?): Boolean {
        require(!(cal1 == null || cal2 == null)) { "The dates must not be null" }
        return cal1[Calendar.ERA] === cal2[Calendar.ERA] && cal1[Calendar.YEAR] === cal2[Calendar.YEAR] && cal1[Calendar.DAY_OF_YEAR] === cal2[Calendar.DAY_OF_YEAR]
    }

    fun getNowSeconds() = System.currentTimeMillis() / 1000L

    fun getTitleDate(neededTimeMilis: Long, context: Context, needsToday: Boolean): String {
        val nowTime = Calendar.getInstance()
        val neededTime = Calendar.getInstance()
        neededTime.timeInMillis = neededTimeMilis
        return if (neededTime[Calendar.YEAR] === nowTime[Calendar.YEAR]) {
            if (neededTime[Calendar.MONTH] === nowTime[Calendar.MONTH]) {
                if (neededTime[Calendar.DATE] - nowTime[Calendar.DATE] === 1 && needsToday) {
                    //here return like "Tomorrow at 12:00"
                    context.resources.getString(R.string.tomorrow)
                } else if (nowTime[Calendar.DATE] === neededTime[Calendar.DATE] && needsToday) {
                    //here return like "Today at 12:00"
                    context.resources.getString(R.string.today)
                } else if (nowTime[Calendar.DATE] - neededTime[Calendar.DATE] === 1 && needsToday) {
                    //here return like "Yesterday at 12:00"
                    context.resources.getString(R.string.yesterday)
                } else {
                    //here return like "May 31, 12:00"
//                    DateFormat.format("MMMM", neededTime).toString()
                    context.resources.getString(R.string.this_month)
                }
            } else {
                //here return like "May 31, 12:00"
                DateFormat.format("MMMM", neededTime).toString()
            }
        } else {
            //here return like "May 31 2010, 12:00" - it's a different year we need to show it
            DateFormat.format("MMMM yyyy", neededTime).toString()
        }
    }

}