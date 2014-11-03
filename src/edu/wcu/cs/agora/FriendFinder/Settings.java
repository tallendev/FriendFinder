package edu.wcu.cs.agora.FriendFinder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Tyler Allen
 * 09/29/2014
 *
 * Code for functionality on the user privacy settings page.
 */

public class Settings extends Activity implements View.OnClickListener
{

    private LinearLayout button;

    /**
     * Currently the default onCreate.
     */
    @Override
    public void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        button = (LinearLayout) findViewById(R.id.priv_settings_button);
        button.setOnClickListener(this);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick (View v)
    {
        if (v.getId() == button.getId())
        {
            Intent i = new Intent(this, Privacy.class);
            startActivity(i);
        }
    }
}