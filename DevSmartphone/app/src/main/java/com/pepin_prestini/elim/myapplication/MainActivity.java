package com.pepin_prestini.elim.myapplication;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.pepin_prestini.elim.myapplication.Services.GPSService;
import com.pepin_prestini.elim.myapplication.Services.SearchService;
import com.pepin_prestini.elim.myapplication.Utils.HelperLocationStrategies;
import com.pepin_prestini.elim.myapplication.databinding.ActivityMainBinding;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding viewDataBinding;
    private MaterialSearchView materialSearchView;
    private ListView listView;

    private SearchServiceReceiver receiverSearch;
    private GPSServiceReceiver receiverGPS;
    public static final int MY_PERMISSIONS_REQUEST_GPS = 123;

    private boolean GPSEnable;
    private ArrayList<Place> places;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setSupportActionBar(viewDataBinding.toolbar);


        registerService();

        viewSearchCode();
        gpsPreview();

        example();
    }

    private void example() {
        places = new ArrayList<Place>() {
            {
                add(new Place("Adrien", "87 chemin du moulin", R.drawable.common_ic_googleplayservices));
                add(new Place("Adrien", "87 chemin du moulin", R.drawable.common_ic_googleplayservices));
            }
        };

        PlaceAdapter adapter = new PlaceAdapter(getApplicationContext(),
                R.layout.row, places);


        listView = findViewById(R.id.list_view);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Place place = (Place) parent.getItemAtPosition(position);
                System.out.println(place);
                String adress = place.adresse.replace(' ','+');
                Uri gmmIntentUri = Uri.parse("google.navigation:q="+adress);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });
    }

    private void gpsPreview() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // Permission is already available, start camera preview
            /*Snackbar.make(findViewById(R.id.main_layout),
                    "La permission de localisation GPS  is available. Starting preview.",
                    Snackbar.LENGTH_SHORT).show();*/
            startGPS();
        } else {
            // Permission is missing and must be requested.
            requestLocationPermission();
        }
    }

    private void registerService() {
        //filter for search
        IntentFilter filterSearch = new IntentFilter(SearchServiceReceiver.PROCESS_RESPONSE);
        filterSearch.addCategory(Intent.CATEGORY_DEFAULT);
        receiverSearch = new SearchServiceReceiver();
        registerReceiver(receiverSearch, filterSearch);

        //filter for GPS
        IntentFilter filter = new IntentFilter(GPSServiceReceiver.PROCESS_RESPONSE);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiverGPS = new GPSServiceReceiver();
        registerReceiver(receiverGPS, filter);
    }

    private void startGPS() {
        activateGPS();
        if (GPSEnable) {
            createNotification();
            startService(new Intent(getApplicationContext(), GPSService.class));
        }
    }

    private void createNotification() {

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.icon_plan)
                        .setContentTitle("ELIM activé")
                        .setContentText("L'application récupère vos données GPS")
                        .setOngoing(true);

        Intent notificationIntent = new Intent(this, this.getClass());
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //builder.setVibrate(new long[] {0,200,100,200,100,200});
        if (manager != null) {
            manager.notify(0, builder.build());
        }
    }

    private void viewSearchCode() {

        materialSearchView =  findViewById(R.id.search_view);
        materialSearchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //Do some magic
                Toast.makeText(getApplicationContext(), query, Toast.LENGTH_LONG).show();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Do some magic
                return false;
            }
        });

        materialSearchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                //Do some magic
            }

            @Override
            public void onSearchViewClosed() {
                //Do some magic
            }
        });
        materialSearchView.setVoiceSearch(true);

        materialSearchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //Do some magic
                Intent intent = new Intent(getApplicationContext(), SearchService.class);
                intent.putExtra("searchWord", query);
                startService(intent);

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Do some magic
                return false;
            }
        });

        materialSearchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                //Do some magic
            }

            @Override
            public void onSearchViewClosed() {
                //Do some magic
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MaterialSearchView.REQUEST_VOICE && resultCode == RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null && matches.size() > 0) {
                String searchWrd = matches.get(0);
                if (!TextUtils.isEmpty(searchWrd)) {
                    materialSearchView.setQuery(searchWrd, false);
                }
            }

            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        viewDataBinding.searchView.setMenuItem(menu.findItem(R.id.action_settings));
        return true;
    }
    @Override
    public void onBackPressed() {
        if (materialSearchView.isSearchOpen()) {
            materialSearchView.closeSearch();
        } else {
            super.onBackPressed();
        }
    }




    private void requestLocationPermission() {
        // Permission has not been granted and must be requested.
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // Display a SnackBar with a button to request the missing permission.
            Snackbar.make(findViewById(R.id.activity_main), "Le GPS est necessaire pour accèder à la position",
                    Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Request the permission
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            MY_PERMISSIONS_REQUEST_GPS);
                }
            }).show();

        } else {
            Snackbar.make(findViewById(R.id.activity_main),
                    "Permission non disponible. Demande de permission pour le GPS",
                    Snackbar.LENGTH_SHORT).show();
            // Request the permission. The result will be received in onRequestPermissionResult().
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_GPS);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // BEGIN_INCLUDE(onRequestPermissionsResult)
        if (requestCode == MY_PERMISSIONS_REQUEST_GPS) {
            // Request for camera permission.
            if ((grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission has been granted. GPS camera preview Activity.
                /*Snackbar.make(findViewById(R.id.main_layout), "GPS autorisé. Démarrage du GPS.",
                        Snackbar.LENGTH_SHORT)
                        .show();*/
                startGPS();
            } else {
                // Permission request was denied.
                /*Snackbar.make(findViewById(R.id.main_layout), "Le GPS n'est pas autorisé.",
                        Snackbar.LENGTH_SHORT)
                        .show();*/
            }
        }
        // END_INCLUDE(onRequestPermissionsResult)
    }


    private void activateGPS() {
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        if (manager != null) {
            GPSEnable = manager.isProviderEnabled( LocationManager.GPS_PROVIDER );
        }
        if (manager != null && (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER) || !manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))) {

            // Build the alert dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Location Services Not Active");
            builder.setMessage("Please enable Location Services and GPS");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    // Show location settings when the user acknowledges the alert dialog
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
            Dialog alertDialog = builder.create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
        }

    }
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        unregisterReceiver(this.receiverSearch);
        unregisterReceiver(this.receiverGPS);
        stopService(new Intent(getApplicationContext(), SearchService.class));
        //stopService(new Intent(getApplicationContext(), GPSService.class));
        NotificationManager notifManager= (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (notifManager != null) {
            notifManager.cancelAll();
        }

    }


    public class SearchServiceReceiver extends BroadcastReceiver {

        public static final String PROCESS_RESPONSE = "com.pepin_prestini.elim.myapplication.PROCESS_RESPONSE_SEARCH";

        @Override
        public void onReceive(Context context, Intent intent) {

            String responseString = intent.getStringExtra(SearchService.RESPONSE_STRING);
            System.out.println(responseString);
            //Toast.makeText(getApplicationContext(), responseString,Toast.LENGTH_LONG).show();
            stopService(new Intent(getApplicationContext(), SearchService.class));
            //mots = new String[]{"Adrien", "Nicolas"};


            places.add(new Place("Adrien", "87 chemin du moulin", R.mipmap.gmaps));
            places.add(new Place("Estelle", "18 rue acchiardi de saint léger, Nice, france", R.mipmap.gmaps));
            places.add(new Place("Adrien", "87 chemin du moulin", R.mipmap.gmaps));
            places.add(new Place("Adrien", "87 chemin du moulin", R.mipmap.gmaps));
            places.add(new Place("Adrien", "87 chemin du moulin", R.mipmap.gmaps));
            places.add(new Place("Adrien", "87 chemin du moulin", R.mipmap.gmaps));
            places.add(new Place("Adrien", "87 chemin du moulin", R.mipmap.gmaps));
            places.add(new Place("Adrien", "87 chemin du moulin", R.mipmap.gmaps));
            places.add(new Place("Adrien", "87 chemin du moulin", R.mipmap.gmaps));
            places.add(new Place("Adrien", "87 chemin du moulin",R.mipmap.gmaps));



        }
    }

    public class GPSServiceReceiver extends BroadcastReceiver {

        public static final String PROCESS_RESPONSE = "com.pepin_prestini.elim.myapplication.PROCESS_RESPONSE_GPS";

        @Override
        public void onReceive(Context context, Intent intent) {
            Double defaultValue = 0.0;
            Double responseLatitude = intent.getDoubleExtra(GPSService.LATITUDE_STRING, defaultValue);
            Double responseLongitude = intent.getDoubleExtra(GPSService.LONGITUDE_STRING, defaultValue);
            Double responseAltitude = intent.getDoubleExtra(GPSService.ALTITUDE_STRING, defaultValue);
            //Toast.makeText(getApplicationContext(), responseLatitude + "/" + responseLongitude + "/" + responseAltitude,Toast.LENGTH_LONG).show();
            //stopService(new Intent(getApplicationContext(), GPSService.class));
        }
    }
}
