package edu.wcu.cs.agora.FriendFinder;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
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
    private static final String REGISTRATION = "REGISTRATION";

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
        Log.d("SYNC", "Extras: " + extras);
        try
        {
            Log.d("SYNC", "starting sync");
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            SSLSocket sslSocket = (SSLSocket) sslSocketFactory
                    .createSocket("www.trantracker.com", DEFAULT_PORT);
            OutputStream out = sslSocket.getOutputStream();
            AccountManager accountManager = (AccountManager) getContext()
                    .getSystemService(Context.ACCOUNT_SERVICE);
            Log.d("SYNC", "Extras: " + extras);
            String requestType = extras.getString("request_type", "3");
            JSONObject json = new JSONObject();
            json.put("request_type", requestType);
            json.put("user", account.name);
            json.put("password", accountManager.getPassword(account));
            switch (requestType)
            {
                case "0":
                {
                    processRegistration(json, extras, out, sslSocket);
                    break;
                }
                case "3":
                {
                    processSync(json, provider, out, extras, sslSocket);
                    break;
                }
                default:
                {
                    Log.d("SYNC", "Invalid request type.");
                }
            }
            Log.d("SYNC", "Read");
            out.close();
            sslSocket.close();
        }
        catch (IOException ioe)
        {
            Log.d("SYNC", "An error occurred while attempting to sync");
            Log.d("SYNC", ioe.getMessage());
        } catch (JSONException e)
        {
            Log.d("SYNC", "JSON error:\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void processRegistration(JSONObject json, Bundle extras, OutputStream out, SSLSocket sslSocket) throws JSONException
    {
        boolean ioError = false;
        json.put("birthday", extras.getString("birthday"));
        json.put("gender", extras.getString("gender"));
        json.put("name", extras.getString("name"));
        JSONObject jsonIn = null;
        try
        {
            out.write(json.toString().getBytes());
            Scanner in = new Scanner(sslSocket.getInputStream());
            jsonIn = new JSONObject(in.nextLine());
        }
        catch (IOException ioe)
        {
            Log.d("SYNC", "An error occurred while attempting to register");
            Log.d("SYNC", ioe.getMessage());
            ioError = true;
        }
        Log.d("SYNC", "Attempting to broadcast");
        Intent i = new Intent(REGISTRATION);
        if (jsonIn != null)
        {
            i.putExtra("success", jsonIn.getBoolean("success"));
        }
        i.putExtra("ioerr", ioError);
        i.setAction("registration");
        getContext().sendBroadcast(i);
    }

    private void processSync(JSONObject json, ContentProviderClient provider,
                                OutputStream out, Bundle extras, SSLSocket sslSocket) throws JSONException
    {
        boolean authenticated = false;
        boolean ioError = false;
        try
        {
            json.put("search", extras.getString("search", null));
            int i = 0;
            String current;
            while ((current = extras.getString("table" + i, null)) != null) {
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

            jsonIn = new JSONObject(in.nextLine());
            Log.d("SYNC", "jsonIn:\n" + jsonIn);
            Log.d("SYNC", "new JSONObject");
            authenticated = jsonIn.getBoolean("authenticated");

            i = 0;
            current = null;
            String lines[] = null;
            Log.d("SYNC", "STARTING JSON PARSE");
            while (((jsonIn.has("table" + i)))) {

                Log.d("SYNC", "LINE " + i);
                current = jsonIn.getString("table" + i);
                lines = current.split("~");
                String table = lines[0];

                try {
                    provider.delete(Uri.parse(ServerContentProvider.CONTENT_URI + "/" + table), null, null);
                } catch (RemoteException e) {
                    Log.d("SYNC", "Remote exception caught deleting table values.");
                }

                for (String line : lines) {
                    Log.d("SYNC", "LINE: " + line);
                }
                Log.d("SYNC", "TABLE: " + table);
                for (int j = 1; j < lines.length; j++) {
                    ContentValues vals = new ContentValues();
                    String entries[] = lines[j].split(",");
                    for (String entry : entries) {
                        Log.d("ENTRY", entry);
                    }
                    for (int k = 0; k < entries.length; k++) {
                        String[] val = entries[k].split("=");
                        vals.put(val[0].toUpperCase(), val[1]);
                    }
                    try {
                        provider.insert(Uri.parse(ServerContentProvider.CONTENT_URI + "/" + table), vals);
                    } catch (RemoteException e) {
                        Log.d("SYNC", "Remote Exception:\n" + e.getMessage());
                    }
                    //provider.insert(ServerContentProvider.CONTENT_URI + table, )
                }
                i++;
            }
        }
        catch (IOException ioe)
        {
            Log.d("SYNC", "An error occurred while attempting to sync");
            Log.d("SYNC", ioe.getMessage());
            ioError = true;
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