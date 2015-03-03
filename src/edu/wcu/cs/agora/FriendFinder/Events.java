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
 * @author Tyler Allen
 * @created 09/29/2014
 * @version 12/7/2014
 * Code for the Fragment that contains events.
 */

public class Events extends Fragment implements AdapterView.OnItemClickListener
{
    /**
     * Request for result from event.
     */
    public static final int EVENT_RESULT = 500;

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
    public static final Uri EVENTS  = Uri.parse(ServerContentProvider.CONTENT_URI + "/event");

    /**
     * RootView of this fragment.
     */
    private View                 rootView;
    /**
     * Our ArrayList containing each event to be entered into the ListView.
     */
    private ArrayList<Event>     events;
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
        Event event = ((Event) (adapterView.getAdapter().getItem((int) l)));

        Class page;
        if (account.name.equals(event.getCreator()))
        {
            page = EditEvent.class;
        }
        else
        {
            page = EventsPage.class;
        }
        Intent intent = new Intent(getActivity(), page);
        intent.putExtra("event_name", event.getEventName());
        intent.putExtra("event_date", event.getEventDate());
        intent.putExtra("event_time", event.getEventTime());
        intent.putExtra("description", event.getDescription());
        intent.putExtra("id", event.getId());
        intent.putExtra("location", event.getLocation());
        Log.d("EVENTS", "event.getId: " + event.getId());
        intent.putExtra("attending", event.isAttending());
        intent.putExtra("invited", event.isInvited());
        startActivityForResult(intent, REQUEST);
    }

    @Override
    public void onActivityResult (int requestCode, int resultCode, Intent data)
    {
        // Check which request we're responding to
        if (requestCode == EVENT_RESULT)
        {
            // Make sure the request was successful
            if (resultCode == DATA_INVALID)
            {
                Bundle extras = new Bundle();
                extras.putString("request_type", "3");
                extras.putString("table0", "event");
                extras.putString("search", "%%");
                resolver.registerContentObserver(EVENTS, true,
                                                 new SyncContentObserver(new Handler()));
                ContentResolver
                        .requestSync(account, getActivity().getString(R.string.authority), extras);
                spinnerDialog.show(getFragmentManager(), "Synchronizing...");
                spinnerShowing = true;
            }
        }
    }

    /**
     * Default.
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
     * @param inflater Used to inflate the view.
     * @param container View's container.
     * @param savedInstanceState not used
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
        events = new ArrayList<Event>();
        if (savedInstanceState == null)
        {
            Bundle extras = new Bundle();
            extras.putString("request_type", "3");
            extras.putString("table0", "event");
            extras.putString("search", "%%");
            resolver.registerContentObserver(EVENTS, true, new SyncContentObserver(new Handler()));
            ContentResolver
                    .requestSync(account, getActivity().getString(R.string.authority), extras);
            spinnerDialog.show(getFragmentManager(), "Synchronizing...");
            spinnerShowing = true;
        }

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
        Bundle extras = new Bundle();
        extras.putString("request_type", "3");
        extras.putString("table0", "event");
        extras.putString("search", "%%");
        ContentResolver.requestSync(account, getActivity().getString(R.string.authority), extras);
        Log.d("EVENTS", "Resolver query");
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
                convertView = inflater.inflate(R.layout.events_list_item, null);
                holder.txt1 = (TextView) convertView.findViewById(R.id.eventname);
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
            String[] args = {"false"};
            Cursor cursor = resolver.query(EVENTS, null, "INVITED = ?", args, null);
            //Cursor cursor = resolver.query(EVENTS, null, null, null, null);
            events = new ArrayList<>();
            while (cursor.moveToNext())
            {
                Log.d("EVENTS", "cursor != null");
                // retrieve data from content provider element
                String eventName = cursor.getString(cursor.getColumnIndex("EVENT_NAME"));
                String eventDate = cursor.getString(cursor.getColumnIndex("EVENT_DATE"));
                String eventTime = cursor.getString(cursor.getColumnIndex("EVENT_TIME"));
                String creator = cursor.getString(cursor.getColumnIndex("CREATOR"));
                String id = cursor.getString(cursor.getColumnIndex("ID"));
                String description = cursor.getString(cursor.getColumnIndex("DESCRIPTION"));
                String location = cursor.getString(cursor.getColumnIndex("LOCATION_VALUE"));
                boolean attending = Boolean
                        .valueOf(cursor.getString(cursor.getColumnIndex("ATTENDING")));
                boolean invited = Boolean
                        .valueOf(cursor.getString(cursor.getColumnIndex("INVITED")));
                Log.d("EVENTS", "ID: " + id);
                events.add(new Event(eventName, eventDate, eventTime, description, creator, id,
                                     attending, location, invited));
            }
            Log.d("EVENTS", "ExtendedArray: ");
            for (Event event : events)
            {
                Log.d("EVENTS", event.toString());
            }
            // Create our list.
            ExtendedArrayAdapter<Event> ad = new ExtendedArrayAdapter<Event>(rootView.getContext(),
                                                                             R.layout.events_list_item,
                                                                             R.id.eventname,
                                                                             events);
            lv.setAdapter(ad);

            lv.setOnItemClickListener(Events.this);
            if (spinnerShowing)
            {
                spinnerDialog.dismiss();
                spinnerShowing = false;
            }
        }
    }
}