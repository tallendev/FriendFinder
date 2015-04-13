package edu.wcu.cs.agora.FriendFinder;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.*;
import android.content.*;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.*;

import java.util.Calendar;

/**
 * @author Tyler Allen
 * @version 2/15/2015
 * @created 3/15/2015
 */
public class EditEvent extends Activity implements View.OnClickListener
{
    /**
     * Map location change.
     */
    public static final int MAP_LOCATION = 12345;

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
    private EditEventReceiver    receiver;
    /**
     * If this group is scheduled for deletion.
     */
    private boolean  deleted;
    /**
     * A datePicker fragment that appears for a user to set their birthday.
     */
    private DatePickerFragment datePicker;
    /**
     * A datePicker fragment that appears for a user to set their birthday.
     */
    private TimePickerFragment timePicker;
    /**
     * Map button
     */
    private Button map;
    /**
     * Location data
     */
    private String location;


    /**
     * @param savedInstanceState
     */
    @Override
    public void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_event);
        Intent intent = getIntent();
        account = ((AccountManager) getSystemService(Context.ACCOUNT_SERVICE))
                .getAccountsByType(GenericAccountService.ACCOUNT_TYPE)[0];
        spinnerDialog = new LoadingSpinnerDialog();
        receiver = null;
        map = (Button) findViewById(R.id.map);
        findViewById(R.id.update).setOnClickListener(this);
        EditText eventName = ((EditText) findViewById(R.id.eventname));
        eventName.setText(
                intent.getExtras().getString("event_name", eventName.getText().toString()));

        findViewById(R.id.date).setOnClickListener(this);
        findViewById(R.id.time).setOnClickListener(this);
        findViewById(R.id.invite).setOnClickListener(this);
        map.setOnClickListener(this);

        ((EditText) findViewById(R.id.description))
                .setText(intent.getExtras().getString("description"));
        findViewById(R.id.cancel).setOnClickListener(this);
        datePicker = new DatePickerFragment();
        timePicker = new TimePickerFragment();
        location = getIntent().getExtras().getString("location");
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

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MAP_LOCATION)
        {
            if (resultCode == MAP_LOCATION)
            {
                Log.d("CREATE_EVENT", data.getExtras().toString());
                location = data.getExtras().getString("location");
            }
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
                             ((EditText) findViewById(R.id.eventname)).getText().toString());
            extras.putString("time", timePicker.getHour() + ":" + timePicker.getMinute() + ":00");
            extras.putString("date", datePicker.getYear() + "-" + datePicker.getMonth() + "-" +
                                     datePicker.getDay());
            extras.putString("description",
                             ((EditText) findViewById(R.id.description)).getText().toString());
            extras.putString("id", getIntent().getExtras().getString("id"));
            extras.putString("location", location);

            extras.putBoolean("create", false);
            extras.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
            extras.putBoolean(ContentResolver.SYNC_EXTRAS_FORCE, true);
            extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
            ContentResolver.requestSync(account, getString(R.string.authority), extras);
            spinnerDialog.show(getFragmentManager(), "Synchronizing with Server");
            receiver = new EditEventReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("event_update");
            registerReceiver(receiver, intentFilter);
        }
        else if (v.getId() == R.id.cancel)
        {
            deleteBuilder().show(getFragmentManager(), "Are you sure?");
        }
        else if (v.getId() == R.id.date)
        {
            datePicker.show(getFragmentManager(), "Date Picker");
        }
        else if (v.getId() == R.id.time)
        {
            timePicker.show(getFragmentManager(), "Time Picker");
        }
        else if (v == map)
        {
            Intent i = new Intent(this, Map.class);
            i.putExtra("owner", true);
            i.putExtra("location", location);
            startActivityForResult(i, MAP_LOCATION);
        }
        else if (v.getId() == R.id.invite)
        {
            Intent i = new Intent(this, SendInvites.class);
            i.putExtra("id", getIntent().getExtras().getString("id"));
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
                builder.setMessage(R.string.cancel_sure)
                       .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
                       {
                           @Override
                           public void onClick (DialogInterface dialog, int which)
                           {
                               deleted = true;
                               Bundle extras = new Bundle();
                               // generate sync request based on search parameters.
                               extras.putString("request_type", "7");
                               extras.putString("id", getIntent().getExtras().getString("id"));
                               extras.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
                               extras.putBoolean(ContentResolver.SYNC_EXTRAS_FORCE, true);
                               extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
                               ContentResolver
                                       .requestSync(account, getString(R.string.authority), extras);
                               spinnerDialog
                                       .show(getFragmentManager(), "Synchronizing with Server");
                               receiver = new EditEventReceiver();
                               IntentFilter intentFilter = new IntentFilter();
                               intentFilter.addAction("event_update");
                               registerReceiver(receiver, intentFilter);
                           }
                       }).setNegativeButton(R.string.cancel, null);
                // Create the AlertDialog object and return it
                return builder.create();
            }
        };
    }

    /**
     * Class for creating TimePicker dialogs.
     */
    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener
    {
        private Calendar c;
        private int      hour;
        private int      minute;

        public void onTimeSet (TimePicker view, int hourOfDay, int minute)
        {
            this.hour = hourOfDay;
            this.minute = minute;
        }

        public int getMinute ()
        {
            return minute;
        }

        public int getHour ()
        {
            return hour;
        }

        @Override
        public Dialog onCreateDialog (Bundle savedInstanceState)
        {
            // Use the current time as the default values for the picker
            c = Calendar.getInstance();
            hour = c.get(Calendar.HOUR_OF_DAY);
            minute = c.get(Calendar.MINUTE);
            TimePickerDialog t = new TimePickerDialog(getActivity(), this, hour, minute,
                                                      DateFormat.is24HourFormat(getActivity()));
            // Create a new instance of TimePickerDialog and return it
            return t;
        }




    }

    /**
     * Class for creating DatePicker dialogs.
     */
    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener
    {

        /**
         * Currently selected year.
         */
        private int year;
        /**
         * Currently selected month.
         */
        private int month;
        /**
         * Currently selected day.
         */
        private int day;

        /**
         * @param view The view associated with this listener.
         * @param year The year that was set.
         * @param monthOfYear The month that was set (0-11) for compatibility with {@link
         * java.util.Calendar}.
         * @param dayOfMonth The day of the month that was set.
         */
        @Override
        public void onDateSet (DatePicker view, int year, int monthOfYear, int dayOfMonth)
        {
            this.year = year;
            this.month = monthOfYear;
            this.day = dayOfMonth;
        }

        /**
         * Getter for year.
         *
         * @return year
         */
        public int getYear ()
        {
            return year;
        }

        /**
         * Getter for month.
         *
         * @return month
         */
        public int getMonth ()
        {
            return month;
        }

        /**
         * Getter for day.
         *
         * @return day
         */
        public int getDay ()
        {
            return day;
        }        /**
         * Assigns default values to fields. Returns a datePicker dialog.
         *
         * @param savedInstanceState not used.
         *
         * @return A new DatePickerDialog.
         */
        @Override
        public Dialog onCreateDialog (Bundle savedInstanceState)
        {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            year = c.get(Calendar.YEAR);
            month = c.get(Calendar.MONTH);
            day = c.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog d = new DatePickerDialog(getActivity(), this, year, month, day);
            //d.getDatePicker().setMinDate(new Date().getTime() - 1);

            // Create a new instance of TimePickerDialog and return it
            return d;
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
                Toast.makeText(EditEvent.this, "Error connecting to server.", Toast.LENGTH_LONG)
                     .show();
                cleanupReceiver();
                deleted = false;
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
                        if (deleted)
                        {
                            builder.setMessage(R.string.cancelled);
                        }
                        else
                        {
                            builder.setMessage(R.string.event_update);
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
                Toast.makeText(EditEvent.this, "Event error.", Toast.LENGTH_LONG).show();
                deleted = false;
            }
        }
    }
}