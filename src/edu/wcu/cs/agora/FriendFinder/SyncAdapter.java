package edu.wcu.cs.agora.FriendFinder;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.*;
import android.os.Bundle;
import android.util.Log;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

/**
 * Created by tyler on 10/16/2014.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter
{
    private static final String SYNC_FINISHED = "SYNC_FINISHED";

    private final ContentResolver contentResolver;
    private       SSLContext      sslContext;

    /**
     * Creates an {@link android.content.AbstractThreadedSyncAdapter}.
     *
     * @param context the {@link android.content.Context} that this is running within.
     * @param autoInitialize if true then sync requests that have {@link
     * ContentResolver#SYNC_EXTRAS_INITIALIZE} set will be internally handled by {@link
     * android.content.AbstractThreadedSyncAdapter} by calling {@link
     * ContentResolver#setIsSyncable(android.accounts.Account,
     * String, int)} with 1 if it
     */
    public SyncAdapter (Context context, boolean autoInitialize)
    {
        super(context, autoInitialize);
        contentResolver = context.getContentResolver();
        Log.d("SYNC", "constructor called");
        try {
            KeyStore trustStore = KeyStore.getInstance("BKS");
            Log.d("SYNC", "KeyStore.getInstance");
            InputStream trustStoreStream = context.getResources().openRawResource(R.raw.truststore);
            Log.d("SYNC", "turstStoreStream init");
            trustStore.load(trustStoreStream, "hadouken!".toCharArray());
            Log.d("SYNC", "turstStoreStream load");

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            Log.d("SYNC", "trustManagerFactory getInstance");
            trustManagerFactory.init(trustStore);
            Log.d("SYNC", "trustManagerFactory init");

            sslContext = SSLContext.getInstance("TLS");
            Log.d("SYNC", "SSLContext.getInstance()");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);

        } catch (GeneralSecurityException e) {
            Log.e(this.getClass().toString(), "Exception while creating context: ", e);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        Log.d("SYNC", "constructor end");
    }

    /**
     * Creates an {@link android.content.AbstractThreadedSyncAdapter}.
     *
     * @param context the {@link android.content.Context} that this is running within.
     * @param autoInitialize if true then sync requests that have {@link
     * android.content.ContentResolver#SYNC_EXTRAS_INITIALIZE} set will be internally handled by
     * {@link android.content.AbstractThreadedSyncAdapter} by calling {@link
     * android.content.ContentResolver#setIsSyncable(android.accounts.Account, String, int)} with 1
     * if it is currently set to <0.
     * @param allowParallelSyncs if true then allow syncs for different accounts to run at the same
     * time, each in their own thread. This must be consistent with the setting
     */
    public SyncAdapter (Context context, boolean autoInitialize, boolean allowParallelSyncs)
    {
        super(context, autoInitialize);
        contentResolver = context.getContentResolver();
        Log.d("SYNC", "constructor called");
        try {
            KeyStore trustStore = KeyStore.getInstance("BKS");
            Log.d("SYNC", "KeyStore.getInstance");
            InputStream trustStoreStream = context.getResources().openRawResource(R.raw.truststore);
            Log.d("SYNC", "turstStoreStream init");
            trustStore.load(trustStoreStream, "hadouken!".toCharArray());
            Log.d("SYNC", "turstStoreStream load");

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            Log.d("SYNC", "trustManagerFactory getInstance");
            trustManagerFactory.init(trustStore);

            Log.d("SYNC", "trustManagerFactory init");

            sslContext = SSLContext.getInstance("TLS");
            Log.d("SYNC", "SSLContext.getInstance()");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);

        } catch (GeneralSecurityException e) {
            Log.e(this.getClass().toString(), "Exception while creating context: ", e);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        Log.d("SYNC", "constructor end");
    }

    /**
     * Perform a sync for this account. SyncAdapter-specific parameters may be specified in extras,
     * which is guaranteed to not be null. Invocations of this method are guaranteed to be
     * serialized.
     *
     * @param account the account that should be synced
     * @param extras SyncAdapter-specific parameters
     * @param authority the authority of this sync request
     * @param provider a ContentProviderClient that points to the ContentProvider for this
     * authority
     * @param syncResult SyncAdapter-specific parameters
     */
    @Override
    public void onPerformSync (Account account, Bundle extras, String authority,
                               ContentProviderClient provider, SyncResult syncResult)
    {
        try
        {
            Log.d("SYNC", "starting sync");
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket("www.trantracker.com",
                                                                            1337);
            OutputStream out = sslSocket.getOutputStream();
            AccountManager accountManager = (AccountManager) getContext().getSystemService
                                                             (Context.ACCOUNT_SERVICE);
            out.write(("3 " + account.name + " " +
                       accountManager.getPassword(account)).getBytes());
            out.flush();
            Log.d("SYNC", "Written");
            out.close();
            sslSocket.close();

        }
        catch (IOException ioe)
        {
            Log.d("SYNC", "An error occurred while attempting to sync");
            Log.d("SYNC", ioe.getMessage());
        }
        Intent i = new Intent(SYNC_FINISHED);
        getContext().sendBroadcast(i);
    }
}
