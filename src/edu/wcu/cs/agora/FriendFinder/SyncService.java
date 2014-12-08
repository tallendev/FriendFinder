package edu.wcu.cs.agora.FriendFinder;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * @author Tyler Allen
 * @created 10/16/2014
 * @version 12/8/2014
 * Credit: Android Framework SyncService.java example.
 * Reference: https://developer.android.com/training/sync-adapters/creating-authenticator.html to
 * continue
 */
public class SyncService extends Service
{
    /**
     * Lock to ensure that we don't have multiple SyncAdapter instances.
     */
    private static final Object      SYNC_ADAPTER_LOCK = new Object();
    /**
     * The SyncService's syncAdapter.
     */
    private static       SyncAdapter syncAdapter       = null;

    /**
     * Thread-safe constructor, creates static {@link SyncAdapter} instance.
     */
    @Override
    public void onCreate ()
    {
        super.onCreate();
        Log.d("SYNC SERVICE", "OnCreate");
        synchronized (SYNC_ADAPTER_LOCK)
        {
            if (syncAdapter == null)
            {
                syncAdapter = new SyncAdapter(getApplicationContext(), true);
                Log.d("SYNC SERVICE", "SyncAdapter created");
            }
        }
    }

    @Override
    /**
     * Logging-only destructor.
     */ public void onDestroy ()
    {
        super.onDestroy();
    }

    /**
     * Return Binder handle for IPC communication with {@link SyncAdapter}.
     * <p>
     * <p>New sync requests will be sent directly to the SyncAdapter using this channel.
     *
     * @param intent Calling intent
     *
     * @return Binder handle for {@link SyncAdapter}
     */
    @Override
    public IBinder onBind (Intent intent)
    {
        return syncAdapter.getSyncAdapterBinder();
    }
}
