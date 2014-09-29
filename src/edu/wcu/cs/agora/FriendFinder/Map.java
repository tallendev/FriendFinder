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
 * Code for functionality on the log in page.
 */
public class Map extends Fragment
{

    /**
     * Currently the default fragment onCreateView.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.map, container, false);
    }
}