package edu.wcu.cs.agora.FriendFinder;

import android.app.Activity;
import android.os.Bundle;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;


/**
 * @author Tyler Allen
 * @created 09/29/2014
 * @version 12/8/2014
 * Google maps implementation. Not used as of yet.
 */
public class Map extends Activity
{

    private GoogleMap map;

    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);

        // Get the map fragment
        map = ((MapFragment) getFragmentManager().findFragmentById(R.layout.map)).getMap();
        map.setMyLocationEnabled(true);
    }
}