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
 * @author Tyler Allen
 * @created 10/16/2014
 * @version 12/08/2014
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter
{
    /**
     * SSL Type
     */
    private static final String SSL_TYPE      = "TLS";
    /**
     * Supported keystore type.
     */
    private static final String KEYSTORE_TYPE = "BKS";
    /**
     * Keystore password.
     */
    private static final String KEY_PASS      = "hadouken!";
    /**
     * Broadcast flags for finished synchronization.
     */
    private static final String SYNC_FINISHED = "SYNC_FINISHED";
    /**
     * Broadcast flag for finished registration.
     */
    private static final String REGISTRATION  = "REGISTRATION";
    /**
     * Broadcast flag for group update.
     */
    private static final String GROUP_UPDATE  = "GROUP_UPDATE";
    /**
     * Broadcast flag for group update.
     */
    private static final String EVENT_UPDATE  = "EVENT_UPDATE";
    /**
     * Hostname of server to contact.
     */
    private static final String HOSTNAME      = "75.143.177.14";//"www.trantracker.com";
    /**
     * Default server port used by this application.
     */
    private static final int    DEFAULT_PORT  = 1337;
    /**
     * SSLContext used to generate SSLSockets from our keystores.
     */
    private SSLContext sslContext;

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
        this(context, autoInitialize, false);
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
        try
        {
            KeyStore trustStore = KeyStore.getInstance(KEYSTORE_TYPE);
            //open and load our trustStore
            InputStream trustStoreStream = context.getResources().openRawResource(R.raw.truststore);
            trustStore.load(trustStoreStream, KEY_PASS.toCharArray());

            //Initialize our trustmanagerfactory object.
            TrustManagerFactory trustManagerFactory = TrustManagerFactory
                    .getInstance(KeyManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);

            //get a context and initialize it using our TrustManager factory.
            sslContext = SSLContext.getInstance(SSL_TYPE);
            Log.d("SYNC", "SSLContext.getInstance()");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);

        }
        catch (GeneralSecurityException e)
        {
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
     * serialized. This section of code handles a lot of different input/output pathways and could
     * use some severe refactoring.
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
        OutputStream out = null;
        SSLSocket sslSocket = null;
        Log.d("SYNC", "Extras: " + extras);
        try
        {
            Log.d("SYNC", "starting sync");
            // Create SSL socket factory.
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            // Generate SSL socket
            sslSocket = (SSLSocket) sslSocketFactory.createSocket(HOSTNAME, DEFAULT_PORT);
            sslSocket.setSoTimeout(3000);
            // get an output stream
            out = sslSocket.getOutputStream();
            // Get accountManager to access our current account.
            AccountManager accountManager = (AccountManager) getContext()
                    .getSystemService(Context.ACCOUNT_SERVICE);
            Log.d("SYNC", "Extras: " + extras);
            // SYNC is default requestType
            String requestType = extras.getString("request_type", "-1");
            //validate a valid sync request.
            if (!requestType.equals("-1"))
            {
                // fill outgoing JSON object with request information.
                buildOutput(out, sslSocket, account, requestType, extras, accountManager, provider);
                Log.d("SYNC", "Read");
            }
        }
        catch (IOException ioe)
        {
            Log.d("SYNC", "An error occurred while attempting to sync");
            Log.d("SYNC", ioe.getMessage());
        }
        catch (JSONException e)
        {
            Log.d("SYNC", "JSON error:\n" + e.getMessage());
            e.printStackTrace();
        }
        finally
        {
            adapterCleanup(out, sslSocket);
        }
    }

    /**
     * Helper method for directing output.
     *
     * @param out Output stream for communication with the server.
     * @param sslSocket Socket for communicating with the server.
     * @param account Current account.
     * @param requestType The type of request we are making to the server.
     * @param extras Any extras containing server request values from calling activity.
     * @param manager AccountManager used for retrieving auxiliary account information.
     * @param provider Used in creation of a table URI to dynamically request tables from the
     * content provider.
     *
     * @throws JSONException
     */
    private void buildOutput (OutputStream out, SSLSocket sslSocket, Account account,
                              String requestType, Bundle extras, AccountManager manager,
                              ContentProviderClient provider) throws JSONException
    {
        // start packing JSON object.
        JSONObject json = new JSONObject();
        json.put("request_type", requestType);
        json.put("user", account.name);
        json.put("password", manager.getPassword(account));
        switch (requestType)
        {
            case "0": //case REGISTER:
            {
                processRegistration(json, extras, out, sslSocket);
                break;
            }
            case "1": //case GROUP_UPDATE:
            {
                processGroupUpdate(json, extras, out, sslSocket);
                break;
            }
            case "3": //case SYNC
            {
                processSync(json, provider, out, extras, sslSocket);
                break;
            }
            case "5": //case EVENT_UPDATE
            {
                processEventUpdate(json, extras, out, sslSocket);
                break;
            }
            case "6":
            {
                processGroupDelete(json, extras, out, sslSocket);
            }
            case "7":
            {
                processEventCancel(json, extras, out, sslSocket);
            }
            case "8":
            {
                processGroupJoinLeave(json, extras, out, sslSocket);
            }
            case "9":
            {
                processEventJoinLeave(json, extras, out, sslSocket);
            }
            default:
            {
                Log.d("SYNC", "Invalid request type.");
            }
        }
    }

    /**
     * Cleans up output streams and SSL sockets associated with the server.
     *
     * @param out Output stream used for connecting with server.
     * @param sslSocket The sslSocket we are using to communicate with the server.
     */
    private void adapterCleanup (OutputStream out, SSLSocket sslSocket)
    {
        try
        {
            if (out != null)
            {
                out.close();
            }
            if (sslSocket != null)
            {
                sslSocket.close();
            }
        }
        catch (IOException e)
        {
            Log.d("SYNC", "Error cleaning up connection.");
            e.printStackTrace();
        }
    }

    /**
     * Process a registration request.
     *
     * @param json Stores outgoing data.
     * @param extras Contains extras from calling activity containing information to send to
     * server.
     * @param out Output stream connected to server.
     * @param sslSocket SSLSocket maintaining connection to server.
     *
     * @throws JSONException
     */
    private void processRegistration (JSONObject json, Bundle extras, OutputStream out,
                                      SSLSocket sslSocket) throws JSONException
    {
        boolean ioError = false;
        // more data into our outgoing json object.
        json.put("birthday", extras.getString("birthday"));
        json.put("gender", extras.getString("gender"));
        json.put("name", extras.getString("name"));
        JSONObject jsonIn = null;
        try
        {
            // write and read
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

        // did we succeed?
        if (jsonIn != null)
        {
            i.putExtra("success", jsonIn.getBoolean("success"));
        }
        i.putExtra("ioerr", ioError);
        i.setAction("registration");
        getContext().sendBroadcast(i);
    }

    /**
     * Process a group update request.
     *
     * @param json Stores outgoing data.
     * @param extras Contains extras from calling activity containing information to send to
     * server.
     * @param out Output stream connected to server.
     * @param sslSocket SSLSocket maintaining connection to server.
     *
     * @throws JSONException
     */
    private void processGroupUpdate (JSONObject json, Bundle extras, OutputStream out,
                                     SSLSocket sslSocket) throws JSONException
    {
        boolean ioError = false;
        // more data into our outgoing json object.
        json.put("groupname", extras.getString("groupname"));
        json.put("groupdesc", extras.getString("groupdesc"));
        json.put("create", extras.getBoolean("create"));
        JSONObject jsonIn = null;
        try
        {
            // write and read
            Log.d("SYNC", json.toString());
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
        Intent i = new Intent(GROUP_UPDATE);

        // did we succeed?
        if (jsonIn != null)
        {
            i.putExtra("success", jsonIn.getBoolean("success"));
        }
        i.putExtra("ioerr", ioError);
        i.setAction("group_update");
        getContext().sendBroadcast(i);
    }

    /**
     * Contains information about a synchronization request.
     *
     * @param json Used to store outgoing data.
     * @param provider Used to create URIs for dynamically storing and receiving data from the
     * content provider.
     * @param out Output stream to server.
     * @param extras Extras received from calling class containing the request information.
     * @param sslSocket Socket maintaining connection to server.
     *
     * @throws JSONException
     */
    private void processSync (JSONObject json, ContentProviderClient provider, OutputStream out,
                              Bundle extras, SSLSocket sslSocket) throws JSONException
    {
        boolean authenticated = false;
        boolean ioError = false;
        try
        {
            if (extras.containsKey("group_name"))
            {
                json.put("group_name", extras.getString("group_name"));
            }
            // add search parameters
            json.put("search", extras.getString("search", null));
            int i = 0;
            String current;
            // iterate through received tables.
            while ((current = extras.getString("table" + i, null)) != null)
            {
                // add tables to outgoing request.
                json.put("table" + i, current);
                Log.d("SYNC", "CURRENT: " + json.getString("table" + i));
                i++;
            }
            // write away
            out.write(json.toString().getBytes());
            out.flush();
            Log.d("SYNC", "FLUSH DATA");
            Scanner in = new Scanner(sslSocket.getInputStream());
            if (in.hasNextLine())
            {
                JSONObject jsonIn = new JSONObject(in.nextLine());
                authenticated = jsonIn.getBoolean("authenticated");
                Log.d("SYNC", jsonIn.toString());
                syncDatabaseInsertions(jsonIn, provider);
            }
            else
            {
                Log.d("SYNC", "Error with server connection.");
                ioError = true;
            }
        }
        catch (IOException ioe)
        {
            Log.d("SYNC", "An error occurred while attempting to sync");
            Log.d("SYNC", ioe.getMessage());
            ioError = true;
        }
        syncBroadcast(extras, authenticated, ioError);
    }

    /**
     * Process a group update request.
     *
     * @param json Stores outgoing data.
     * @param extras Contains extras from calling activity containing information to send to
     * server.
     * @param out Output stream connected to server.
     * @param sslSocket SSLSocket maintaining connection to server.
     *
     * @throws JSONException
     */
    private void processEventUpdate (JSONObject json, Bundle extras, OutputStream out,
                                     SSLSocket sslSocket) throws JSONException
    {
        boolean ioError = false;
        // more data into our outgoing json object.
        json.put("eventname", extras.getString("eventname"));
        json.put("description", extras.getString("description"));
        json.put("time", extras.getString("time"));
        json.put("date", extras.getString("date"));
        json.put("location", extras.getString("location"));
        if (extras.containsKey("id"))
        {
            json.put("id", extras.getString("id"));
        }
        json.put("create", extras.getBoolean("create"));
        JSONObject jsonIn = null;
        try
        {
            // write and read
            Log.d("SYNC", json.toString());
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
        Intent i = new Intent(EVENT_UPDATE);

        // did we succeed?
        if (jsonIn != null)
        {
            i.putExtra("success", jsonIn.getBoolean("success"));
        }
        i.putExtra("ioerr", ioError);
        i.setAction("event_update");
        getContext().sendBroadcast(i);
    }

    /**
     * Helper method for deletion of group.
     *
     * @param json Stores outgoing data.
     * @param extras Contains extras from calling activity containing information to send to
     * server.
     * @param out Output stream connected to server.
     * @param sslSocket SSLSocket maintaining connection to server.
     */
    private void processGroupDelete (JSONObject json, Bundle extras, OutputStream out,
                                     SSLSocket sslSocket) throws JSONException
    {
        boolean ioError = false;
        // more data into our outgoing json object.
        json.put("groupname", extras.getString("groupname"));
        JSONObject jsonIn = null;
        try
        {
            // write and read
            Log.d("SYNC", json.toString());
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
        Intent i = new Intent(GROUP_UPDATE);

        // did we succeed?
        if (jsonIn != null)
        {
            i.putExtra("success", jsonIn.getBoolean("success"));
        }
        i.putExtra("ioerr", ioError);
        i.setAction("group_update");
        getContext().sendBroadcast(i);
    }

    /**
     * Helper method for deletion of event.
     *
     * @param json Stores outgoing data.
     * @param extras Contains extras from calling activity containing information to send to
     * server.
     * @param out Output stream connected to server.
     * @param sslSocket SSLSocket maintaining connection to server.
     */
    private void processEventCancel (JSONObject json, Bundle extras, OutputStream out,
                                     SSLSocket sslSocket) throws JSONException
    {
        boolean ioError = false;
        // more data into our outgoing json object.
        json.put("id", extras.getString("id"));
        JSONObject jsonIn = null;
        try
        {
            // write and read
            Log.d("SYNC", json.toString());
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
        Intent i = new Intent(EVENT_UPDATE);

        // did we succeed?
        if (jsonIn != null)
        {
            i.putExtra("success", jsonIn.getBoolean("success"));
        }
        i.putExtra("ioerr", ioError);
        i.setAction("event_update");
        getContext().sendBroadcast(i);
    }

    /**
     * Process a group join/leave request.
     *
     * @param json Stores outgoing data.
     * @param extras Contains extras from calling activity containing information to send to
     * server.
     * @param out Output stream connected to server.
     * @param sslSocket SSLSocket maintaining connection to server.
     *
     * @throws JSONException
     */
    private void processGroupJoinLeave (JSONObject json, Bundle extras, OutputStream out,
                                        SSLSocket sslSocket) throws JSONException
    {
        boolean ioError = false;
        // more data into our outgoing json object.
        json.put("group_name", extras.getString("group_name"));
        Log.d("SYNC", "group_name: " + extras.getString("group_name"));
        json.put("joining", extras.getBoolean("joining"));
        JSONObject jsonIn = null;
        try
        {
            // write and read
            Log.d("SYNC", json.toString());
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
        Intent i = new Intent(GROUP_UPDATE);

        // did we succeed?
        if (jsonIn != null)
        {
            i.putExtra("success", jsonIn.getBoolean("success"));
        }
        i.putExtra("ioerr", ioError);
        i.setAction("group_update");
        getContext().sendBroadcast(i);
    }

    /**
     * Process a group join/leave request.
     *
     * @param json Stores outgoing data.
     * @param extras Contains extras from calling activity containing information to send to
     * server.
     * @param out Output stream connected to server.
     * @param sslSocket SSLSocket maintaining connection to server.
     *
     * @throws JSONException
     */
    private void processEventJoinLeave (JSONObject json, Bundle extras, OutputStream out,
                                        SSLSocket sslSocket) throws JSONException
    {
        boolean ioError = false;
        // more data into our outgoing json object.
        json.put("joining", extras.getBoolean("joining"));
        json.put("id", extras.getString("id"));
        Log.d("SYNC", "id: " + extras.getString("id"));
        JSONObject jsonIn = null;
        try
        {
            // write and read
            Log.d("SYNC", json.toString());
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
        Intent i = new Intent(EVENT_UPDATE);

        // did we succeed?
        if (jsonIn != null)
        {
            i.putExtra("success", jsonIn.getBoolean("success"));
        }
        i.putExtra("ioerr", ioError);
        i.setAction("event_update");
        getContext().sendBroadcast(i);
    }

    /**
     * Helper method for inserting data received from server into content provider.
     *
     * @param jsonIn Received JSONObject
     * @param provider Provider used to generate URIs for ContentProvider tables.
     *
     * @throws JSONException
     */
    private void syncDatabaseInsertions (JSONObject jsonIn, ContentProviderClient provider)
            throws JSONException
    {
        String current;
        int i = 0;
        String lines[] = null;
        // for each table
        while (((jsonIn.has("table" + i))))
        {
            // parse table.
            current = jsonIn.getString("table" + i);
            lines = current.split("~");
            String table = lines[0];
            if (table.equals("user_group_self"))
            {
                table = "user_group";
            }
            // currently we delete the existing table. this can be changed to reduce redundancy
            // by requesting only data that has changed or doesn't exist.
            try
            {
                provider.delete(Uri.parse(ServerContentProvider.CONTENT_URI + "/" + table), null,
                                null);
            }
            catch (RemoteException e)
            {
                Log.d("SYNC", "Remote exception caught deleting table values.");
            }
            parseInsertions(lines, provider, table);
            i++;
        }
    }

    /**
     * Sends a broadcast with the success of the first synchronization (first login).
     *
     * @param extras Extras received from requesting activity.
     * @param authenticated If authentication was successful.
     * @param ioError IOerror when communicating with server.
     */
    private void syncBroadcast (Bundle extras, boolean authenticated, boolean ioError)
    {
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

    /**
     * Handle the individual insertions into the content provider for a sync request.
     *
     * @param lines The lines parsed from json.
     * @param provider Used to dynamically generate ContentProvider table URIs.
     * @param table The table name.
     */
    private void parseInsertions (String[] lines, ContentProviderClient provider, String table)
    {
        for (int j = 1; j < lines.length; j++)
        {
            ContentValues vals = new ContentValues();
            String entries[] = lines[j].split(",");
            for (String entry : entries)
            {
                String[] val = entry.split("=");
                vals.put(val[0].toUpperCase(), val[1]);
            }
            try
            {
                provider.insert(Uri.parse(ServerContentProvider.CONTENT_URI + "/" + table), vals);
            }
            catch (RemoteException e)
            {
                Log.d("SYNC", "Remote Exception:\n" + e.getMessage());
            }
        }
    }
}