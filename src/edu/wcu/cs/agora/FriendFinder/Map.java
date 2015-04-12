package edu.wcu.cs.agora.FriendFinder;

import android.app.*;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


/**
 * @author Tyler Allen
 * @created 09/29/2014
 * @version 3/1/2015
 * Google maps implementation. Not used as of yet.
 */
public class Map extends Activity implements GoogleMap.OnMarkerDragListener
{
    public static final LatLng DEFAULT_LAT_LNG = new LatLng(35.309523, -83.187878);
    /**
     * Map location change.
     */
    public static final int    MAP_LOCATION    = 12345;


    private GoogleMap map;
    private LatLng location;
    private Marker mark;

    public static String getDefaultLocation ()
    {
        return DEFAULT_LAT_LNG.latitude + " " + DEFAULT_LAT_LNG.longitude;
    }

    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);

        // Get the map fragment
        FragmentManager fmanager = getFragmentManager();
        map = ((MapFragment) fmanager.findFragmentById(R.id.map)).getMap();
        map.setMyLocationEnabled(true);
        String sLocation = getIntent().getExtras().getString("location");
        if (sLocation == null)
        {
            location = DEFAULT_LAT_LNG;
        }
        else
        {
            String[] split = sLocation.split("\\s+");
            location = new LatLng(Double.parseDouble(split[0]), Double.parseDouble(split[1]));
        }
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 13));
        boolean draggable = getIntent().getExtras().getBoolean("owner");
        mark = map.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(
                BitmapDescriptorFactory.HUE_AZURE)).
                                                        position(location).draggable(draggable));
        map.setOnMarkerDragListener(this);
    }

    @Override
    public void onMarkerDragStart (Marker marker)
    {

    }

    @Override
    public void onMarkerDrag (Marker marker)
    {

    }

    @Override
    public void onMarkerDragEnd (Marker marker)
    {
        Log.d("MAP", "MarkerDragEnd");
        location = marker.getPosition();
        DialogFragment dialog = new DialogFragment()
        {
            @Override
            public Dialog onCreateDialog (Bundle savedInstanceState)
            {
                // Use the Builder class for convenient dialog construction
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(R.string.change_location)
                       .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
                       {
                           @Override
                           public void onClick (DialogInterface dialog, int which)
                           {
                               Intent i = new Intent();
                               i.putExtra("location", location.latitude + " " + location.longitude);
                               getActivity().setResult(MAP_LOCATION, i);
                               getActivity().finish();
                           }
                       }).setNegativeButton(R.string.cancel, null);
                // Create the AlertDialog object and return it
                return builder.create();
            }
        };
        dialog.show(getFragmentManager(), "Are you sure?");
    }
}