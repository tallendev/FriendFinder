package edu.wcu.cs.agora.FriendFinder;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author Tyler Allen
 * @created 12/12/2014
 * @version 12/12/2014
 *
 * A fragment that will contain a ListView of current pending invitations.
 */
public class Invites extends Fragment
{
    /**
     * The root layout of this fragment.
     */
    private View rootView;

    @Override
    public void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    /**
     * Initializes fields and creates a ContentObserver to receive notification of the
     * ContentProvider completing an update.
     *
     * @param inflater Used to inflate the view.
     * @param container View's container.
     * @param savedInstanceState not used
     *
     * @return The view created.
     */
    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState)
    {
        rootView = inflater.inflate(R.layout.events_list, container, false);
        return rootView;
    }
}