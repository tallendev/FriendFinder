package edu.wcu.cs.agora.FriendFinder;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.*;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Tyler Allen
 * @created 09/29/2014
 * @version 2/15/2015
 * Code for functioning specific event instance.
 */
public class EventsPage extends Activity implements View.OnClickListener
{
    /**
     * Map button
     */
    private Button               map;
    /**
     * The current user account while it is being created.
     */
    private Account              account;
    /**
     * Spinner Dialog to display while synchronizing with the server.
     */
    private LoadingSpinnerDialog spinnerDialog;
    /**
     * Broadcast receiver for confirmation of account synchronization.
     */
    private EditEventReceiver    receiver;
    /**
     * If user is already attending.
     */
    private boolean              attending;

    /**
     * Currently the default fragment onCreate.
     */
    @Override
    public void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.eventspage);

        account = ((AccountManager) this.getSystemService(Context.ACCOUNT_SERVICE))
                .getAccountsByType(GenericAccountService.ACCOUNT_TYPE)[0];
        receiver = null;
        spinnerDialog = new LoadingSpinnerDialog();
        map = (Button) findViewById(R.id.map);
        Intent intent = getIntent();
        TextView eventName = ((TextView) findViewById(R.id.eventname));
        eventName.setText(
                intent.getExtras().getString("event_name", eventName.getText().toString()));
        ((TextView) findViewById(R.id.date))
                .setText(intent.getExtras().getString("event_date"));

        ((TextView) findViewById(R.id.time)).setText(intent.getExtras().getString("event_time"));
        ((TextView) findViewById(R.id.description))
                .setText(intent.getExtras().getString("description"));
        map.setOnClickListener(this);
        Button attend = ((Button) findViewById(R.id.attending));
        attend.setOnClickListener(this);
        attending = getIntent().getExtras().getBoolean("attending");
        if (getIntent().getExtras().getBoolean("attending"))
        {
            attend.setText("Leave");
        }
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
     * Called when a view has been clicked. Fires map page if clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick (View v)
    {
        if (v == map)
        {
            Intent i = new Intent(this, Map.class);
            startActivity(i);
        }
        else if (v.equals(findViewById(R.id.join)))
        {
            Button join = (Button) v;
            boolean joining = join.getText().equals("Leave");
            Bundle extras = new Bundle();
            extras.putString("request_type", "9");
            extras.putString("id", extras.getString("id"));
            extras.putBoolean("joining", joining);
            ContentResolver.requestSync(account, this.getString(R.string.authority), extras);
            spinnerDialog.show(getFragmentManager(), "Synchronizing with Server");
            receiver = new EditEventReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("event_update");
            registerReceiver(receiver, intentFilter);
        }
    }

    /**
     * Inner class for a broadcast receiver used to get registration confirmation from the
     * SyncAdapter.
     */
    public class EditEventReceiver extends BroadcastReceiver
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
                Toast.makeText(EventsPage.this, "Error connecting to server.", Toast.LENGTH_LONG)
                     .show();
                cleanupReceiver();
            }
            // Success. Move to next activity and kill this one to preserve state.
            else if (intent.getExtras().getBoolean("success", false))
            {
                DialogFragment dialog = new DialogFragment()
                {
                    @Override
                    public Dialog onCreateDialog (Bundle savedInstanceState)
                    {
                        // Use the Builder class for convenient dialog construction
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        if (attending)
                        {
                            builder.setMessage("No longer attending this event.");
                        }
                        else
                        {
                            builder.setMessage("Now attending this event.");
                        }
                        builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener()
                        {
                            /**
                             * This method will be
                             * invoked when a button
                             * in the dialog is
                             * clicked.
                             *
                             * @param dialog The
                             * dialog that received
                             * the click.
                             * @param which The
                             * button that was
                             * clicked (e.g. {@link
                             * android.content.DialogInterface#BUTTON1})
                             * or the position
                             */
                            @Override
                            public void onClick (DialogInterface dialog, int which)
                            {
                                getActivity().setResult(Search.DATA_INVALID);
                                getActivity().finish();
                            }
                        });
                        // Create the AlertDialog object and return it
                        return builder.create();
                    }
                };
                dialog.show(getFragmentManager(), "Success");
            }
            // Error from server meaning group name is taken.
            else
            {
                cleanupReceiver();
                Toast.makeText(EventsPage.this, "Event error.", Toast.LENGTH_LONG).show();
            }
        }
    }

}