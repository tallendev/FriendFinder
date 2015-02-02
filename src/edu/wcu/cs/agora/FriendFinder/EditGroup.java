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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Tyler Allen
 * @version 2/1/2015
 * @created 2/1/2015
 * Activity for editing a group if the user is the owner of the group.
 */
public class EditGroup extends Activity implements View.OnClickListener
{
    /**
     * The current user's account.
     */
    private Account              account;
    /**
     * Spinner Dialog to display while synchronizing with the server.
     */
    private LoadingSpinnerDialog spinnerDialog;
    /**
     * Broadcast receiver for confirmation of account synchronization.
     */
    private EditGroupReceiver    receiver;

    @Override
    public void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_group);
        Bundle extras = getIntent().getExtras();
        account = ((AccountManager) getSystemService(Context.ACCOUNT_SERVICE))
                .getAccountsByType(GenericAccountService.ACCOUNT_TYPE)[0];
        spinnerDialog = new LoadingSpinnerDialog();
        receiver = null;
        ((Button) findViewById(R.id.update)).setOnClickListener(this);
        ((TextView) findViewById(R.id.title)).setText(extras.getString("group_name"));
        ((EditText) findViewById(R.id.group_description))
                .setText(extras.getString("group_description"));

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
        if (v.getId() == R.id.update)
        {
            Bundle extras = new Bundle();
            // generate sync request based on search parameters.
            extras.putString("request_type", "5");
            extras.putString("eventname",
                             ((TextView) findViewById(R.id.title)).getText().toString());
            extras.putString("event",
                             ((EditText) findViewById(R.id.group_description)).getText()
                                                                              .toString());
            extras.putBoolean("create", false);
            ContentResolver.requestSync(account, getString(R.string.authority), extras);
            spinnerDialog.show(getFragmentManager(), "Synchronizing with Server");
            receiver = new EditGroupReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("group_update");
            registerReceiver(receiver, intentFilter);
        }
    }

    /**
     * Inner class for a broadcast receiver used to get registration confirmation from the
     * SyncAdapter.
     */
    public class EditGroupReceiver extends BroadcastReceiver
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
                Toast.makeText(EditGroup.this, "Error connecting to server.", Toast.LENGTH_LONG)
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
                        builder.setMessage(R.string.event_update)
                               .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener()
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
                Toast.makeText(EditGroup.this, "Group name is taken.", Toast.LENGTH_LONG).show();
            }
        }
    }
}