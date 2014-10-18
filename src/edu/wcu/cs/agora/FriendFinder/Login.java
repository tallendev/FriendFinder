package edu.wcu.cs.agora.FriendFinder;

import android.app.Activity;
import android.content.ContentProviderClient;
import android.os.Bundle;

/**
 * Tyler Allen
 * 09/29/2014
 *
 * Code for functionality on the log in page.
 */
public class Login extends Activity
{
    public final static String AUTHORITY = "edu.wcu.cs.agora.FriendFinder";

    /**
     * Called when the activity is first created. Boilerplate code.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        getContentResolver().requestSync(GenericAccountService.getAccount(), AUTHORITY, null);
    }
}
