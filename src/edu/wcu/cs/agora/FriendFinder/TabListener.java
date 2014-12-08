package edu.wcu.cs.agora.FriendFinder;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentTransaction;

/**
 * @author Tyler Allen
 * @created 11/2/2014
 * @version 12/7/2014
 *
 * A listener for tab clicks on a page where tab switching is available.
 */
public class TabListener implements ActionBar.TabListener
{
    /**
     * Current available fragment.
     */
    private Fragment fragment;

    /**
     * Constructor for this listener.
     *
     * @param fragment The current fragment.
     */
    public TabListener (Fragment fragment)
    {
        this.fragment = fragment;

    }

    /**
     * Swap fragment in to view.
     *
     * @param tab not used
     * @param ft not used
     */
    @Override
    public void onTabSelected (ActionBar.Tab tab, FragmentTransaction ft)
    {
        ft.replace(android.R.id.content, fragment);
    }//endOnTabSelected

    /**
     * Removes fragment from view.
     * @param tab not used
     * @param ft not used
     */
    @Override
    public void onTabUnselected (ActionBar.Tab tab, FragmentTransaction ft)
    {
        ft.remove(fragment);
    }//endOnTabUnselected

    /**
     * Required but not used.
     * @param tab not used
     * @param ft not used
     */
    @Override
    public void onTabReselected (ActionBar.Tab tab, FragmentTransaction ft)
    {
    }//end onTabReselected
}