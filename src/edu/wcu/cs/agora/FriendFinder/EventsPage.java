package edu.wcu.cs.agora.FriendFinder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * @author Tyler Allen
 * @created 09/29/2014
 * @version 2/1/2015
 * Code for functioning specific event instance.
 */
public class EventsPage extends Activity implements View.OnClickListener
{
    /**
     * Map button
     */
    private Button map;

    /**
     * Currently the default fragment onCreate.
     */
    @Override
    public void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.eventspage);
        map = (Button) findViewById(R.id.map);
        Intent intent = getIntent();
        TextView eventName = ((TextView) findViewById(R.id.eventname));
        eventName.setText(
                intent.getExtras().getString("event_name", eventName.getText().toString()));
        ((TextView) findViewById(R.id.date))
                .setText(intent.getExtras().getString("event_date"));

        ((TextView) findViewById(R.id.time)).setText(intent.getExtras().getString("event_time"));
        ((TextView) findViewById(R.id.description))
                .setText(intent.getExtras().getString("description"));
        map.setOnClickListener(this);
    }

    /**
     * Called when a view has been clicked. Fires map page if clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick (View v)
    {
        if (v == map)
        {
            Intent i = new Intent(this, Map.class);
            startActivity(i);
        }
    }

}