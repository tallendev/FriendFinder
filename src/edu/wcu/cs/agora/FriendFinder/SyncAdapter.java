package edu.wcu.cs.agora.FriendFinder;

import android.accounts.Account;
import android.content.*;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import javax.net.ssl.*;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by tyler on 10/16/2014.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter
{
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
        // Load CAs from an InputStream
        // (could be from a resource or ByteArrayInputStream or ...)
        try {
            KeyStore trustStore = KeyStore.getInstance("BKS");
            InputStream trustStoreStream = context.getResources().openRawResource(R.raw.truststore);
            trustStore.load(trustStoreStream, "hadouken!".toCharArray());

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);

            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
        } catch (GeneralSecurityException e) {
            Log.e(this.getClass().toString(), "Exception while creating context: ", e);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

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
        super(context, autoInitialize, allowParallelSyncs);
        contentResolver = context.getContentResolver();
        Log.d("SYNC", "constructor called");
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
            Log.d("SYNC", "starting");
            URL url = new URL("https://www.trantracker.com:1337");
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setSSLSocketFactory(sslContext.getSocketFactory());
            //URLConnection urlConnection =  url.openConnection();
            Log.d("SYNC", "Success connecting to server.");
            //urlConnection.setDoInput(true);
            // in = urlConnection.getInputStream();
           // in.close();
           // urlConnection.setDoInput(false);
            urlConnection.setDoOutput(true);
            OutputStream out = urlConnection.getOutputStream();
            Log.d("SYNC", "Do we live this long?");
            out.write("Hello World\n".getBytes());
            out.flush();
            Log.d("SYNC", "Written");
            out.close();
            urlConnection.setDoOutput(false);
        }
        catch (IOException ioe)
        {
            Log.d("SYNC", "An error occurred while attempting to sync");
            Log.d("SYNC", ioe.getMessage());
        }
    }
}
