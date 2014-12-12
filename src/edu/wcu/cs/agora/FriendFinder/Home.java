package edu.wcu.cs.agora.FriendFinder;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/**
 * @author Tyler Allen
 * @created 11/2/2014
 * @version 12/7/2014
 *
 * The home page containing a list of the user's current events.
 */
public class Home extends Activity
{

    /**
     * Creates tabs on page and attaches listeners.
     *
     * @param savedInstanceState not used
     */
    @Override
    public void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        Fragment fragment1 = new Events();
        Fragment fragment2 = new Invites(); //FIXME should be invitations

        ActionBar actionBar = getActionBar();

        // Hide Actionbar Icon
        actionBar.setDisplayShowHomeEnabled(false);
        // Hide Actionbar Title
        actionBar.setDisplayShowTitleEnabled(false);
        // Create Actionbar Tabs
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        ActionBar.Tab tab1 = actionBar.newTab().setIcon(R.drawable.group);
        ActionBar.Tab tab2 = actionBar.newTab().setIcon(R.drawable.invite);

        tab1.setTabListener(new TabListener(fragment1));
        tab2.setTabListener(new TabListener(fragment2));

        actionBar.addTab(tab1);
        actionBar.addTab(tab2);
    }


    /**
     * Creates menu when options button is pressed.
     *
     * @param menu Menu to be inflated.
     *
     * @return True
     */
    @Override
    public boolean onCreateOptionsMenu (Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.events, menu);
        return true;
    }


    /**
     * Determines actions taken when a menu item is selected.
     * @param item The item that was selected.
     * @return True
     */
    @Override
    public boolean onOptionsItemSelected (MenuItem item)
    {
        // Handle item selection
        Intent i;
        switch (item.getItemId())
        {
            case R.id.create_event:
                i = new Intent(this, CreateEvent.class);
                startActivity(i);
                break;

            case R.id.search:
                i = new Intent(this, Search.class);
                startActivity(i);
                break;

            case R.id.view_profile:
                i = new Intent(this, Profile.class);
                Bundle extras = new Bundle();
                extras.putBoolean("owner", true);
                i.putExtras(extras);
                startActivity(i);
                break;

            case R.id.settings:
                i = new Intent(this, Settings.class);
                startActivity(i);
                break;

            case R.id.logout:
                logout();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    /**
     * Functionality for logging out of the application.
     * <p>
     * TODO: finish me
     */
    private void logout ()
    {
        //FIXME delete user from shared preferences here
        Intent i = new Intent(getApplicationContext(), Login.class);
        //finish all other existing activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

}