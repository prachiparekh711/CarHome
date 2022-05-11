package ro.westaco.carhome.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.CalendarContract;

import java.util.TimeZone;

public class CalendarHelper {
    private static Uri uri;
    private static Uri uri2;
    private static Context context;

    public static void MakeNewCalendarEntry(Context contex, String title, String description, long startTime, long endTime, boolean allDay, boolean hasAlarm, int calendarId, int selectedReminderValue) {
        context = contex;
        ContentResolver cr = contex.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, startTime);
        values.put(CalendarContract.Events.DTEND, endTime);
        values.put(CalendarContract.Events.TITLE, title);
        values.put(CalendarContract.Events.DESCRIPTION, description);
        values.put(CalendarContract.Events.CALENDAR_ID, calendarId);
        values.put(CalendarContract.Events.STATUS, CalendarContract.Events.STATUS_CONFIRMED);

        if (allDay) {
            values.put(CalendarContract.Events.ALL_DAY, true);
        }

        if (hasAlarm) {
            values.put(CalendarContract.Events.HAS_ALARM, true);
        }

        values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());
        uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
        long eventID = Long.parseLong(uri.getLastPathSegment());

        if (hasAlarm) {
            ContentValues reminders = new ContentValues();
            reminders.put(CalendarContract.Reminders.EVENT_ID, eventID);
            reminders.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
            reminders.put(CalendarContract.Reminders.MINUTES, selectedReminderValue);

            uri2 = cr.insert(CalendarContract.Reminders.CONTENT_URI, reminders);

        }
    }

    public static void deleteReminder() {
        context.getContentResolver().delete(uri, null, null);
    }
}