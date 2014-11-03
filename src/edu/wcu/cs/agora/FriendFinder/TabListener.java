package edu.wcu.cs.agora.FriendFinder;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentTransaction;

/**
 * Created by tyler on 11/2/2014.
 */
public class TabListener implements ActionBar.TabListener {
    Fragment fragment;

    public TabListener (Fragment fragment)
    {
        this.fragment = fragment;

    }//end tabListener

    @Override
    public void onTabSelected (ActionBar.Tab tab, FragmentTransaction ft)
    {
        ft.replace(android.R.id.content, fragment);
    }//endOnTabSelected

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft)
    {
        ft.remove(fragment);
    }//endOnTabUnselected

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft)
    {
    }//end onTabReselected
}