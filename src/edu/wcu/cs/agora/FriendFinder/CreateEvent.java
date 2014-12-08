package edu.wcu.cs.agora.FriendFinder;

import android.app.Activity;
import android.os.Bundle;

/**
 * @author Tyler Allen
 * @created 11/2/2014.
 * @version 12/08/2014
 *
 * Page used when a user is creating their own event.
 */
public class CreateEvent extends Activity
{

    /**
     * Default onCreate for CreateEvent.
     *
     * @param savedInstanceState not used
     */
    @Override
    public void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_event);
    }
}