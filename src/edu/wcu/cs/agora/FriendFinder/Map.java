package edu.wcu.cs.agora.FriendFinder;

import android.app.Activity;
import android.os.Bundle;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;


/**
 * Tyler Allen
 * Karen Dana
 * 09/29/2014
 *
 * Code for functionality on the log in page.
 */
public class Map extends Activity {

    private GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);

        // Get the map fragment
        map = ((MapFragment) getFragmentManager().findFragmentById(R.layout.map)).getMap();
        map.setMyLocationEnabled(true);
    }
}