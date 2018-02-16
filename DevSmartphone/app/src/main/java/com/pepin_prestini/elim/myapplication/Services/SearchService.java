package com.pepin_prestini.elim.myapplication.Services;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.pepin_prestini.elim.myapplication.MainActivity;

import java.util.Hashtable;
import java.util.Map;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.pepin_prestini.elim.myapplication.Utils.HelperLocationStrategies;


/**
 * Created by Adrien on 14/02/2018.
 */

public class SearchService extends Service {

    private static final String TAG = "ELIM-GPS";
    public static final String REQUEST_STRING = "myRequest";
    public static final String RESPONSE_STRING = "myResponse";
    public static final String RESPONSE_MESSAGE = "myResponseMessage";
    LocationManager mLocationManager;
    private String URL = null;
    private static final int REGISTRATION_TIMEOUT = 3 * 1000;
    private static final int WAIT_TIMEOUT = 30 * 1000;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            sendToServer(intent.getStringExtra("searchWord"));
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("MissingPermission")
    private void sendToServer(final String search) {
        //I make a log to see the results

        // Get LocationManager object
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Create a criteria object to retrieve provider
        Criteria criteria = new Criteria();

        // Get the name of the best provider
        String provider = null;
        if (locationManager != null) {
            provider = locationManager.getBestProvider(criteria, true);
            // Get Current Location


            @SuppressLint("MissingPermission") Location myLocation = locationManager.getLastKnownLocation(provider);
            //latitude of location
            final double myLatitude = myLocation.getLatitude();

            //longitude og location
            final double myLongitude = myLocation.getLongitude();


            Toast.makeText(getApplicationContext(),"lat : " +  myLatitude +" lng : " + myLongitude, Toast.LENGTH_LONG ).show();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            System.out.println(search);
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(MainActivity.SearchServiceReceiver.PROCESS_RESPONSE);
            broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
            broadcastIntent.putExtra(RESPONSE_STRING, "hello");
            sendBroadcast(broadcastIntent);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, "http://172.20.10.3:3000/recherche", new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    //System.out.println(response);
                    Intent broadcastIntent = new Intent();
                    broadcastIntent.setAction(MainActivity.SearchServiceReceiver.PROCESS_RESPONSE);
                    broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
                    broadcastIntent.putExtra(RESPONSE_STRING, response);
                    sendBroadcast(broadcastIntent);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    System.out.println(error);
                }
            })
            {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> map = new Hashtable<>();
                    map.put("imei", "123456789" );
                    map.put("latitude", myLatitude+"" );
                    map.put("longitude", myLongitude +"");
                    map.put("mot", search);
                    return map;
                }
            };

            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            queue.add(stringRequest);



        }







    }
}
