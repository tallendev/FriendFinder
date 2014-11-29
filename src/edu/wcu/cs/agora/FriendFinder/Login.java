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
 * Tyler Allen
 * Karen Dana
 *
 * 09/29/2014
 *
 * Code for functionality on the log in page.
 */
public class Login extends Activity implements View.OnClickListener
{
    private String AUTHORITY;
    private Button loginButton;
    private Button registerButton;
    private SharedPreferences sharedPreferences;
    private Account account;
    private String user;
    private String pass;
    private BroadcastReceiver receiver;
    private LoadingSpinnerDialog spinnerDialog;

    /**
     * Called when the activity is first created. Boilerplate code.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
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

        AUTHORITY = getResources().getString(R.string.authority);
        Intent intent = new Intent(this, SyncService.class);

        startService(intent);
        startService(new Intent(this, GenericAccountService.class));

        sharedPreferences = this.getSharedPreferences(getString(R.string.shared_prefs),
                                                      Context.MODE_PRIVATE);
        String user = sharedPreferences.getString("user", null);
        spinnerDialog = new LoadingSpinnerDialog();

        if (null != user)
        {
            nextScreen();
        }
        Log.d("LOGIN", "End on_create");
    }

    private void nextScreen()
    {
        if (spinnerDialog.isVisible())
        {
            spinnerDialog.dismiss();
        }
        Intent intent = new Intent(this, Home.class);
        startActivity(intent);
        if (receiver != null)
        {
            unregisterReceiver(receiver);
            receiver = null;
        }
        finish();
    }

    /**
     * Create a new dummy account for the sync adapter
     *
     * @param context The application context
     */
    public static Account createSyncAccount(Context context, String username, String password)
    {
        // Create the account type and default account
        Account newAccount = new Account(username, "edu.wcu");
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(
                        ACCOUNT_SERVICE);
        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
        if (accountManager.addAccountExplicitly(newAccount, password, null))
        {
            Log.d("LOGIN", "Successfully created account.");
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call context.setIsSyncable(account, authority, 1)
             * here.
             */
        }
        else
        {
            Log.d("LOGIN", "Error creating account, probably already exists");
            /*
             * The account exists or some other error occurred. Log this, report it,
             * or handle it internally.
             */
        }

        return newAccount;
    }

    // should possibly be onDestroy
    @Override
    protected void onStop ()
    {
        super.onDestroy();
        if (receiver != null)
        {
            if (account != null)
            {
                AccountManager accountManager = (AccountManager) this.getSystemService(ACCOUNT_SERVICE);
                accountManager.removeAccount(account, null, null);
            }
            if (receiver != null)
            {
                unregisterReceiver(receiver);
                receiver = null;
            }
        }
    }

    @Override
    public void onClick(View view)
    {
        Log.d("LOGIN", "onclick\nview.getId(): " + view.getId() + "\nvR.id.login: " + R.id.login);
        switch (view.getId())
        {
            case (R.id.login):
                //get username and password
                user = String.valueOf(((EditText) findViewById(R.id.email)).getText());
                pass = String.valueOf(((EditText) findViewById(R.id.pass)).getText());
                if (user != null && pass != null && !user.isEmpty() && !pass.isEmpty())
                {
                    account = createSyncAccount(this, user, pass);
                    Bundle extras = new Bundle();
                    extras.putBoolean("first_sync", true);
                    receiver = new AuthenticationChecker();

                    IntentFilter intentFilter = new IntentFilter();
                    intentFilter.addAction("first_sync");
                    registerReceiver(receiver, intentFilter);
                    ContentResolver.setSyncAutomatically(account, AUTHORITY, true);

                    ContentResolver.requestSync(account, AUTHORITY, extras);
                    Log.d("LOGIN", "SYNC_REQUESTED");
                    spinnerDialog.show(getFragmentManager(), "Attempting to Log In");

                }
                else
                {
                    Toast.makeText(this, "Please enter a valid user name and password.",
                                   Toast.LENGTH_LONG).show();
                }
                break;
            case (R.id.register):
                Intent intent = new Intent(this, Register.class);
                startActivity(intent);
                break;
        }
    }

    public class AuthenticationChecker extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            Bundle extras = intent.getExtras();
            AccountManager accountManager = (AccountManager) context.getSystemService(ACCOUNT_SERVICE);
            //TODO: refactor me to be actual true value not string
            Log.d("AUTH_CHECKER", "Checking authentication validity");
            if (extras != null)
            {
                if (extras.getBoolean("success"))
                {
                    sharedPreferences.edit().putString("user", user).putString("password",
                                                                                pass).apply();
                    nextScreen();
                }
                else
                {
                    // FIXME this is a race condition when screen tilts... needs to be handled
                    if (account != null)
                    {
                        accountManager.removeAccount(account, null, null);
                    }
                    account = null;
                    user = null;
                    pass = null;
                    //fixme dialog would be better
                    if (extras.getBoolean("net_issue"))
                    {
                        Toast.makeText(context, "Issue connecting to server", Toast.LENGTH_LONG)
                             .show();
                    }
                    else
                    {
                        Toast.makeText(context, "Invalid login information", Toast.LENGTH_LONG)
                             .show();
                    }
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
    }
}
