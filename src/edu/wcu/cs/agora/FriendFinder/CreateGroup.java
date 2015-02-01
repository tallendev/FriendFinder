package edu.wcu.cs.agora.FriendFinder;

import android.app.Activity;
import android.os.Bundle;

/**
 * @author Tyler Allen
 * @version 2/1/2015
 *          <p>
 *          Page used when a user is creating a group.
 * @created 2/1/2015
 */
public class CreateGroup extends Activity
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