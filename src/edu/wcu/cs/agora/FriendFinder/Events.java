package edu.wcu.cs.agora.FriendFinder;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Tyler Allen
 * 09/29/2014
 *
 * Code for functioning the events page.
 */

public class Events extends Fragment
{
    @Override

    /**
     * Currently the default fragment onCreate.
     */
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.events, container, false);
    }
}