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
 * Created by tyler on 11/2/2014.
 */
public class Search extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener
{

    /** The request status when requesting a PicInfo class.*/
    public static final int REQUEST = 1;

    public static final Uri USER_GROUP = Uri.parse(ServerContentProvider.CONTENT_URI + "/user_group");
    public static final Uri USERS = Uri.parse(ServerContentProvider.CONTENT_URI + "/users");
    public static final Uri LIKES = Uri.parse(ServerContentProvider.CONTENT_URI + "/likes");

    /** Our ArrayList containing each picture to be entered into the listview. */
    private ContentResolver resolver;
    private Account account;
    private Spinner spinner;
    private ListView lv;
    private EditText editText;
    private Button search;
    private SearchOption currentOption;
    private LoadingSpinnerDialog spinnerDialog;

    @Override
    public void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);
        spinnerDialog = new LoadingSpinnerDialog();
        spinner = (Spinner) findViewById(R.id.spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.search_options_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        resolver = getContentResolver();
        account = ((AccountManager) getSystemService(Context.ACCOUNT_SERVICE)).getAccounts()[0];
        lv = (ListView) findViewById(R.id.listView1);
        editText = (EditText) findViewById(R.id.searchEditText);
        search = (Button) findViewById(R.id.search);
        search.setOnClickListener(this);
        currentOption = SearchOption.USERS;
        Handler handler = new Handler();
        resolver.registerContentObserver(USER_GROUP, true, new SearchContentObserver(handler));
        resolver.registerContentObserver(LIKES, true, new SearchContentObserver(handler));
        resolver.registerContentObserver(USERS, true, new SearchContentObserver(handler));
    }

    /**
     * Here we do all the main work of setting up the list, so that it is
     * regenerated every time we return to this screen in the event of file
     * system changes from other fragments/activities.
     */
    public void onResume()
    {
        super.onResume();
    }


    /**
     * When an item is clicked, the appropriate pic is opened up in the PicInfo
     * class.
     *
     * @param adapterView The adapterview for this list view.
     * @param view        The view that was clicked.
     * @param i           Not used
     * @param l           Index of item we are looking for.
     */
    @Override
    public void onItemClick (AdapterView<?> adapterView, View view,
                             int i, long l)
    {
        switch (currentOption)
        {
            case GROUPS:
            {
                Intent intent = new Intent(this, GroupPage.class);
                Group group = ((Group) (adapterView.getAdapter().getItem((int) l)));
                intent.putExtra("group_name", group.getGroupName());
                intent.putExtra("group_description", group.getDescription());
                startActivityForResult(intent, REQUEST);
                break;
            }
            case LIKES:
            {
                break;
            }
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
                startActivityForResult(intent, REQUEST);
                break;
            }
        }
    }

    /**
     * <p>Callback method to be invoked when an item in this view has been
     * selected. This callback is invoked only when the newly selected
     * position is different from the previously selected position or if
     * there was no selected item.</p>
     * <p>
     * Impelmenters can call getItemAtPosition(position) if they need to access the
     * data associated with the selected item.
     *
     * @param parent   The AdapterView where the selection happened
     * @param view     The view within the AdapterView that was clicked
     * @param position The position of the view in the adapter
     * @param id       The row id of the item that is selected
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        lv.setAdapter(null);
        Object item = parent.getItemAtPosition(position);
        currentOption = SearchOption.selectOption(item.toString());
        Log.d("SPINNER", "Item string: " + item.toString());
    }

    /**
     * Callback method to be invoked when the selection disappears from this
     * view. The selection can disappear for instance when touch is activated
     * or when the adapter becomes empty.
     *
     * @param parent The AdapterView that now contains no selected item.
     */
    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {

    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v)
    {
        if (v.getId() == R.id.search)
        {
            Bundle extras = new Bundle();
            extras.putString("request_type", "3");
            extras.putString("table0", currentOption.getServerColumn());
            extras.putString("search", "%" + editText.getText().toString() + "%");
            ContentResolver.requestSync(account, getString(R.string.authority), extras);
            lv.setOnItemClickListener(this);
            spinnerDialog.show(getFragmentManager(), "Synchronizing with Server");
        }
    }

    /**
     * Extension of the ArrayAdapter class, using Websites as an object located
     * inside each list view item.
     *
     * @param <T>
     */
    private class ExtendedArrayAdapter<T> extends ArrayAdapter<T>
    {
        LayoutInflater inflater;
        List<T> list;

        /**
         * Sets the fields.
         *
         * @param context   Passed to super.
         * @param layout    Passed to super.
         * @param txtLayout The txtLayout we are modifying in the list item
         *                  view.
         * @param list      The list of objects we put in our list view.
         */
        public ExtendedArrayAdapter (Context context, int layout,
                                     int txtLayout, List<T> list)
        {
            super(context, layout, txtLayout, list);
            this.list = list;
            inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        /**
         * Returns the view requested.
         *
         * @param position    Not used
         * @param convertView the view we are converting.
         * @param parent      Not used.
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
     * Holder pattern object containing a text view.
     */
    public static class ViewHolder
    {
        /** First text view holding name.*/
        public TextView txt1;
    }

    private enum SearchOption
    {
        USERS("users", "EMAIL", Search.USERS),
        GROUPS("user_group", "GROUP_NAME", USER_GROUP),
        LIKES("likes", "LIKE_LABEL",  Search.LIKES);

        private String serverColumn;
        private String query;
        private Uri uri;

        private SearchOption(String serverColumn, String query, Uri uri)
        {
            this.serverColumn = serverColumn;
            this.query = query;
            this.uri = uri;
        }

        public static SearchOption selectOption(String option)
        {
            SearchOption returnOption= null;
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

        public String getServerColumn()
        {
            return serverColumn;
        }

        public String getQuery()
        {
            return query;
        }

        public Uri getUri()
        {
            return uri;
        }
    }

    private class SearchContentObserver extends ContentObserver
    {
        public SearchContentObserver(Handler handler)
        {
            super(handler);

        }

        @Override
        public void onChange(boolean selfChange)
        {
            super.onChange(selfChange);
            // Build the list of each picture to be displayed in the listview.
            Log.d("GROUPS", "Resolver query");
            //String[] projArgs = {"%" + editText.getText().toString() + "%"};
            //Cursor cursor = resolver.query(currentOption.getUri(), null, currentOption.getQuery() + " like ?", projArgs, null);
            Cursor cursor = resolver.query(currentOption.getUri(), null, null, null, null);
            updateListView(cursor, lv);
            spinnerDialog.dismiss();
        }


        private void updateListView(Cursor cursor, ListView lv)
        {
            lv.invalidateViews();
            switch (currentOption)
            {
                case USERS:
                {
                    ArrayList<User> results = new ArrayList<User>();
                    while (cursor != null && cursor.moveToNext())
                    {
                        String email = cursor.getString(cursor.getColumnIndex("EMAIL"));
                        String birthday = cursor.getString(cursor.getColumnIndex("BIRTHDAY"));
                        String gender = cursor.getString(cursor.getColumnIndex("GENDER"));
                        String name = cursor.getString(cursor.getColumnIndex("FULL_NAME"));
                        results.add(new User(email, birthday, gender, name));//, eventLocation));
                    }
                    ExtendedArrayAdapter<User> ad = new ExtendedArrayAdapter<User>
                            (getApplicationContext(), R.layout.group_list_item, R.id.groupname, results);
                    lv.setAdapter(ad);
                    break;
                }
                case LIKES:
                {
                    ArrayList<Like> results = new ArrayList<Like>();
                    while (cursor != null && cursor.moveToNext())
                    {
                        String likeLabel = cursor.getString(cursor.getColumnIndex("LIKE_LABEL"));
                        results.add(new Like(likeLabel));//, eventLocation));
                    }
                    ExtendedArrayAdapter<Like> ad = new ExtendedArrayAdapter<Like>
                            (getApplicationContext(), R.layout.group_list_item, R.id.groupname, results);
                    lv.setAdapter(ad);
                    break;
                }
                case GROUPS:
                {
                    ArrayList<Group> results = new ArrayList<Group>();
                    while (cursor != null && cursor.moveToNext())
                    {
                        String groupName = cursor.getString(cursor.getColumnIndex("GROUP_NAME"));
                        String groupDescription = cursor.getString(cursor.getColumnIndex("GROUP_DESCRIPTION"));
                        results.add(new Group(groupName, groupDescription));//, eventLocation));
                    }
                    ExtendedArrayAdapter<Group> ad = new ExtendedArrayAdapter<Group>
                            (getApplicationContext(), R.layout.group_list_item, R.id.groupname, results);
                    lv.setAdapter(ad);
                    break;
                }

            }
        }
    }
}