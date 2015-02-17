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
 * @created 11/27/14
 * @version 2/15/2015
 *
 * Class containing setup and interaction of the GroupPage.
 */
public class GroupPage extends Activity implements View.OnClickListener
{
    /**
     * The current user account while it is being created.
     */
    private Account              account;
    /**
     * Broadcast receiver for confirmation of account synchronization.
     */
    private EditGroupReceiver    receiver;
    /**
     * Spinner Dialog to display while synchronizing with the server.
     */
    private LoadingSpinnerDialog spinnerDialog;
    /**
     * If user is already attending.
     */
    private boolean              member;

    /**
     * Attempts to overwrite default page text settings with group-specific ones.
     *
     * @param savedInstanceState not used
     */
    @Override
    public void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grouppage);

        account = ((AccountManager) this.getSystemService(Context.ACCOUNT_SERVICE))
                .getAccountsByType(GenericAccountService.ACCOUNT_TYPE)[0];
        receiver = null;

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        spinnerDialog = new LoadingSpinnerDialog();

        TextView groupName = ((TextView) findViewById(R.id.groupname));
        groupName.setText(extras.getString("group_name", groupName.getText().toString()));
        ((TextView) findViewById(R.id.group_description))
                .setText(extras.getString("group_description"));
        ((Button) findViewById(R.id.members)).setOnClickListener(this);
        Button join = ((Button) findViewById(R.id.join));
        member = extras.getBoolean("member");
        if (member)
        {
            join.setText("Leave");
        }
        join.setOnClickListener(this);
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
        if (v.equals(findViewById(R.id.members)))
        {
            Intent i = new Intent(this, MemberList.class);
            i.putExtra("group_name", getIntent().getExtras().getString("group_name"));
            startActivity(i);
        }
        if (v.equals(findViewById(R.id.join)))
        {
            Button join = (Button) v;
            boolean joining = !join.getText().equals("Leave");
            Bundle extras = new Bundle();
            extras.putString("request_type", "8");
            extras.putString("group_name", extras.getString("group_name"));
            extras.putBoolean("joining", joining);
            ContentResolver.requestSync(account, this.getString(R.string.authority), extras);
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
                Toast.makeText(GroupPage.this, "Error connecting to server.", Toast.LENGTH_LONG)
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
                        if (member)
                        {
                            builder.setMessage("No longer a member of this group.");
                        }
                        else
                        {
                            builder.setMessage("Successfully joined the group.");
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
                Toast.makeText(GroupPage.this, "Event error.", Toast.LENGTH_LONG).show();
            }
        }
    }
}