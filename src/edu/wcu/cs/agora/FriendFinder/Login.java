package edu.wcu.cs.agora.FriendFinder;

import android.app.Activity;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import com.google.android.gms.identity.intents.AddressConstants;

/**
 * Tyler Allen
 * 09/29/2014
 *
 * Code for functionality on the log in page.
 */
public class Login extends Activity
{
    private String AUTHORITY;

    /**
     * Called when the activity is first created. Boilerplate code.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        Log.d("LOGIN", "OnCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        AUTHORITY = getResources().getString(R.string.authority);
        Intent intent = new Intent(this, SyncService.class);

        startService(intent);
        startService(new Intent(this, GenericAccountService.class));
        ContentResolver.setSyncAutomatically(GenericAccountService.getAccount(), AUTHORITY, true);


        Bundle extras = new Bundle();
        Runnable r = new Runnable()
        {
            @Override
            public void run ()
            {
                getContentResolver().requestSync(GenericAccountService.getAccount(), AUTHORITY, extras);
                Log.d("LOGIN", "Sync Requested");
            }
        };
        Handler handler = new Handler();
        handler.postDelayed(r, 5000);
    }
}
