package edu.wcu.cs.agora.FriendFinder;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.widget.CalendarView;

/**
 * Created by tyler on 3/23/2015.
 */
public class Calendar extends Activity implements CalendarView.OnDateChangeListener
{
    /**
     * Calendar widget of this page.
     */
    private CalendarView calendar;

    @Override
    public void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar);
        calendar = (CalendarView) findViewById(R.id.calendar);
        calendar.setOnDateChangeListener(this);
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