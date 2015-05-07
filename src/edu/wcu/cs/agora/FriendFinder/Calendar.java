package edu.wcu.cs.agora.FriendFinder;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.text.format.DateUtils;
import android.widget.CalendarView;

import java.util.Date;

/**
 * @author Tyler Allen
 * @created 3/23/2015
 * @version 3/24/2015
 *
 * This file contains the logic for communicating calendar updates to the backend database.
 */

public class Calendar extends Activity implements CalendarView.OnDateChangeListener
{
    /** Delay for calendar updates.. */
    public static final long     FIVE_MINUTES    = 300000;
    /** The database location of the Calendar table. */
    public static final Uri      CALENDAR_URI    = Uri
            .parse("content://com.android.calendar/instances/when");
    /** The begin date/time and end date/time constants. */
    public static final String[] CALENDAR_FIELDS = {CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END};
    /**
     * Last time the calendar was synced.
     */
    private static long         lastCalled;
    /**
     * Calendar widget of this page.
     */
    private        CalendarView calendar;
    private        Account      account;

    /**
     * Initialize our calendar.
     * @param savedInstanceState not used
     */
    @Override
    public void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        account = ((AccountManager) getSystemService(Context.ACCOUNT_SERVICE))
                .getAccountsByType(GenericAccountService.ACCOUNT_TYPE)[0];
        setContentView(R.layout.calendar);
        calendar = (CalendarView) findViewById(R.id.calendar);
        calendar.setOnDateChangeListener(this);
        if (savedInstanceState == null)
        {
            lastCalled = 0;
        }
    }

    /**
     * Synchronizes with server if necessary.
     */
    @Override
    protected void onResume ()
    {
        super.onResume();
        if (calendar.getDate() - lastCalled > FIVE_MINUTES)
        {
            lastCalled = calendar.getDate();
            Bundle extras = new Bundle();

            // generate sync request based on search parameters.
            extras.putString("request_type", "11");
            extras.putString("events", getEvents());
            extras.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
            extras.putBoolean(ContentResolver.SYNC_EXTRAS_FORCE, true);
            extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
            ContentResolver.requestSync(account, getString(R.string.authority), extras);
        }
    }

    /**
     * Gets all the events from google calendar.
     * @return A string representing all google calendar events.
     */
    private String getEvents ()
    {
        Uri.Builder builder = Uri.parse("content://com.android.calendar/instances/when")
                                 .buildUpon();
        long now = new Date().getTime();

        ContentUris.appendId(builder, now);
        ContentUris.appendId(builder, now + DateUtils.DAY_IN_MILLIS);

        Cursor cursor = getContentResolver()
                .query(builder.build(), CALENDAR_FIELDS, null, null, null);
        String string = "";
        while (cursor.moveToNext())
        {
            string += (cursor.getString(0) + "," + cursor.getString(1) + ";");
        }
        cursor.close();
        return string;
    }

    /**
     * Called upon change of the selected day.
     *
     * @param view The view associated with this listener.
     * @param year The year that was set.
     * @param month The month that was set [0-11].
     * @param dayOfMonth The day of the month that was set.
     */
    @Override
    public void onSelectedDayChange (CalendarView view, int year, int month, int dayOfMonth)
    {
        Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
        builder.appendPath("time");
        ContentUris.appendId(builder, view.getDate());
        Intent intent = new Intent(Intent.ACTION_VIEW).setData(builder.build());
        startActivity(intent);
    }
}