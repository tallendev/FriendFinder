package edu.wcu.cs.agora.FriendFinder;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.*;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import java.util.Calendar;

/**
 * @author Tyler Allen
 * @created 9/26/14
 * @version 12/07/2014
 */
public class Register extends Activity
        implements View.OnClickListener, AdapterView.OnItemSelectedListener
{

    /**
     * EditText associated with user name.
     */
    private EditText             user;
    /**
     * EditText associated with password.
     */
    private EditText             password;
    /**
     * EditText associated with field requesting password repetition.
     */
    private EditText             rpassword;
    /**
     * EditText associated with the user's full name.
     */
    private EditText             name;
    /**
     * A dialog to display during account synchronization.
     */
    private LoadingSpinnerDialog spinnerDialog;
    /**
     * Broadcast receiver for confirmation of account synchronization.
     */
    private RegistrationReceiver receiver;
    /**
     * Current user account. Null if user has not made an attempt to register yet.
     */
    private Account              account;
    /**
     * A datePicker fragment that appears for a user to set their birthday.
     */
    private DatePickerFragment   datePicker;
    /**
     * Spinner indicating a user's gender.
     */
    private Spinner              gender;
    /**
     * The user's selected gender.
     */
    private String               genderSelected;
    /**
     * Button to display the datePicker.
     */
    private Button               date;
    /**
     * Register button.
     */
    private Button               register;

    /**
     * Initializes fields and assigns button listeners.
     * @param savedInstanceState none
     */
    @Override
    public void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        this.user = (EditText) findViewById(R.id.email);
        this.password = (EditText) findViewById(R.id.pass);
        this.rpassword = (EditText) findViewById(R.id.repeat_pass);
        this.name = (EditText) findViewById(R.id.name);

        spinnerDialog = new LoadingSpinnerDialog();
        receiver = null;
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter
                .createFromResource(this, R.array.genders, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        this.datePicker = new DatePickerFragment();
        this.gender = (Spinner) findViewById(R.id.gender);
        gender.setAdapter(adapter);
        gender.setOnItemSelectedListener(this);
        genderSelected = "Male";

        this.register = (Button) findViewById(R.id.register);
        this.date = (Button) findViewById(R.id.date);
        this.register.setOnClickListener(this);
        this.date.setOnClickListener(this);
    }

    /**
     * Unregisters receiver and deletes account if they exist.
     */
    @Override
    protected void onStop ()
    {
        super.onStop();
        if (receiver != null)
        {
            cleanupReceiver();
            cleanupAccount();
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
     * Helper for deleting account that has not been registered with server.
     */
    private void cleanupAccount ()
    {
        if (account != null)
        {
            AccountManager accountManager = (AccountManager) this.getSystemService(ACCOUNT_SERVICE);
            accountManager.removeAccount(account, null, null);
            account = null;
        }
    }


    /**
     * Called when a view has been clicked. Handles register and date selections.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick (View v)
    {
        if (v == register)
        {
            // Validate matching passwords.
            if (password.getText().toString().equals(rpassword.getText().toString()))
            {
                if (!genderSelected.equals("Select"))
                {
                    String AUTHORITY = getResources().getString(R.string.authority);
                    Bundle extras = new Bundle();
                    // pack bundle with user extras
                    extras.putString("request_type", "0");
                    extras.putString("user", user.getText().toString());
                    extras.putString("password", user.getText().toString());
                    extras.putString("birthday",
                                     datePicker.getYear() + "-" + datePicker.getMonth() + "-" +
                                     datePicker.getDay());
                    extras.putString("gender", genderSelected);
                    extras.putString("name", name.getText().toString());
                    // Attempt to create a sync account
                    Account account = FriendFinder
                            .createSyncAccount(this, user.getText().toString(),
                                               user.getText().toString());
                    // Allow synchronization.
                    ContentResolver.setSyncAutomatically(account, AUTHORITY, true);
                    // Attempt to synchronize.
                    ContentResolver.requestSync(account, AUTHORITY, extras);
                    spinnerDialog.show(getFragmentManager(), "Registering User");
                    receiver = new RegistrationReceiver();
                    IntentFilter intentFilter = new IntentFilter();
                    intentFilter.addAction("registration");
                    registerReceiver(receiver, intentFilter);
                }
                else
                {
                    Toast.makeText(this, "Please select a gender.", Toast.LENGTH_LONG).show();
                }
            }
            else
            {
                Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_LONG).show();
            }
        }
        else if (v == date)
        {
            // display datepicker.
            datePicker.show(getFragmentManager(), "Date Picker");
        }
    }

    /**
     * <p>Callback method to be invoked when an item in this view has been selected. This callback
     * is invoked only when the newly selected position is different from the previously selected
     * position or if there was no selected item.</p>
     * <p>
     * Implementers can call getItemAtPosition(position) if they need to access the data associated
     * with the selected item.
     *
     * @param parent The AdapterView where the selection happened
     * @param view The view within the AdapterView that was clicked
     * @param position The position of the view in the adapter
     * @param id The row id of the item that is selected
     */
    @Override
    public void onItemSelected (AdapterView<?> parent, View view, int position, long id)
    {
        genderSelected = parent.getItemAtPosition(position).toString();
    }

    /**
     * Callback method to be invoked when the selection disappears from this view. The selection can
     * disappear for instance when touch is activated or when the adapter becomes empty.
     *
     * @param parent The AdapterView that now contains no selected item.
     */
    @Override
    public void onNothingSelected (AdapterView<?> parent)
    {
        // don't care, not used.
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
         * Assigns default values to fields. Returns a datePicker dialog.
         * @param savedInstanceState not used.
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
            // Create a new instance of TimePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

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
        }
    }

    /**
     * Inner class for a broadcast receiver used to get registration confirmation from the
     * SyncAdapter.
     */
    public class RegistrationReceiver extends BroadcastReceiver
    {
        /**
         * If registration is succesful, we move to the next activity. Otherwise we clean up our
         * receiver and account, and await the user to try again.
         *
         * @param context
         * @param intent
         */
        @Override
        public void onReceive (Context context, Intent intent)
        {
            spinnerDialog.dismiss();
            // Server error, cleanup and toast.
            if (intent.getExtras().getBoolean("ioerr", false))
            {
                Toast.makeText(Register.this, "Error connecting to server.", Toast.LENGTH_LONG)
                     .show();
                cleanupReceiver();
                cleanupAccount();
            }
            // Success. Move to next activity and kill this one to preserve state.
            else if (intent.getExtras().getBoolean("success", false))
            {
                Intent i = new Intent(Register.this, Profile.class);
                i.putExtra("owner", true);
                startActivity(i);
                finish();
            }
            // Error from server meaning email address is taken.
            else
            {
                //fixme longterm add checks for valid email beforehand
                cleanupReceiver();
                cleanupAccount();
                Toast.makeText(Register.this, "Email is already in use.", Toast.LENGTH_LONG).show();
            }
        }
    }
}