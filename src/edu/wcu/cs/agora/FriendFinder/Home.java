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
 * Created by tyler on 11/2/2014.
 */
public class Home extends Activity
{
    @Override
    public void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        Fragment fragment1 = new Events();
        Fragment fragment2 = new Events(); //FIXME should be invitations

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.events, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        boolean success;
        // Handle item selection
        Intent i;
        switch (item.getItemId())
        {
            case R.id.create_event:
                success = true;
                i = new Intent(this, CreateEvent.class);
                startActivity(i);
                break;

            case R.id.search:
                success = true;
                i = new Intent(this, Search.class);
                startActivity(i);
                break;

            case R.id.view_profile:
                success = true;
                i = new Intent(this, Profile.class);
                startActivity(i);
                break;

            case R.id.settings:
                success = true;
                i = new Intent(this, Settings.class);
                startActivity(i);
                break;

            case R.id.logout:
                success = true;
                logout();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return success;
    }

    private void logout()
    {
        //FIXME delete user from shared preferences here!
        Intent i = new Intent(getApplicationContext(), Login.class);
        //finish all other existing activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }
}