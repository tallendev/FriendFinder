package edu.wcu.cs.agora.FriendFinder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

/**
 * @author Tyler Allen
 * @created 11/27/14
 * @version 12/07/2014
 *
 * Class containing setup and interaction of the GroupPage.
 */
public class GroupPage extends Activity
{
    /**
     * Attempts to overwrite default page text settings with group-specific ones.
     *
     * @param savedInstanceState not used
     */
    @Override
    public void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grouppage);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        TextView groupName = ((TextView) findViewById(R.id.groupname));
        groupName.setText(extras.getString("group_name", groupName.getText().toString()));
        ((TextView) findViewById(R.id.group_description))
                .setText(extras.getString("group_description"));
    }
}