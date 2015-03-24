package edu.wcu.cs.agora.FriendFinder;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tyler on 2/16/2015.
 */
public class MemberList extends Activity implements AdapterView.OnItemClickListener
{

    /**
     * The request status when requesting a class.
     */
    public static final int REQUEST = 1;

    /**
     * URI for making a request from the group_member table in the content provider.
     */
    public static final Uri MEMBERS = Uri.parse(ServerContentProvider.CONTENT_URI + "/users");

    /**
     * The ListView containing our search results.
     */
    private ListView             lv;
    /**
     * Spinner Dialog to display while synchronizing with the server.
     */
    private LoadingSpinnerDialog spinnerDialog;
    /**
     * Our ArrayList containing each member to be entered into the ListView.
     */
    private ArrayList<User>      members;
    /**
     * The current user's account.
     */
    private Account              account;
    /**
     * Content Resolver
     */
    private ContentResolver      resolver;
    /**
     * True if spinner is showing.
     */
    private boolean              spinnerShowing;

    @Override
    public void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.members);
        spinnerDialog = new LoadingSpinnerDialog();
        resolver = getContentResolver();
        account = ((AccountManager) getSystemService(Context.ACCOUNT_SERVICE))
                .getAccountsByType(GenericAccountService.ACCOUNT_TYPE)[0];
        spinnerShowing = false;
        lv = (ListView) findViewById(R.id.members);
        if (savedInstanceState == null)
        {
            Bundle extras = new Bundle();
            extras.putString("request_type", "3");
            extras.putString("table0", "users");
            extras.putString("search", "%%");
            extras.putString("group_name", getIntent().getExtras().getString("group_name"));
            resolver.registerContentObserver(MEMBERS, true, new SyncContentObserver(new Handler()));
            ContentResolver.requestSync(account, this.getString(R.string.authority), extras);
            spinnerDialog.show(getFragmentManager(), "Synchronizing...");
            spinnerShowing = true;
        }
    }

    /**
     * Here we do all the main work of setting up the list, so that it is regenerated every time we
     * return to this screen in the event of file system changes from other fragments/activities.
     */
    @Override
    public void onResume ()
    {
        super.onResume();
        Bundle extras = new Bundle();
        extras.putString("request_type", "3");
        extras.putString("table0", "users");
        extras.putString("search", "%%");
        extras.putString("group_name", getIntent().getExtras().getString("group_name"));
        ContentResolver.requestSync(account, this.getString(R.string.authority), extras);
        Log.d("EVENTS", "Resolver query");
    }

    /**
     * Callback method to be invoked when an item in this AdapterView has been clicked.
     * <p>
     * Implementers can call getItemAtPosition(position) if they need to access the data associated
     * with the selected item.
     *
     * @param adapterView The AdapterView where the click happened.
     * @param view The view within the AdapterView that was clicked (this will be a view provided by
     * the adapter)
     * @param i The position of the view in the adapter.
     * @param l The row id of the item that was clicked.
     */
    @Override
    public void onItemClick (AdapterView<?> adapterView, View view, int i, long l)
    {
        Intent intent = new Intent(this, Profile.class);
        User user = ((User) (adapterView.getAdapter().getItem((int) l)));
        boolean owner;
        owner = user.getEmail().equals(account.name);
        intent.putExtra("owner", owner);
        intent.putExtra("gender", user.getGender());
        intent.putExtra("birthday", user.getBirthday());
        intent.putExtra("name", user.getName());
        intent.putExtra("busy", user.isBusy());
        startActivityForResult(intent, REQUEST);
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
     * Extension of the ArrayAdapter class, using Websites as an object located inside each list
     * view item.
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
            inflater = (LayoutInflater) MemberList.this
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
                convertView = inflater.inflate(R.layout.members_list_item, null);
                holder.txt1 = (TextView) convertView.findViewById(R.id.membername);
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

    private class SyncContentObserver extends ContentObserver
    {

        /**
         * onChange() will happen on the provider Handler.
         *
         * @param handler The handler to run {@link #onChange} on.
         */
        public SyncContentObserver (Handler handler)
        {
            super(handler);
        }

        /**
         * This method is called when a change occurs to the cursor that is being observed.
         *
         * @param selfChange true if the update was caused by a call to <code>commit</code> on the
         * cursor that is being observed.
         */
        @Override
        public void onChange (boolean selfChange)
        {
            super.onChange(selfChange);
            // query content provider
            lv.invalidateViews();
            Cursor cursor = resolver.query(MEMBERS, null, null, null, null);
            members = new ArrayList<>();
            while (cursor.moveToNext())
            {
                String email = cursor.getString(cursor.getColumnIndex("EMAIL"));
                String birthday = cursor.getString(cursor.getColumnIndex("BIRTHDAY"));
                String gender = cursor.getString(cursor.getColumnIndex("GENDER"));
                String name = cursor.getString(cursor.getColumnIndex("FULL_NAME"));
                boolean busy = Boolean.getBoolean(cursor.getString(cursor.getColumnIndex("BUSY")));
                members.add(new User(email, birthday, gender, name, busy));//, eventLocation));
                ;//,
                // eventLocation));
            }
            cursor.close();
            // Create our list.
            ExtendedArrayAdapter<User> ad = new ExtendedArrayAdapter<User>(MemberList.this,
                                                                           R.layout.members_list_item,
                                                                           R.id.members, members);
            lv.setAdapter(ad);

            lv.setOnItemClickListener(MemberList.this);
            if (spinnerShowing)
            {
                spinnerDialog.dismiss();
                spinnerShowing = false;
            }
        }
    }
}