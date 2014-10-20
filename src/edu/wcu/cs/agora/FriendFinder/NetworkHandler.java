package edu.wcu.cs.agora.FriendFinder;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

/**
 * Handles the data sent to and received from the server.
 *
 * Reference: Android docs simple request using Volley example
 *
 * Created by Karen on 10/20/2014.
 */
public class NetworkHandler {

    public void send(Context context, String message) {
        RequestQueue queue = Volley.newRequestQueue(context);

        //TODO: replace when figure out the server
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, Constants.URL,
                new Response.Listener() {
                    @Override
                    public void onResponse(Object response) {
                        String responseStr = (String) response;
                        // What happens with response.

                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // What happens when error
            }
        });

    }

}
