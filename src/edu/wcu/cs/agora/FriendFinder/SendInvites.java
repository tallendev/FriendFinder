package edu.wcu.cs.agora.FriendFinder;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.*;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tyler Allen
 * @version 4/13/2015
 * Activity allowing user to search and invite users.
 * @created 3/3/015
 */
public class SendInvites extends Activity
        implements View.OnClickListener, AdapterView.OnItemClickListener
{
    /**
     * URI for making a request from the Events table in the content provider.
     */
    public static final Uri EVENTS = Uri.parse(ServerContentProvider.CONTENT_URI + "/event");

    /**
     * Spinner Dialog to display while synchronizing with the server.
     */
    private LoadingSpinnerDialog spinnerDialog;
    /**
     * The ListView containing our search results.
     */
    private ListView             lv;
    /**
     * The editText box containing a user's search parameters.
     */
    private EditText             editText;
    /**
     * Search button.
     */
    private Button               search;
    /**
     * The current user's account.
     */
    private Account              account;
    /**
     * Our ArrayList containing each picture to be entered into the listview.
     */
    private ContentResolver      resolver;
    /**
     * Broadcast receiver for confirmation of account synchronization.
     */
    private SendInvitesReceiver  receiver;

    /**
     * @param savedInstanceState
     */
    @Override
    public void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_invites);

        spinnerDialog = new LoadingSpinnerDialog();
        lv = (ListView) findViewById(R.id.listView1);
        editText = (EditText) findViewById(R.id.searchEditText);
        search = (Button) findViewById(R.id.search);
        account = ((AccountManager) getSystemService(Context.ACCOUNT_SERVICE))
                .getAccountsByType(GenericAccountService.ACCOUNT_TYPE)[0];
        resolver = getContentResolver();
        receiver = null;
        resolver.registerContentObserver(Search.USERS, true,
                                         new SearchContentObserver(new Handler()));

        search.setOnClickListener(this);
    }

    /**
     * Unregister receiver if it exists.
     */
    @Override
    protected void onStop ()
    {
        super.onStop();
        if (receiver != null)
        {
            cleanupReceiver();
        }
    }

    /**
     * Helper for cleaning up the receiver.
     */
    private void cleanupReceiver ()
    {
        if (receiver != null)
        {
            unregisterReceiver(receiver);
            receiver = null;
        }
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick (View v)
    {
        if (v.getId() == R.id.search)
        {
            Bundle extras = new Bundle();
            // generate sync request based on search parameters.
            extras.putString("request_type", "3");
            //extras.putString("table0", "users_not_invited");
            extras.putString("table0", "users");
            extras.putString("search", "%" + editText.getText().toString() + "%");
            extras.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
            extras.putBoolean(ContentResolver.SYNC_EXTRAS_FORCE, true);
            extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
            ContentResolver.requestSync(account, getString(R.string.authority), extras);
            lv.setOnItemClickListener(this);
            spinnerDialog.show(getFragmentManager(), "Synchronizing with Server");
        }
    }

    /**
     * Callback method to be invoked when an item in this AdapterView has been clicked.
     * <p>
     * Implementers can call getItemAtPosition(position) if they need to access the data associated
     * with the selected item.
     *
     * @param parent The AdapterView where the click happened.
     * @param view The view within the AdapterView that was clicked (this will be a view provided by
     * the adapter)
     * @param position The position of the view in the adapter.
     * @param l The row id of the item that was clicked.
     */
    @Override
    public void onItemClick (AdapterView<?> parent, View view, int position, long l)
    {
        DialogFragment dialog = new DialogFragment()
        {
            @Override
            public Dialog onCreateDialog (Bundle savedInstanceState)
            {
                // Use the Builder class for convenient dialog construction
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Invite user?")
                       .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
                       {
                           @Override
                           public void onClick (DialogInterface dialog, int which)
                           {
                               User user = ((User) (parent.getAdapter().getItem((int) l)));
                               Bundle extras = new Bundle();
                               extras.putString("request_type", "10");
                               extras.putString("id", getIntent().getExtras().getString("id"));
                               extras.putString("invited_user", user.getEmail());
                               extras.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
                               extras.putBoolean(ContentResolver.SYNC_EXTRAS_FORCE, true);
                               extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
                               ContentResolver
                                       .requestSync(account, getString(R.string.authority), extras);
                               receiver = new SendInvitesReceiver();
                               IntentFilter intentFilter = new IntentFilter();
                               intentFilter.addAction("event_invite");
                               registerReceiver(receiver, intentFilter);
                               search.performClick();
                           }
                       }).setNegativeButton(R.string.cancel, null);
                // Create the AlertDialog object and return it
                return builder.create();
            }
        };
        dialog.show(getFragmentManager(), "Invite user to event??");
    }

    /**
     * Holder pattern object containing a text view.
     */
    public static class ViewHolder
    {
        /**
         * First text view holding name.
         */
        public TextView txt1;
    }

    /**
     * Extension of the ArrayAdapter class, using users as an object located inside each list view
     * item.
     *
     * @param <T>
     */
    private class ExtendedArrayAdapter <T> extends ArrayAdapter<T>
    {
        LayoutInflater inflater;
        List<T>        list;

        /**
         * Sets the fields.
         *
         * @param context Passed to super.
         * @param layout Passed to super.
         * @param txtLayout The txtLayout we are modifying in the list item view.
         * @param list The list of objects we put in our list view.
         */
        public ExtendedArrayAdapter (Context context, int layout, int txtLayout, List<T> list)
        {
            super(context, layout, txtLayout, list);
            this.list = list;
            inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        /**
         * Returns the view requested.
         *
         * @param position Not used
         * @param convertView the view we are converting.
         * @param parent Not used.
         *
         * @return The new view.
         */
        @Override
        public View getView (int position, View convertView, ViewGroup parent)
        {
            ViewHolder holder = null;
            if (convertView == null)
            {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.group_list_item, null);
                holder.txt1 = (TextView) convertView.findViewById(R.id.groupname);
                convertView.setTag(holder);
            }
            else
            {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.txt1.setText(((User) (list.get(position))).getName());
            return convertView;
        }
    }

    /**
     * An observer for a contentProvider. Allows us to register a callback to stop displaying a
     * spinner dialog and take other action.
     */
    private class SearchContentObserver extends ContentObserver
    {
        public SearchContentObserver (Handler handler)
        {
            super(handler);

        }

        /**
         * Updates the list view and dismissed the dialog.
         *
         * @param selfChange not used.
         */
        @Override
        public void onChange (boolean selfChange)
        {
            super.onChange(selfChange);
            // Build the list of each picture to be displayed in the listview.
            Log.d("GROUPS", "Resolver query");
            Cursor cursor = resolver.query(Search.USERS, null, null, null, null);
            buildUserList(cursor);
            if (spinnerDialog != null)
            {
                spinnerDialog.dismiss();
            }
        }

        /**
         * Helper method for building a list of users.
         *
         * @param cursor Contains all groups to add to the list.
         */
        private void buildUserList (Cursor cursor)
        {
            ArrayList<User> results = new ArrayList<User>();
            while (cursor != null && cursor.moveToNext())
            {
                String email = cursor.getString(cursor.getColumnIndex("EMAIL"));
                String birthday = cursor.getString(cursor.getColumnIndex("BIRTHDAY"));
                String gender = cursor.getString(cursor.getColumnIndex("GENDER"));
                String name = cursor.getString(cursor.getColumnIndex("FULL_NAME"));
                boolean busy = Boolean.valueOf(cursor.getString(cursor.getColumnIndex("BUSY")));
                results.add(new User(email, birthday, gender, name, busy));
            }
            ExtendedArrayAdapter<User> ad = new ExtendedArrayAdapter<User>(getApplicationContext(),
                                                                           R.layout.group_list_item,
                                                                           R.id.groupname, results);
            lv.setAdapter(ad);
        }
    }

    /**
     * Inner class for a broadcast receiver used to get registration confirmation from the
     * SyncAdapter.
     */
    public class SendInvitesReceiver extends BroadcastReceiver
    {
        /**
         * If registration is successful, we move to the next activity. Otherwise we clean up our
         * receiver and account, and await the user to try again.
         *
         * @param context Context of received broadcast.
         * @param intent Intent containing extra information used to determine success of sync.
         */
        @Override
        public void onReceive (Context context, Intent intent)
        {
            spinnerDialog.dismiss();
            // Server error, cleanup and toast.
            if (intent.getExtras().getBoolean("ioerr", false))
            {
                Toast.makeText(SendInvites.this, "Error connecting to server.", Toast.LENGTH_LONG)
                     .show();
                cleanupReceiver();
            }
            // Success. Move to next activity and kill this one to preserve state.
            else
            {
                DialogFragment dialog = new DialogFragment()
                {
                    @Override
                    public Dialog onCreateDialog (Bundle savedInstanceState)
                    {
                        // Use the Builder class for convenient dialog construction
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        if (intent.getExtras().getBoolean("success", false))
                        {
                            builder.setMessage("User invited.");
                        }
                        else
                        {
                            builder.setMessage("User already invited or attending.");
                        }
                        builder.setNeutralButton(R.string.ok, null);
                        // Create the AlertDialog object and return it
                        return builder.create();
                    }
                };
                dialog.show(getFragmentManager(), "Success");
            }
            cleanupReceiver();
        }
    }

}