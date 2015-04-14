package edu.wcu.cs.agora.FriendFinder;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.*;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * @author Tyler Allen
 * @created 09/29/2014
 * @version 12/8/2014
 * Code for functionality on the log in page.
 */
public class Login extends Activity implements View.OnClickListener
{
    /**
     * Authority for this application.
     */
    private static final String AUTHORITY = ServerContentProvider.AUTHORITY;
    /**
     * The login button.
     */
    private Button               loginButton;
    /**
     * The register button.
     */
    private Button               registerButton;
    /**
     * Shared preferences for storing user credentials.
     */
    private SharedPreferences    sharedPreferences;
    /**
     * The current user account while it is being created.
     */
    private Account              account;
    /**
     * User's username from input field.
     */
    private String               user;
    /**
     * User's password from the input field.
     */
    private String               pass;
    /**
     * Broadcast receiver for confirming successful authentication.
     */
    private BroadcastReceiver    receiver;
    /**
     * A spinning dialog to indicate synchronization with server.
     */
    private LoadingSpinnerDialog spinnerDialog;

    /**
     * Called when the activity is first created. Handles assigning fields and setting up
     * action listeners.
     */
    @Override
    public void onCreate (Bundle savedInstanceState)
    {
        Log.d("LOGIN", "OnCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        user = null;
        pass = null;
        receiver = null;
        //set handler for login and register buttons
        loginButton = (Button) findViewById(R.id.login);
        registerButton = (Button) findViewById(R.id.register);
        loginButton.setOnClickListener(this);
        registerButton.setOnClickListener(this);

        Intent intent = new Intent(this, SyncService.class);

        startService(intent);
        startService(new Intent(this, GenericAccountService.class));

        sharedPreferences = this
                .getSharedPreferences(getString(R.string.shared_prefs), Context.MODE_PRIVATE);
        String user = sharedPreferences.getString("user", null);
        spinnerDialog = new LoadingSpinnerDialog();

        if (null != user)
        {
            nextScreen();
        }
        Log.d("LOGIN", "End on_create");
    }

    /**
     * Helper method for entering the next screen. Ensures receivers are unregistered.
     */
    private void nextScreen ()
    {
        if (spinnerDialog.isVisible())
        {
            spinnerDialog.dismiss();
        }
        if (receiver != null)
        {
            unregisterReceiver(receiver);
            receiver = null;
        }
        Intent intent = new Intent(this, Home.class);
        ContentProviderClient client = getContentResolver().acquireContentProviderClient(ServerContentProvider.AUTHORITY);
        ServerContentProvider provider = (ServerContentProvider) client.getLocalContentProvider();
        provider.resetDatabase();
        client.release();

        startActivity(intent);
        finish();
    }


    /**
     * Verifies that unconfirmed accounts and receivers are unregistered and deleted.
     */
    @Override
    protected void onStop ()
    {
        super.onStop();
        if (receiver != null)
        {
            if (account != null)
            {
                AccountManager accountManager = (AccountManager) this
                        .getSystemService(ACCOUNT_SERVICE);
                accountManager.removeAccount(account, null, null);
            }
            unregisterReceiver(receiver);
        }
    }

    /**
     * Handles button clicks for sign up and login.
     *
     * @param view The button that was clicked.
     */
    @Override
    public void onClick (View view)
    {
        Log.d("LOGIN", "onclick\nview.getId(): " + view.getId() + "\nvR.id.login: " + R.id.login);
        switch (view.getId())
        {
            case (R.id.login):
                onClickLogin();
                break;
            case (R.id.register):
                Intent intent = new Intent(this, Register.class);
                startActivity(intent);
                break;
        }
    }

    /**
     * Helper method for processing onClick for a login button.
     */
    private void onClickLogin ()
    {
        //get username and password
        user = String.valueOf(((EditText) findViewById(R.id.email)).getText());
        pass = String.valueOf(((EditText) findViewById(R.id.pass)).getText());
        if (user != null && pass != null && !user.isEmpty() && !pass.isEmpty())
        {
            account = FriendFinder.createSyncAccount(this, user, pass);
            Bundle extras = new Bundle();
            extras.putString("request_type", "3");
            extras.putBoolean("first_sync", true);
            receiver = new AuthenticationChecker();

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("first_sync");
            registerReceiver(receiver, intentFilter);
            ContentResolver.setSyncAutomatically(account, AUTHORITY, true);
            extras.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
            extras.putBoolean(ContentResolver.SYNC_EXTRAS_FORCE, true);
            extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
            ContentResolver.requestSync(account, AUTHORITY, extras);
            Log.d("LOGIN", "SYNC_REQUESTED");
            spinnerDialog.show(getFragmentManager(), "Attempting to Log In");

        }
        else
        {
            Toast.makeText(this, "Please enter a valid user name and password.", Toast.LENGTH_LONG)
                 .show();
        }
    }

    /**
     * Authenticates user attempting to login. This class is a broadcast receiver because it takes
     * action on a successful login after a successful synchronization is completed.
     */
    public class AuthenticationChecker extends BroadcastReceiver
    {
        /**
         * Checks if the authentication was successful. If so, we move on to the next screen.
         * Otherwise, we have to cleanup and let the user try again.
         *
         * @param context Current application's context.
         * @param intent Intent received from class making callback.
         */
        @Override
        public void onReceive (Context context, Intent intent)
        {
            Bundle extras = intent.getExtras();
            //TODO: refactor me to be actual true value not string
            Log.d("AUTH_CHECKER", "Checking authentication validity");
            if (extras != null)
            {
                if (extras.getBoolean("success"))
                {
                    sharedPreferences.edit().putString("user", user).putString("password", pass)
                                     .apply();
                    nextScreen();
                }
                else
                {
                    onAuthFail(context, extras);
                }
            }
            else
            {
                Log.d("LOGIN", "Unhandled server connection, fixme");
            }
            if (receiver != null)
            {
                unregisterReceiver(receiver);
                receiver = null;
            }
        }

        /**
         * Helper method for onReceive to handle an authentication failure.
         *
         * @param context Activity's context.
         * @param extras Contains information about why the authentication was failed.
         */
        private void onAuthFail (Context context, Bundle extras)
        {
            AccountManager manager = (AccountManager) context.getSystemService(ACCOUNT_SERVICE);
            // FIXME this is a race condition when screen tilts... needs to be handled
            if (account != null)
            {
                manager.removeAccount(account, null, null);
            }
            account = null;
            user = null;
            pass = null;
            //fixme dialog would be better
            if (extras.getBoolean("net_issue"))
            {
                Toast.makeText(context, "Issue connecting to server", Toast.LENGTH_LONG).show();
            }
            else
            {
                Toast.makeText(context, "Invalid login information", Toast.LENGTH_LONG).show();
            }
        }
    }
}
