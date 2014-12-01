package edu.wcu.cs.agora.FriendFinder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Created by tyler on 11/27/14.
 */
public class GroupPage extends Activity
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grouppage);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        TextView eventName = ((TextView) findViewById(R.id.groupname));
        eventName.setText(extras.getString("group_name", eventName.getText().toString()));
        ((TextView) findViewById(R.id.group_description)).setText(extras.getString("group_description"));
    }
}