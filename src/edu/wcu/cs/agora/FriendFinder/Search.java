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
import android.widget.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tyler Allen
 * @created 11/2/2014
 * @version 2/1/2015
 *
 * This class sets up and performs visual and interactive functions available on the search page.
 */
public class Search extends Activity
        implements View.OnClickListener, AdapterView.OnItemClickListener,
        AdapterView.OnItemSelectedListener
{

    /**
     * The request status when requesting a class.
     */
    public static final int REQUEST = 1;
    /**
     * Request for result from group.
     */
    public static final int GROUP_RESULT = 500;

    /**
     * Code indicating data invalidation.
     */
    public static final int DATA_INVALID = 10000;

    /**
     * URI for the table containing groups.
     */
    public static final Uri USER_GROUP = ServerContentProvider.USER_GROUP;
    /**
     * URI for the table containing users.
     */
    public static final Uri USERS      = ServerContentProvider.USERS;
    /**
     * URI for the table containing likes.
     */
    public static final Uri LIKES      = ServerContentProvider.LIKES;

    /**
     * Our ArrayList containing each picture to be entered into the listview.
     */
    private ContentResolver      resolver;
    /**
     * The current user's account.
     */
    private Account              account;
    /**
     * Spinner containing dropdown options for the search parameters.
     */
    private Spinner              spinner;
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
     * User's current search option.
     */
    private SearchOption         currentOption;
    /**
     * Spinner Dialog to display while synchronizing with the server.
     */
    private LoadingSpinnerDialog spinnerDialog;

    /**
     * Assigns values to the parameters. Sets up button listeners/
     *
     * @param savedInstanceState not used
     */
    @Override
    public void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);
        spinnerDialog = new LoadingSpinnerDialog();
        spinner = (Spinner) findViewById(R.id.spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter
                .createFromResource(this, R.array.search_options_array,
                                    android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        resolver = getContentResolver();
        account = ((AccountManager) getSystemService(Context.ACCOUNT_SERVICE))
                .getAccountsByType(GenericAccountService.ACCOUNT_TYPE)[0];
        lv = (ListView) findViewById(R.id.listView1);
        editText = (EditText) findViewById(R.id.searchEditText);
        search = (Button) findViewById(R.id.search);
        search.setOnClickListener(this);
        currentOption = SearchOption.USERS;
        Handler handler = new Handler();
        // register content providers in order to cease showing the spinner dialog when an update
        // is received.
        resolver.registerContentObserver(USER_GROUP, true, new SearchContentObserver(handler));
        resolver.registerContentObserver(LIKES, true, new SearchContentObserver(handler));
        resolver.registerContentObserver(USERS, true, new SearchContentObserver(handler));
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data)
    {
        // Check which request we're responding to
        if (requestCode == GROUP_RESULT)
        {
            // Make sure the request was successful
            if (resultCode == DATA_INVALID)
            {
                search.performClick();
            }
        }
    }

    /**
     * When an item is clicked, the appropriate page is opened.
     *
     * @param adapterView The adapterview for this list view.
     * @param view The view that was clicked.
     * @param i Not used
     * @param l Index of item we are looking for.
     */
    @Override
    public void onItemClick (AdapterView<?> adapterView, View view, int i, long l)
    {
        switch (currentOption)
        {
            // Displays a Groups page.
            case GROUPS:
            {
                Class page;
                Group group = ((Group) (adapterView.getAdapter().getItem((int) l)));
                if (group.getOwner().equals(account.name))
                {
                    page = EditGroup.class;
                }
                else
                {
                    page = GroupPage.class;
                }
                Intent intent = new Intent(this, page);
                intent.putExtra("group_name", group.getGroupName());
                intent.putExtra("group_description", group.getDescription());
                intent.putExtra("member", group.isMember());
                startActivityForResult(intent, GROUP_RESULT);
                break;
            }
            // Likes do not have their own pages.
            case LIKES:
            {
                break;
            }
            // Creates a user page.
            case USERS:
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
                break;
            }
        }
    }

    /**
     * <p>Callback method to be invoked when an item in this view has been selected. This callback
     * is invoked only when the newly selected position is different from the previously selected
     * position or if there was no selected item.</p>
     * <p>
     * Impelmenters can call getItemAtPosition(position) if they need to access the data associated
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
        // set current item string.
        lv.setAdapter(null);
        Object item = parent.getItemAtPosition(position);
        currentOption = SearchOption.selectOption(item.toString());
        Log.d("SPINNER", "Item string: " + item.toString());
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
        //required but not used.
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
            extras.putString("table0", currentOption.getTable());
            extras.putString("search", "%" + editText.getText().toString() + "%");
            ContentResolver.requestSync(account, getString(R.string.authority), extras);
            lv.setOnItemClickListener(this);
            spinnerDialog.show(getFragmentManager(), "Synchronizing with Server");
        }
    }

    /**
     * Private enum containing possible searchable options. Allows easy holding of data to provide
     * to list view.
     */
    private enum SearchOption
    {
        // Users option.
        USERS("users", "EMAIL", Search.USERS),
        // Groups option.
        GROUPS("user_group", "GROUP_NAME", USER_GROUP),
        // Likes option.
        LIKES("likes", "LIKE_LABEL", Search.LIKES);

        /**
         * Table related to this query type.
         */
        private String table;
        /**
         * Column used for query
         */
        private String query;
        /**
         * Associated table's URI.
         */
        private Uri    uri;

        /**
         * Constructor that assigns values for fields.
         *
         * @param table Table name.
         * @param query Relevant Query Column
         * @param uri URI to access table in ContentProvider.
         */
        SearchOption (String table, String query, Uri uri)
        {
            this.table = table;
            this.query = query;
            this.uri = uri;
        }

        /**
         * Chooses appropriate object for provided string. Similar to builder method design
         * pattern.
         *
         * @param option The input option.
         *
         * @return The object associated with the input option.
         */
        public static SearchOption selectOption (String option)
        {
            SearchOption returnOption = null;
            switch (option)
            {
                case "Users":
                {
                    returnOption = USERS;
                    break;
                }
                case "Groups":
                {
                    returnOption = GROUPS;
                    break;
                }
                case "Likes":
                {
                    returnOption = LIKES;
                    break;
                }
                default:
                {
                    Log.d("SearchOption", "Bug encountered: null returned from SearchOption");
                    break;
                }
            }
            return returnOption;
        }

        /**
         * Getter for table.
         *
         * @return table
         */
        public String getTable ()
        {
            return table;
        }

        /**
         * Getter for associated column.
         *
         * @return query
         */
        public String getQuery ()
        {
            return query;
        }

        /**
         * Getter for associated ContentProvider URI.
         *
         * @return uri
         */
        public Uri getUri ()
        {
            return uri;
        }
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
            // could be refactored to make these types more polymorphic.
            switch (currentOption)
            {
                case USERS:
                {
                    holder.txt1.setText(((User) (list.get(position))).getName());
                    break;
                }
                case LIKES:
                {
                    holder.txt1.setText(((Like) (list.get(position))).getLike());
                    break;
                }
                case GROUPS:
                {
                    holder.txt1.setText(((Group) (list.get(position))).getGroupName());
                    break;
                }

            }
            return convertView;
        }
    }

    /**
     * An observer for a contentProvider. Allows us to register a callback to stop displaying
     * a spinner dialog and take other action.
     */
    private class SearchContentObserver extends ContentObserver
    {
        public SearchContentObserver (Handler handler)
        {
            super(handler);

        }

        /**
         * Updates the list view and dismissed the dialog.
         * @param selfChange not used.
         */
        @Override
        public void onChange (boolean selfChange)
        {
            super.onChange(selfChange);
            // Build the list of each picture to be displayed in the listview.
            Log.d("GROUPS", "Resolver query");
            Cursor cursor = resolver.query(currentOption.getUri(), null, null, null, null);
            updateListView(cursor, lv);
            if (spinnerDialog != null)
            {
                spinnerDialog.dismiss();
            }
        }


        /**
         * Helper method for refreshing the list view with new data. In the future it may be
         * possible to refactor the different types of list to fit into one interface or abstract
         * class so that we may do this polymorphically.
         *
         * @param cursor Query information received from content provider to fill new listview.
         * @param lv Current list view.
         */
        private void updateListView (Cursor cursor, ListView lv)
        {
            lv.invalidateViews();
            switch (currentOption)
            {
                case USERS:
                {
                    buildUserList(cursor);
                    break;
                }
                case LIKES:
                {
                    buildLikeList(cursor);
                    break;
                }
                case GROUPS:
                {
                    buildGroupList(cursor);
                    break;
                }
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
                Log.d("BuildUserList", "Busy: " + cursor.getString(cursor.getColumnIndex("BUSY")));
                results.add(new User(email, birthday, gender, name, busy));//, eventLocation));
            }
            ExtendedArrayAdapter<User> ad = new ExtendedArrayAdapter<User>(getApplicationContext(),
                                                                           R.layout.group_list_item,
                                                                           R.id.groupname, results);
            lv.setAdapter(ad);
        }

        /**
         * Helper method for building a list of likes.
         *
         * @param cursor Contains all groups to add to the list.
         */
        private void buildLikeList (Cursor cursor)
        {
            ArrayList<Like> results = new ArrayList<Like>();
            while (cursor != null && cursor.moveToNext())
            {
                String likeLabel = cursor.getString(cursor.getColumnIndex("LIKE_LABEL"));
                results.add(new Like(likeLabel));//, eventLocation));
            }
            ExtendedArrayAdapter<Like> ad = new ExtendedArrayAdapter<Like>(getApplicationContext(),
                                                                           R.layout.group_list_item,
                                                                           R.id.groupname, results);
            lv.setAdapter(ad);
        }

        /**
         * Helper method for building a list of groups.
         *
         * @param cursor Contains all groups to add to the list.
         */
        private void buildGroupList (Cursor cursor)
        {
            ArrayList<Group> results = new ArrayList<Group>();
            while (cursor != null && cursor.moveToNext())
            {
                String groupName = cursor.getString(cursor.getColumnIndex("GROUP_NAME"));
                String groupDescription = cursor
                        .getString(cursor.getColumnIndex("GROUP_DESCRIPTION"));
                String owner = cursor.getString(cursor.getColumnIndex("OWNER"));
                boolean member = Boolean.valueOf(cursor.getString(cursor.getColumnIndex("MEMBER")));
                results.add(new Group(groupName, groupDescription, owner, member));
            }
            ExtendedArrayAdapter<Group> ad = new ExtendedArrayAdapter<Group>(
                    getApplicationContext(), R.layout.group_list_item, R.id.groupname, results);
            lv.setAdapter(ad);
        }
    }
}