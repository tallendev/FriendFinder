package edu.wcu.cs.agora.FriendFinder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

/**
 * @author Tyler Allen
 * @created 09/29/2014
 * @version 12/8/2014
 * Code for functioning specific event instance.
 */
public class EventsPage extends Activity
{

    /**
     * Currently the default fragment onCreate.
     */
    @Override
    public void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.eventspage);
        Intent intent = getIntent();
        TextView eventName = ((TextView) findViewById(R.id.eventname));
        eventName.setText(
                intent.getExtras().getString("event_name", eventName.getText().toString()));
        ((TextView) findViewById(R.id.timedate))
                .setText(intent.getExtras().getString("event_date") + "   " +
                         intent.getExtras().getString("event_time"));
    }
}