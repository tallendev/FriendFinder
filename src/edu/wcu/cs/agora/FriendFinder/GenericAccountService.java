package edu.wcu.cs.agora.FriendFinder;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.NetworkErrorException;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

/**
 * @author Tyler Allen
 * @created 9/28/2014
 * @version 12/8/2014
 *
 * Reference: Google docs GenericAccountService example.
 * Stubbed, most of these methods to be implemented if required/
 */
public class GenericAccountService extends Service
{
    public static final String ACCOUNT_TYPE = "edu.wcu";
    private Authenticator authenticator;

    @Override
    public void onCreate ()
    {
        authenticator = new Authenticator(this);
    }

    @Override
    public IBinder onBind (Intent intent)
    {
        return authenticator.getIBinder();
    }

    public class Authenticator extends AbstractAccountAuthenticator
    {
        public Authenticator (Context context)
        {
            super(context);
        }

        @Override
        public Bundle editProperties (AccountAuthenticatorResponse accountAuthenticatorResponse,
                                      String s)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Bundle addAccount (AccountAuthenticatorResponse accountAuthenticatorResponse,
                                  String s, String s2, String[] strings, Bundle bundle)
        throws NetworkErrorException
        {
            return null;
        }

        @Override
        public Bundle confirmCredentials (AccountAuthenticatorResponse accountAuthenticatorResponse,
                                          Account account, Bundle bundle)
        throws NetworkErrorException
        {
            return null;
        }

        @Override
        public Bundle getAuthToken (AccountAuthenticatorResponse accountAuthenticatorResponse,
                                    Account account, String s, Bundle bundle)
        throws NetworkErrorException
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getAuthTokenLabel (String s)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Bundle updateCredentials (AccountAuthenticatorResponse accountAuthenticatorResponse,
                                         Account account, String s, Bundle bundle)
        throws NetworkErrorException
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Bundle hasFeatures (AccountAuthenticatorResponse accountAuthenticatorResponse,
                                   Account account, String[] strings) throws NetworkErrorException
        {
            throw new UnsupportedOperationException();
        }
    }

}

