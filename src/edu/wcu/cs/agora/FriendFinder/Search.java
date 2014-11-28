package edu.wcu.cs.agora.FriendFinder;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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
 * Created by tyler on 11/2/2014.
 */
public class Search extends Activity implements AdapterView.OnItemClickListener
{

    /** The request status when requesting a PicInfo class.*/
    public static final int REQUEST = 1;

    /** Our ArrayList containing each picture to be entered into the listview. */
    private ArrayList<Group> results;
    private ContentResolver resolver;
    private Account account;

    @Override
    public void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        resolver = getContentResolver();
        account = ((AccountManager) getSystemService(Context.ACCOUNT_SERVICE)).getAccounts()[0];
    }

    /**
     * Here we do all the main work of setting up the list, so that it is
     * regenerated every time we return to this screen in the event of file
     * system changes from other fragments/activities.
     */
    public void onResume()
    {
        super.onResume();
        Bundle extras = new Bundle();
        extras.putString("request_type", "3");
        extras.putString("table0", "user_group");
        ContentResolver.requestSync(account, getString(R.string.authority), extras);
        ListView lv = (ListView) findViewById(R.id.listView1);
        results = new ArrayList<Group>();
        // Build the list of each picture to be displayed in the listview.
        Log.d("GROUPS", "Resolver query");
        Cursor cursor = resolver.query(Uri.parse(ServerContentProvider.CONTENT_URI + "/user_group"), null, null, null, null);
        while (cursor != null && cursor.moveToNext())
        {
            Log.d("EVENTS", "cursor != null");
            String groupName = cursor.getString(cursor.getColumnIndex("GROUP_NAME"));
            String groupDescription = cursor.getString(cursor.getColumnIndex("GROUP_DESCRIPTION"));
//            String eventLocation = cursor.getString(cursor.getColumnIndex("EVENT_LOCATION"));
            results.add(new Group(groupName, groupDescription));//, eventLocation));
        }
        // Create our list.
        Log.d("EVENTS", "ExtendedArray");
        ExtendedArrayAdapter<Group> ad = new ExtendedArrayAdapter<Group>
                (this, R.layout.group_list_item, R.id.groupname,
                        results);

        lv.setAdapter(ad);
        lv.setOnItemClickListener(this);
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
        Intent intent = new Intent(this, GroupPage.class);
        Group group = ((Group) (adapterView.getAdapter().getItem((int) l)));
        intent.putExtra("group_name", group.getGroupName());
        intent.putExtra("group_description", group.getDescription());
        startActivityForResult(intent, REQUEST);
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
                holder.txt1 = (TextView) convertView.findViewById(R.id.groupname);//FIXME
                convertView.setTag(holder);
            }
            else
            {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.txt1.setText(((Event) (list.get(position))).getEventName());
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
}