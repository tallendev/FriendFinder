package edu.wcu.cs.agora.FriendFinder;

import android.app.Activity;
import android.os.Bundle;

/**
 * @author Tyler Allen
 * @created 09/29/2014
 * @version 12/8/2014
 * Code for functionality on the user privacy settings page.
 */

public class Privacy extends Activity
{
    /**
     * Currently the default onCreate.
     */
    @Override
    public void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.privacy_settings);
    }
}