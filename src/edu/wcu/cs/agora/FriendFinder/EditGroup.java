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
    /**
     * If this group is scheduled for deletion.
     */
    private boolean  deleted;
    /**
     * This activity.
     */
    private Activity activity;


    /**
     * @param savedInstanceState
     */
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
        findViewById(R.id.update).setOnClickListener(this);
        ((TextView) findViewById(R.id.title)).setText(extras.getString("group_name"));
        ((EditText) findViewById(R.id.group_description))
                .setText(extras.getString("group_description"));
        findViewById(R.id.delete).setOnClickListener(this);
        deleted = false;
        activity = this;
        findViewById(R.id.members).setOnClickListener(this);
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
            extras.putString("request_type", "1");
            extras.putString("groupname",
                             ((TextView) findViewById(R.id.title)).getText().toString());
            extras.putString("groupdesc",
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
        else if (v.getId() == R.id.delete)
        {
            deleteBuilder().show(getFragmentManager(), getString(R.string.delete_sure));
        }
        else if (v.equals(findViewById(R.id.members)))
        {
            Intent i = new Intent(this, MemberList.class);
            i.putExtra("group_name", getIntent().getExtras().getString("group_name"));
            startActivity(i);
        }
    }

    /**
     * Creates a dialog fragment to check if the user is sure that they would like to delete the
     * group.
     * @return The created dialog fragment.
     */
    public DialogFragment deleteBuilder ()
    {
        return new DialogFragment()
        {
            @Override
            public Dialog onCreateDialog (Bundle savedInstanceState)
            {
                // Use the Builder class for convenient dialog construction
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(R.string.delete_sure)
                       .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
                       {
                           @Override
                           public void onClick (DialogInterface dialog, int which)
                           {
                               deleted = true;
                               Bundle extras = new Bundle();
                               // generate sync request based on search parameters.
                               extras.putString("request_type", "6");
                               extras.putString("groupname",
                                                ((TextView) findViewById(R.id.title)).getText()
                                                                                     .toString());
                               ContentResolver
                                       .requestSync(account, getString(R.string.authority), extras);
                               spinnerDialog
                                       .show(getFragmentManager(), "Synchronizing with Server");
                               receiver = new EditGroupReceiver();
                               IntentFilter intentFilter = new IntentFilter();
                               intentFilter.addAction("group_update");
                               registerReceiver(receiver, intentFilter);
                           }
                       }).setNegativeButton(R.string.cancel, null);
                // Create the AlertDialog object and return it
                return builder.create();
            }
        };
    }

    /**
     * Creates a dialog fragment for successful updating or deletion of a group.
     * @return The created dialog fragment.
     */
    private DialogFragment successBuilder ()
    {
        return new DialogFragment()
        {
            @Override
            public Dialog onCreateDialog (Bundle savedInstanceState)
            {
                // Use the Builder class for convenient dialog construction
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                if (deleted)
                {
                    builder.setMessage(R.string.group_deleted);
                }
                else
                {
                    builder.setMessage(R.string.group_update);
                }
                builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener()
                {
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
                deleted = false;
            }
            // Success. Move to next activity and kill this one to preserve state.
            else if (intent.getExtras().getBoolean("success", false))
            {
                DialogFragment dialog = successBuilder();
                dialog.show(getFragmentManager(), "Success");
            }
            // Error from server meaning group name is taken.
            else
            {
                cleanupReceiver();
                Toast.makeText(EditGroup.this, "Group name is taken.", Toast.LENGTH_LONG).show();
                deleted = false;
            }
        }
    }
}