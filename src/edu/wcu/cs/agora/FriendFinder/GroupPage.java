package edu.wcu.cs.agora.FriendFinder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * @author Tyler Allen
 * @created 11/27/14
 * @version 12/07/2014
 *
 * Class containing setup and interaction of the GroupPage.
 */
public class GroupPage extends Activity implements View.OnClickListener
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
        ((Button) findViewById(R.id.members)).setOnClickListener(this);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick (View v)
    {
        if (v.equals(findViewById(R.id.members)))
        {
            Intent i = new Intent(this, MemberList.class);
            i.putExtra("group_name", getIntent().getExtras().getString("group_name"));
            startActivity(i);
        }
    }
}