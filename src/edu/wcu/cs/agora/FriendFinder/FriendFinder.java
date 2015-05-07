package edu.wcu.cs.agora.FriendFinder;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Application;
import android.content.Context;
import android.util.Log;

/**
 * @author Tyler Allen
 * @created 11/29/14
 * @version 12/08/2014
 * Used for application-wide data and static methods.
 */
public class FriendFinder extends Application
{
    /**
     * Create a new account for the sync adapter
     *
     * @param context The application context
     */
    public static Account createSyncAccount (Context context, String username, String password)
    {
        Account newAccount = new Account(username, "edu.wcu");
        // Get an instance of the Android account manager
        AccountManager accountManager = (AccountManager) context.getSystemService(ACCOUNT_SERVICE);
        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise error.
         */
        if (accountManager.addAccountExplicitly(newAccount, password, null))
        {
            Log.d("LOGIN", "Successfully created account.");
        }
        else
        {
            Log.d("LOGIN", "Error creating account, probably already exists");
        }

        return newAccount;
    }
}
