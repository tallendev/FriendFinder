package edu.wcu.cs.agora.FriendFinder;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.*;
import android.os.Bundle;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Scanner;

/**
 * Created by tyler on 10/16/2014.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter
{
    private static final String SYNC_FINISHED = "SYNC_FINISHED";

    private final ContentResolver contentResolver;
    private       SSLContext      sslContext;
    private static final int DEFAULT_PORT = 1337;

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
        try {
            KeyStore trustStore = KeyStore.getInstance("BKS");
            InputStream trustStoreStream = context.getResources().openRawResource(R.raw.truststore);
            trustStore.load(trustStoreStream, "hadouken!".toCharArray());

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);

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
        boolean authenticated = false;
        boolean ioError = false;
        try
        {
            Log.d("SYNC", "starting sync");
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            SSLSocket sslSocket = (SSLSocket) sslSocketFactory
                    .createSocket("www.trantracker.com", DEFAULT_PORT);
            OutputStream out = sslSocket.getOutputStream();
            AccountManager accountManager = (AccountManager) getContext()
                    .getSystemService(Context.ACCOUNT_SERVICE);
            JSONObject json = new JSONObject();

            json.put("request_type", "3");
            json.put("user", account.name);
            json.put("password", accountManager.getPassword(account));
            int i = 0;
            String current;
            while((current = extras.getString("table" + i, null)) != null)
            {
                json.put("table" + i, current);
                i++;
            }
            Log.d("SYNC", "CURRENT:\n" + current);
            Log.d("SYNC", "JSON dump:\n" + json);
            out.write(json.toString().getBytes());
            out.flush();
            Log.d("SYNC", "Written");
            JSONObject jsonIn = null;
            Scanner in = new Scanner(sslSocket.getInputStream());
            Log.d("SYNC", "Made Socket");
            Log.d("SYNC", "in.hasNext()");
            //if (in.hasNextLine())
            //{
                jsonIn = new JSONObject(in.nextLine());
                Log.d("SYNC", "new JSONObject");
                authenticated = jsonIn.getBoolean("authenticated");

                i = 0;
                current = null;
                String lines[] = null;
                while (((current = jsonIn.getString("table" + i)) != null)) {
                    lines = current.split("\\r?\\n");
                    String table = lines[0];
                    ContentValues vals = new ContentValues();
                    for (int j = 1; j < lines.length; j++) {
                        String entries[] = lines[i].split(" ");
                        for (String entry : entries) {
                            //vals.put("entry" + j, )
                        }
                        //provider.insert(ServerContentProvider.CONTENT_URI + table, )
                    }

                }
                Log.d("SYNC", "Read");
            /*}
            else
            {
                Log.d("SYNC", "Error reading from server.");
            }*/
            out.close();
            sslSocket.close();

        }
        catch (IOException ioe)
        {
            Log.d("SYNC", "An error occurred while attempting to sync");
            Log.d("SYNC", ioe.getMessage());
            ioError = true;
        } catch (JSONException e)
        {
            Log.d("SYNC", "JSON error:\n" + e.getMessage());
            e.printStackTrace();
        }
        Log.d("SYNC", "first_sync: " + extras.getBoolean("first_sync"));
        if (extras.getBoolean("first_sync", false))
        {
            Log.d("SYNC", "Attempting to broadcast");
            Intent i = new Intent(SYNC_FINISHED);
            i.putExtra("success", authenticated);
            i.putExtra("net_issue", ioError);
            i.setAction("first_sync");
            getContext().sendBroadcast(i);
        }
    }
}