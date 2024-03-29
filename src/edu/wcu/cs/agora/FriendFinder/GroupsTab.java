package edu.wcu.cs.agora.FriendFinder;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
 * @author Tyler Allen
 * @version 12/7/2014 Code for the Fragment that contains events.
 * @created 09/29/2014
 *
 * This tab displays the user's groups.
 */

public class GroupsTab extends Fragment implements AdapterView.OnItemClickListener
{
    /**
     * Request for result from event.
     */
    public static final int GROUP_RESULT = 600;

    /**
     * Code indicating data invalidation.
     */
    public static final int DATA_INVALID = 10000;

    /**
     * The request status when requesting an event class.
     */
    public static final int REQUEST = 1;
    /**
     * URI for making a request from the Events table in the content provider.
     */
    public static final Uri GROUPS  = Uri.parse(ServerContentProvider.CONTENT_URI + "/user_group");

    /**
     * RootView of this fragment.
     */
    private View                 rootView;
    /**
     * Our ArrayList containing each event to be entered into the ListView.
     */
    private ArrayList<Group>     groups;
    /**
     * ContentResolver used for retrieving information from the ContentProvider.
     */
    private ContentResolver      resolver;
    /**
     * Current User's account.
     */
    private Account              account;
    /**
     * SpinnerDialog that we will display when synchronization is in progress.
     */
    private LoadingSpinnerDialog spinnerDialog;
    /**
     * True if the spinnerDialog is showing.
     */
    private boolean              spinnerShowing;
    /**
     * ListView containing events.
     */
    private ListView             lv;

    /**
     * When an item is clicked, the appropriate event is opened up in the EventPage class.
     *
     * @param adapterView The AdapterView for this list view.
     * @param view The view that was clicked.
     * @param i Not used
     * @param l Index of item we are looking for.
     */
    @Override
    public void onItemClick (AdapterView<?> adapterView, View view, int i, long l)
    {
        Group group = ((Group) (adapterView.getAdapter().getItem((int) l)));

        Class page;
        if (account.name.equals(group.getOwner()))
        {
            page = EditGroup.class;
        }
        else
        {
            page = GroupPage.class;
        }
        Intent intent = new Intent(getActivity(), page);
        intent.putExtra("group_name", group.getGroupName());
        intent.putExtra("group_description", group.getDescription());
        intent.putExtra("member", group.isMember());
        startActivityForResult(intent, REQUEST);
    }

    @Override
    public void onActivityResult (int requestCode, int resultCode, Intent data)
    {
        // Check which request we're responding to
        if (requestCode == GROUP_RESULT)
        {
            // Make sure the request was successful
            if (resultCode == DATA_INVALID)
            {
                Bundle extras = new Bundle();
                extras.putString("request_type", "3");
                extras.putString("table0", "user_group_self");
                extras.putString("search", "%%");
                resolver.registerContentObserver(GROUPS, true,
                                                 new SyncContentObserver(new Handler()));
                extras.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
                extras.putBoolean(ContentResolver.SYNC_EXTRAS_FORCE, true);
                extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
                ContentResolver
                        .requestSync(account, getActivity().getString(R.string.authority), extras);
                spinnerDialog.show(getFragmentManager(), "Synchronizing...");
                spinnerShowing = true;
            }
        }
    }

    /**
     * Default.
     *
     * @param savedInstanceState not used
     */
    @Override
    public void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    /**
     * Initializes fields and creates a ContentObserver to receive notification of the
     * ContentProvider completing an update.
     *
     * @param inflater Used to inflate the view.
     * @param container View's container.
     * @param savedInstanceState not used
     *
     * @return The view created.
     */
    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        rootView = inflater.inflate(R.layout.events_list, container, false);
        lv = (ListView) rootView.findViewById(R.id.listView1);
        spinnerDialog = new LoadingSpinnerDialog();
        resolver = getActivity().getContentResolver();
        account = ((AccountManager) getActivity().getSystemService(Context.ACCOUNT_SERVICE))
                .getAccountsByType(GenericAccountService.ACCOUNT_TYPE)[0];
        groups = new ArrayList<Group>();

        return rootView;
    }

    /**
     * Here we do all the main work of setting up the list, so that it is regenerated every time we
     * return to this screen in the event of file system changes from other fragments/activities.
     */
    @Override
    public void onResume ()
    {
        super.onResume();
        spinnerDialog.show(getFragmentManager(), "Synchronizing...");
        spinnerShowing = true;
        Bundle extras = new Bundle();
        extras.putString("request_type", "3");
        extras.putString("table0", "user_group_self");
        extras.putString("search", "%%");
        resolver.registerContentObserver(GROUPS, true, new SyncContentObserver(new Handler()));
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_FORCE, true);
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(account, getActivity().getString(R.string.authority), extras);
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
            inflater = (LayoutInflater) getContext()
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
                convertView = inflater.inflate(R.layout.group_list_item, null);
                holder.txt1 = (TextView) convertView.findViewById(R.id.groupname);
                convertView.setTag(holder);
            }
            else
            {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.txt1.setText(((Group) (list.get(position))).getGroupName());
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
            Cursor cursor = resolver.query(GROUPS, null, null, null, null);
            groups = new ArrayList<>();
            while (cursor.moveToNext())
            {
                // retrieve data from content provider element
                String groupName = cursor.getString(cursor.getColumnIndex("GROUP_NAME"));
                String description = cursor.getString(cursor.getColumnIndex("GROUP_DESCRIPTION"));
                String owner = cursor.getString(cursor.getColumnIndex("OWNER"));
                boolean member = Boolean.valueOf(cursor.getString(cursor.getColumnIndex("MEMBER")));
                groups.add(new Group(groupName, description, owner, member));
            }
            // Create our list.
            ExtendedArrayAdapter<Group> ad = new ExtendedArrayAdapter<Group>(rootView.getContext(),
                                                                             R.layout.group_list_item,
                                                                             R.id.groupname,
                                                                             groups);
            lv.setAdapter(ad);

            lv.setOnItemClickListener(GroupsTab.this);
            if (spinnerShowing)
            {
                spinnerDialog.dismiss();
                spinnerShowing = false;
            }
        }
    }
}