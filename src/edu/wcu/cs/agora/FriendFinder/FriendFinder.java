package edu.wcu.cs.agora.FriendFinder;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Application;
import android.content.Context;
import android.util.Log;

/**
 * Created by tyler on 11/29/14.
 * stub for future global info
 */
public class FriendFinder extends Application
{
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
                (AccountManager) context.getSystemService(ACCOUNT_SERVICE);
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
}
