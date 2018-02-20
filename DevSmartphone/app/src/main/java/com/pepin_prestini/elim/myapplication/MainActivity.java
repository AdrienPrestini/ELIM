package com.pepin_prestini.elim.myapplication;

import android.Manifest;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.arch.persistence.room.Room;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.Menu;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.pepin_prestini.elim.myapplication.Services.GPSService;
import com.pepin_prestini.elim.myapplication.Services.SearchService;
import com.pepin_prestini.elim.myapplication.Utils.AppDatabase;
import com.pepin_prestini.elim.myapplication.Utils.Places.Place;
import com.pepin_prestini.elim.myapplication.databinding.ActivityMainBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding viewDataBinding;
    private MaterialSearchView materialSearchView;
    private ListView listView;
    PlaceAdapter adapter;
    AlphaAnimation inAnimation;
    AlphaAnimation outAnimation;

    FrameLayout progressBarHolder;

    private SearchServiceReceiver receiverSearch;
    private GPSServiceReceiver receiverGPS;
    private RoutineServiceReceiver routineServiceReceiver;
    public static final int MY_PERMISSIONS_REQUEST_GPS = 123;

    private boolean GPSEnable;
    private boolean networkEnable;
    private ArrayList<Place> places;
    AppDatabase db = Room.databaseBuilder(this,
            AppDatabase.class, "database-name").allowMainThreadQueries().fallbackToDestructiveMigration().build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setSupportActionBar(viewDataBinding.toolbar);

        checkActivations();

        registerService();

        viewSearchCode();
        gpsPreview();

        example();
        progressBarHolder = findViewById(R.id.progressBarHolder);
    }

    private void checkActivations() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (manager != null) {
            GPSEnable = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        networkEnable = connectivityManager != null && connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    private void example() {
        places = new ArrayList<>(db.placesDao().getAll());

        adapter = new PlaceAdapter(getApplicationContext(),
                R.layout.row, places);
        adapter.notifyDataSetChanged();
        adapter.setNotifyOnChange(true);

        listView = findViewById(R.id.list_view);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Place place = (Place) parent.getItemAtPosition(position);
                System.out.println(place);
                Double lat = place.lat;
                Double lng = place.lng;
                Uri gmmIntentUri = Uri.parse("google.navigation:q="+lat+","+lng);
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

        //filter for GPS
        IntentFilter filterRoutine = new IntentFilter(RoutineServiceReceiver.PROCESS_RESPONSE);
        filterRoutine.addCategory(Intent.CATEGORY_DEFAULT);
        routineServiceReceiver = new RoutineServiceReceiver();
        registerReceiver(routineServiceReceiver, filterRoutine);

    }

    private void startGPS() {
        if(!GPSEnable){
            activateGPS();
        }
        if(GPSEnable) {
            createNotification();
            startService(new Intent(getApplicationContext(), GPSService.class));
            //startService(new Intent(getApplicationContext(), RoutineService.class));
        }
    }

    private void createNotification() {

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.logo_app)
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
                //Toast.makeText(getApplicationContext(), query, Toast.LENGTH_LONG).show();
                new MyTask(query).execute();
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


        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            if (connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnectedOrConnecting()) {
                if (manager != null) {

                }
            }else{
                // Build the alert dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Internet non activé");
                builder.setMessage("Besoin d'internet");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Show location settings when the user acknowledges the alert dialog
                        Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                        startActivity(intent);
                    }
                });
                Dialog alertDialog = builder.create();
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.show();
            }
        }
    }
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        unregisterReceiver(this.receiverSearch);
        unregisterReceiver(this.receiverGPS);
        unregisterReceiver(this.routineServiceReceiver);
        stopService(new Intent(getApplicationContext(), GPSService.class));
        //stopService(new Intent(getApplicationContext(), RoutineService.class));
        NotificationManager notifManager= (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (notifManager != null) {
            notifManager.cancelAll();
        }

    }
    public void createNotificationPush() {
        // Prepare intent which is triggered if the
        // notification is selected
        /*Intent intent = new Intent(this, NotificationReceiverActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);
-+
**
        // Build notification
        // Actions are just fake
        Notification noti = new Notification.Builder(this)
                .setContentTitle("Du nouveau dans Memory Finder")
                .setContentText("nouveaux commerces dans la galerie").setSmallIcon(R.mipmap.logo_app)
                .setContentIntent(pIntent)
                .addAction(R.mipmap.logo_app, "Call", pIntent)
                .addAction(R.mipmap.logo_app, "More", pIntent)
                .addAction(R.mipmap.logo_app, "And more", pIntent).build();
        noti.vibrate = new long[] { 1000, 1000, 1000, 1000, 1000 };
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // hide the notification after its selected
        noti.flags |= Notification.FLAG_AUTO_CANCEL;

        if (notificationManager != null) {
            notificationManager.notify(0, noti);
        }
*/

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.logo_app)
                        .setContentTitle("Du nouveau dans Memory Finder")
                        .setContentText("Nouveaux commerces dans la galerie");

        Intent notificationIntent = new Intent(this, this.getClass());
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        builder.setVibrate(new long[] {0,200,100,200,100,200});
        if (manager != null) {
            manager.notify(0, builder.build());
        }
    }

    public class SearchServiceReceiver extends BroadcastReceiver {

        public static final String PROCESS_RESPONSE = "com.pepin_prestini.elim.myapplication.PROCESS_RESPONSE_SEARCH";

        @Override
        public void onReceive(Context context, Intent intent) {

            //String responseString = "[{\"geometry\":{\"location\":{\"lat\":43.66112340000001,\"lng\":7.1957764},\"viewport\":{\"northeast\":{\"lat\":43.66247322989273,\"lng\":7.197126229892723},\"southwest\":{\"lat\":43.65977357010729,\"lng\":7.194426570107279}}},\"icon\":\"https://maps.gstatic.com/mapfiles/place_api/icons/lodging-71.png\",\"id\":\"85fa9cbb18ce524ac23325340b8c717de3e1105f\",\"name\":\"Hotel Mercure Nice Cap 3000 Aeroport\",\"opening_hours\":{\"open_now\":false,\"weekday_text\":[]},\"photos\":[{\"height\":2322,\"html_attributions\":[\"<a href=\\\"https://maps.google.com/maps/contrib/106877304469267309325/photos\\\">A Google User</a>\"],\"photo_reference\":\"CmRaAAAAcF30u6vLvyhv4fpQBsm2tbc0282OFYqhBJ_9HyjyIssqAQ9gxi9jxt8WpCsuk90gf7BMu76ta4xphO_-n8so3fRLEF3SdntVBlmhfNb8dmAtOZ8J3u4vIDAyF7DDvAnPEhDECyXPXoAemWEyc0eGXOeLGhTSTpJvi1DyWHaViH3TXjgzS7hnAQ\",\"width\":4128}],\"place_id\":\"ChIJ5TZMFmrRzRIRm6zGwMizVg0\",\"rating\":3.6,\"reference\":\"CmRbAAAA7aWFSpIVTJlGbZSw5r4DjutPMpND9V4O_wS5XU9NC2d4kBD7MnlGEpw40ZlCzq4IVSxGcacBz398RQfIx0GT137GojreXKba2Mq9zM2S68zgbi-uobzd0g1ZR0gSUW5REhA14M1qC8H8f5B-Ec7HZlGSGhRpqW3tOEb5iX6H7WNEcLuS0obxnA\",\"scope\":\"GOOGLE\",\"types\":[\"lodging\",\"restaurant\",\"food\",\"point_of_interest\",\"establishment\"],\"vicinity\":\"190 Avenue Georges Guynemer, Saint-Laurent-du-Var\"},{\"geometry\":{\"location\":{\"lat\":43.6587928,\"lng\":7.194790599999999},\"viewport\":{\"northeast\":{\"lat\":43.66005702989272,\"lng\":7.196152879892722},\"southwest\":{\"lat\":43.65735737010728,\"lng\":7.193453220107278}}},\"icon\":\"https://maps.gstatic.com/mapfiles/place_api/icons/lodging-71.png\",\"id\":\"5882d0dd04da20cbb8a284f8175abf11255a53ab\",\"name\":\"Hôtel Novotel Nice Aéroport Cap 3000\",\"opening_hours\":{\"open_now\":true,\"weekday_text\":[]},\"photos\":[{\"height\":1552,\"html_attributions\":[\"<a href=\\\"https://maps.google.com/maps/contrib/112731672171736560429/photos\\\">Hôtel Novotel Nice Aéroport Cap 3000</a>\"],\"photo_reference\":\"CmRaAAAABL1WVS5IXL82qHcAZrHeaWXxEnfy1OERMoRLh5IdhrIFqy9s3hq1OSiutiorKUW3z7SsAkWPfeHbsN2UYDbdz6XFJ_f0EIy1IPZ0TvyQn4KPC7CEi6inSz_hmMAKz37PEhAUi1NxYEmsgUojbjcP6thoGhRlZxblsp_7J2c6UH1sxhuL8HsPPg\",\"width\":1557}],\"place_id\":\"ChIJ29ciIULRzRIRtOGEK5D8HKY\",\"rating\":3.8,\"reference\":\"CmRbAAAAGqgigi8Hf9i2YDCGVUYwEftiBt8w6LQUKJvSQt6lkMj94r2IiJn-sF8huLZgBYEbcMSDK0wHrrosB1KGpqN2_ln441MRcyhaplixho6D_uplpVcWnLFDinRM8gyl1Yh-EhDfLVCqeGv0skXFe4pydS21GhS1NpNUhjDz6FXecYA9wBX-KMmmqQ\",\"scope\":\"GOOGLE\",\"types\":[\"lodging\",\"restaurant\",\"food\",\"point_of_interest\",\"establishment\"],\"vicinity\":\"40 Avenue de Verdun, Saint-Laurent-du-Var\"},{\"geometry\":{\"location\":{\"lat\":43.6595854,\"lng\":7.193241200000001},\"viewport\":{\"northeast\":{\"lat\":43.66100912989272,\"lng\":7.194674079892724},\"southwest\":{\"lat\":43.65830947010727,\"lng\":7.19197442010728}}},\"icon\":\"https://maps.gstatic.com/mapfiles/place_api/icons/restaurant-71.png\",\"id\":\"7078ef251530dbfbbc8199342f30728274190191\",\"name\":\"Chez Mme Phok\",\"opening_hours\":{\"open_now\":false,\"weekday_text\":[]},\"photos\":[{\"height\":4128,\"html_attributions\":[\"<a href=\\\"https://maps.google.com/maps/contrib/104766217153829022075/photos\\\">André RIHANI</a>\"],\"photo_reference\":\"CmRaAAAAj5yURbxeyA0Nn-E-JlBUEOrufvfxmBk2t_hRjE2y0uS5EmISYTw5Tex3RFNpMVqXUXlJIEbl7ht4QSZPfSX71C6QKQ_zVEMqpnqwAXbl_6qiux3zv-f-T7W7xx6xNJE8EhDjjmw6wcpSt2PumBjjfUDSGhTV096H4W5tx_ccBCHll5rj44jPvg\",\"width\":3096}],\"place_id\":\"ChIJ45_pJenTzRIRFk8q2hl0cTc\",\"rating\":4.3,\"reference\":\"CmRbAAAATIlS4IQoEMIpsbA3U6qedYOmaRcQb6aZYu9m_ukvY2a4KCIxeMdcm_hCFt8SEf_9wDAxMCTctFJUc3kPSNf86nUC7tmNlV2qbDHCKA6LvU1jTfWSYQs3NInSo7FBgwUlEhCRrAzWg5JqfbvnCc4b71OBGhSN-DKBXaY8gXVH5iYdhfa4zav2qg\",\"scope\":\"GOOGLE\",\"types\":[\"restaurant\",\"food\",\"point_of_interest\",\"establishment\"],\"vicinity\":\"1 Avenue Léon Bérenger, Saint-Laurent-du-Var\"},{\"geometry\":{\"location\":{\"lat\":43.6573917,\"lng\":7.1969381},\"viewport\":{\"northeast\":{\"lat\":43.65902092989272,\"lng\":7.197673649999995},\"southwest\":{\"lat\":43.65632127010728,\"lng\":7.194731450000001}}},\"icon\":\"https://maps.gstatic.com/mapfiles/place_api/icons/restaurant-71.png\",\"id\":\"60e70f1fc92313dca3c952788c5f4262173ffdfc\",\"name\":\"Panasia\",\"opening_hours\":{\"open_now\":false,\"weekday_text\":[]},\"photos\":[{\"height\":1688,\"html_attributions\":[\"<a href=\\\"https://maps.google.com/maps/contrib/100917195381685169864/photos\\\">HD Media 06</a>\"],\"photo_reference\":\"CmRaAAAAmN6NUVydikudhpHaR4J2-lcr69yin6dUuX3p2Exd_VSRXy2oOroHVKNfIv-H52AgO9Bg_ZwdYRWASwZmtr3wGQi7oK0l9b3gTJY-lbw9VHTlwr9z5X7NrN74T5RCg4UvEhDoySLaW1e4j7c7t4UnOfPpGhTIsYMbZj2vvpHl__R3_wi-0UI0zA\",\"width\":3000}],\"place_id\":\"ChIJt2Yb4kDRzRIRV7GY0HvBNdQ\",\"rating\":3.6,\"reference\":\"CmRbAAAAY0LbcmaQmvYKFn7QMFOTtzN7yKoBEeGB4KsAc2TqWIz244CBEF4cXj1cAYRNkIMQIoA6pYEhtNwJKE3FIUtSbnYQrkAXJPgmxLtZNQPe2CBoLtvhR9hP8SqkrBLzcblVEhAxgc3XlBTlnQiTedfDM_PJGhQ7ycHWzmE1J0SGaEj4W3JEjQBbqw\",\"scope\":\"GOOGLE\",\"types\":[\"restaurant\",\"food\",\"point_of_interest\",\"establishment\"],\"vicinity\":\"Centre commercial Cap 30000, Avenue Eugène Donadeï, Saint-Laurent-du-Var\"},{\"geometry\":{\"location\":{\"lat\":43.6623708,\"lng\":7.193944800000001},\"viewport\":{\"northeast\":{\"lat\":43.66370702989273,\"lng\":7.195302929892724},\"southwest\":{\"lat\":43.66100737010728,\"lng\":7.19260327010728}}},\"icon\":\"https://maps.gstatic.com/mapfiles/place_api/icons/restaurant-71.png\",\"id\":\"4b77d665572b11fd7a6621b07f694bd91285985c\",\"name\":\"Bar Restaurant Mediterranee\",\"place_id\":\"ChIJe00Wn2nRzRIR_IyYptmjHKA\",\"rating\":3,\"reference\":\"CmRbAAAAzga5kt2O5dB5kkiR0p3Mwj9E2U5fhx1nIX_TzRHpQECJIujRVwAzFw6LEvxfWGcdanBFOUMGUf62FSOPCKr5kUbzCdilW88hvFazYc0aigdD15gkXp8PugnBKPufRR7WEhBqYgGfjvrVJKfLJZaw87khGhQ7R8lllpkHt6sFAPRidGchp5gFDw\",\"scope\":\"GOOGLE\",\"types\":[\"bar\",\"restaurant\",\"food\",\"point_of_interest\",\"establishment\"],\"vicinity\":\"24 Boulevard Jean Ossola, Saint-Laurent-du-Var\"},{\"geometry\":{\"location\":{\"lat\":43.6590137,\"lng\":7.1977307},\"viewport\":{\"northeast\":{\"lat\":43.66036352989272,\"lng\":7.19908052989272},\"southwest\":{\"lat\":43.65766387010727,\"lng\":7.196380870107276}}},\"icon\":\"https://maps.gstatic.com/mapfiles/place_api/icons/restaurant-71.png\",\"id\":\"cbc39a20ff9945c51bc3509662926556dd3e0009\",\"name\":\"O'Sushi\",\"opening_hours\":{\"open_now\":true,\"weekday_text\":[]},\"place_id\":\"ChIJiw1K50HRzRIRwCbg9z6WukE\",\"rating\":2,\"reference\":\"CmRbAAAA4MGjS5YMYvn91shlIN5sujvDJpPTgqwmacwnUMwpzyJe0qnoHYfU3UO4juVNH1Mj_kzUF_ifaWAAQWuUPsEMRfVQ8iYQb4vNLxVzBqpGDowJ6juHIWOqp5WVvmM9xnHjEhCTOlpHRqaKqwLlcr7co6GXGhQjFL1CWwn3KEZnjLORBEzZaBj-zA\",\"scope\":\"GOOGLE\",\"types\":[\"meal_takeaway\",\"restaurant\",\"food\",\"point_of_interest\",\"establishment\"],\"vicinity\":\"Centre Commercial Cap 3000, Avenue Eugène Donadeï, Saint-Laurent-du-Var\"},{\"geometry\":{\"location\":{\"lat\":43.65979799999999,\"lng\":7.197964999999999},\"viewport\":{\"northeast\":{\"lat\":43.66114782989273,\"lng\":7.199314829892721},\"southwest\":{\"lat\":43.65844817010728,\"lng\":7.196615170107277}}},\"icon\":\"https://maps.gstatic.com/mapfiles/place_api/icons/restaurant-71.png\",\"id\":\"8ef87cf030355944acdeb3f6ba9493266bc234c0\",\"name\":\"Jardin Du Cap\",\"opening_hours\":{\"open_now\":false,\"weekday_text\":[]},\"photos\":[{\"height\":1152,\"html_attributions\":[\"<a href=\\\"https://maps.google.com/maps/contrib/106610580586493453393/photos\\\">Jardin Du Cap</a>\"],\"photo_reference\":\"CmRaAAAA3Z-f0A4oEymb4R_2qT5FEvvB4w_ADP7OQSwcuDtus4B2ldsDYy5zAymROegrum9fiAMPtJZXZP1lyi6U-WH65VbmfTQ2HQOamDozPXg-ChrnDOTi2Di0mZ4cyLB1o2GSEhBhmAT_U-MrwgdE3oJfxYT_GhRfmYgvqign8Jvg5yvF0ww64ZgO5g\",\"width\":2048}],\"place_id\":\"ChIJP09--EHRzRIRTjtGjMKtk8Q\",\"rating\":4.2,\"reference\":\"CmRbAAAAUgwz0Dr5nBka-yzryj90KIxPGvCR9mwAzP1bSj0Tw2UyFLBpZU23T62GGBsIQz5b_vl9S5QRxDXRR9sEgDxyW4m5gsh3_JQFPsI5Z54m0TGTcIiBeaY-wGCll_AKFAjqEhDi1STc8XCSyU3aBlPsYzEpGhR_BI0ISpeS72lp7-OGWtNcw7PsMw\",\"scope\":\"GOOGLE\",\"types\":[\"bar\",\"store\",\"restaurant\",\"food\",\"point_of_interest\",\"establishment\"],\"vicinity\":\"Avenue Eugène Donadeï, Saint-Laurent-du-Var\"},{\"geometry\":{\"location\":{\"lat\":43.6593149,\"lng\":7.197624699999999},\"viewport\":{\"northeast\":{\"lat\":43.66066472989272,\"lng\":7.198974529892721},\"southwest\":{\"lat\":43.65796507010727,\"lng\":7.196274870107277}}},\"icon\":\"https://maps.gstatic.com/mapfiles/place_api/icons/restaurant-71.png\",\"id\":\"6df050db8b4463f2b2303521fa477a2656504205\",\"name\":\"Evasion\",\"opening_hours\":{\"open_now\":false,\"weekday_text\":[]},\"photos\":[{\"height\":3840,\"html_attributions\":[\"<a href=\\\"https://maps.google.com/maps/contrib/103747353171486656461/photos\\\">Pierre Cormeau</a>\"],\"photo_reference\":\"CmRaAAAAIehdYjgdofvZJRzDy3znoM63OreFt9xMYIZAAXhbgdRoYwum32qGcUYl9Rxo50iqw2iR62K24Znf0x1p01Rgs8P7eLWULlaYbT7NPA1_Bdm713iB8eAPmQ2N-Wf8LQPFEhDUH7oDBmyQK7DN5Kh5scFMGhQBHGRRvXgzzV0PWxlxkUVkPaSenw\",\"width\":5760}],\"place_id\":\"ChIJP09--EHRzRIR50ysVNnGxTQ\",\"rating\":2.7,\"reference\":\"CmRbAAAA4JpqDqKO_DxVQI-WTxS1FnHKxBvxLoOpfQx11wNlpODxSKLQuQKcck3RCuvXWOz_yUOiAbS3XgRLNyiRkxFxE_pzwYKEersbtP8w0dJR4vtuV4HriCBr22zG6Cbat6COEhB7UFWVMeHDiUm1RbKZba8FGhRHFvu0xhX-3FmnbeiJiOIzN_CFXw\",\"scope\":\"GOOGLE\",\"types\":[\"restaurant\",\"bar\",\"food\",\"point_of_interest\",\"establishment\"],\"vicinity\":\"Avenue Eugène Donadeï, Saint-Laurent-du-Var\"},{\"geometry\":{\"location\":{\"lat\":43.662289,\"lng\":7.19336},\"viewport\":{\"northeast\":{\"lat\":43.66357567989272,\"lng\":7.194681679892721},\"southwest\":{\"lat\":43.66087602010728,\"lng\":7.191982020107277}}},\"icon\":\"https://maps.gstatic.com/mapfiles/place_api/icons/restaurant-71.png\",\"id\":\"48c30068943b9fd5d7c65f135a70aa1e451ff646\",\"name\":\"Saint Laurent Pizza\",\"opening_hours\":{\"open_now\":false,\"weekday_text\":[]},\"photos\":[{\"height\":500,\"html_attributions\":[\"<a href=\\\"https://maps.google.com/maps/contrib/112015733112808401550/photos\\\">Saint Laurent Pizza</a>\"],\"photo_reference\":\"CmRaAAAAGAX9nN5ijjBl_mhbvFwvURjn95nkJ5xzjkoBu86bCzose2lx4yNMYGznv8bxDS5wSCNJbTIutg4FcMlVhOv_lc7vL37eOaT4DzdMweeG9jJCLOLJDwcBpVUPd1ww-KqHEhAXAmKIBQApgYQpqrvsbfEhGhRD8TCGzBd3KlYf11t8oI4UGRZSfw\",\"width\":500}],\"place_id\":\"ChIJAQA0pGnRzRIR4EyHHnh9ScU\",\"rating\":4.5,\"reference\":\"CmRbAAAAtvAZ7_T3QT_9Osq7j1W1S8kmLhYKRH7kopWCChRp32A_PQDGhHGvdR1IRXpzxuW1-U6IEKclHPhSd_LF8-pPjndvyskbNaxCt2S04haGePP3TYbJGhAKYmKpu2Ghe5hnEhClAmX4PbwefR3C3MjZTyJQGhTPOW7LgBUEs_r4URIUxq7rp0BxxA\",\"scope\":\"GOOGLE\",\"types\":[\"restaurant\",\"food\",\"point_of_interest\",\"establishment\"],\"vicinity\":\"66 Boulevard Jean Ossola, Saint-Laurent-du-Var\"},{\"geometry\":{\"location\":{\"lat\":43.659645,\"lng\":7.194166},\"viewport\":{\"northeast\":{\"lat\":43.66091487989272,\"lng\":7.195479679892721},\"southwest\":{\"lat\":43.65821522010727,\"lng\":7.192780020107278}}},\"icon\":\"https://maps.gstatic.com/mapfiles/place_api/icons/restaurant-71.png\",\"id\":\"b7d6dd29d93e4a6e0dbd0627e22b150e771418f7\",\"name\":\"McDonald's\",\"opening_hours\":{\"open_now\":true,\"weekday_text\":[]},\"photos\":[{\"height\":288,\"html_attributions\":[\"<a href=\\\"https://maps.google.com/maps/contrib/101245177780964053364/photos\\\">McDonald&#39;s</a>\"],\"photo_reference\":\"CmRaAAAAaEiqcS-CGev5J1kj8ae_GQReqCfunLNsPRTjAvwfmKbULH0ps-PsXw3tEtB2jC7NielDseD4S88IS7OnHCj-cy2phDKy4Ug1vNN8q6KT92-6JUE13Wgc1lrcq2lvVfwhEhAov-b3ngSiFpFrUwILOGuWGhTgXya7yi9N8vU0SBSRby7rx7R4Zw\",\"width\":512}],\"place_id\":\"ChIJGxiYEULRzRIRw7NO0c_twKA\",\"price_level\":1,\"rating\":3.1,\"reference\":\"CmRbAAAAP2wV_UcqW-dUfAF0nZ--iDUi8ZIKPehAUAAUTnnKAhRzO9hMalbv_fSVAYTLYaJUvzh-U-UgO81SkCPbhvCPDaeEPqtZX5tvY4C_6pu0-x0acSKB7dYt8e1CMNRs5BPfEhBLLsfcmPhjYrmDPBEzqN9vGhSeKZyo92MHei_fo24z89QYSQ2zUQ\",\"scope\":\"GOOGLE\",\"types\":[\"restaurant\",\"food\",\"point_of_interest\",\"establishment\"],\"vicinity\":\"58 Avenue Léon Bérenger, Saint-Laurent-du-Var\"},{\"geometry\":{\"location\":{\"lat\":43.658026,\"lng\":7.189718300000001},\"viewport\":{\"northeast\":{\"lat\":43.65927682989272,\"lng\":7.191075679892721},\"southwest\":{\"lat\":43.65657717010728,\"lng\":7.188376020107277}}},\"icon\":\"https://maps.gstatic.com/mapfiles/place_api/icons/restaurant-71.png\",\"id\":\"43d972ed24c91703f2a1d374f18bfc4bee80036d\",\"name\":\"La Cabane\",\"opening_hours\":{\"open_now\":true,\"weekday_text\":[]},\"photos\":[{\"height\":3456,\"html_attributions\":[\"<a href=\\\"https://maps.google.com/maps/contrib/104643104582113529066/photos\\\">Sylvain Tournaire</a>\"],\"photo_reference\":\"CmRaAAAA5LaoNTkSIlQj7YHSAVCgOXky_1RtzPhWq7wJpf5V2tTdYxgIK3YsVz4l8uVBippD6uWUxPukpZ5LtwV9-NYMHJXa3Re8NadrOOCSeVKYqvIL3zAg_LGaE8vsb1oJ0TqtEhBvqVk8U7ZeRu3tO9zQbAJPGhQtCxPd7Vm7Lx-5MDC_L35reKvtkQ\",\"width\":4608}],\"place_id\":\"ChIJh42D5lzRzRIRGb9eQqFKbkA\",\"rating\":4.2,\"reference\":\"CmRbAAAAHCRBReb-YJ-FZ5zmiuFuMVlIwJozmf4OyWxvbAMoSXZEMl4KVJD6Zo2JxVUlpagKtbwSF_Eore3aSO1fhuliM1VbHnC-4m5UDUAmbuk4JVf42tkojeW_zNaMpgQqKn3SEhBg0yF_k3YLgk31cxAciwXzGhTwILuWm1UWrf3tGfkIKVrRbo_jdA\",\"scope\":\"GOOGLE\",\"types\":[\"restaurant\",\"bar\",\"food\",\"point_of_interest\",\"establishment\"],\"vicinity\":\"167 Prom. des Flots Bleus, Saint-Laurent-du-Var\"}]";
            String responseString = intent.getStringExtra(SearchService.RESPONSE_STRING);
            try {
                JSONArray array = new JSONArray(responseString);
                for(int i = 0; i < array.length(); i++){
                    JSONObject obj = array.getJSONObject(i);
                    String name = obj.getString("name");
                    String iconPath = obj.getString("icon");
                    Double lat = obj.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                    Double lon = obj.getJSONObject("geometry").getJSONObject("location").getDouble("lng");
                    String adresse = obj.getString("vicinity");
                    Place p = new Place(name,lat,lon,iconPath,adresse);
                    adapter.notifyDataSetChanged();

                    listView.invalidateViews();
                    db.placesDao().insertAll(p);
                    places.clear();
                    places.addAll(db.placesDao().getAll());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            stopService(new Intent(getApplicationContext(), SearchService.class));
        }
    }

    public class GPSServiceReceiver extends BroadcastReceiver {

        public static final String PROCESS_RESPONSE = "com.pepin_prestini.elim.myapplication.PROCESS_RESPONSE_GPS";

        @Override
        public void onReceive(Context context, Intent intent) {
            //String responseString = "[{\"geometry\":{\"location\":{\"lat\":43.66112340000001,\"lng\":7.1957764},\"viewport\":{\"northeast\":{\"lat\":43.66247322989273,\"lng\":7.197126229892723},\"southwest\":{\"lat\":43.65977357010729,\"lng\":7.194426570107279}}},\"icon\":\"https://maps.gstatic.com/mapfiles/place_api/icons/lodging-71.png\",\"id\":\"85fa9cbb18ce524ac23325340b8c717de3e1105f\",\"name\":\"Hotel Mercure Nice Cap 3000 Aeroport\",\"opening_hours\":{\"open_now\":false,\"weekday_text\":[]},\"photos\":[{\"height\":2322,\"html_attributions\":[\"<a href=\\\"https://maps.google.com/maps/contrib/106877304469267309325/photos\\\">A Google User</a>\"],\"photo_reference\":\"CmRaAAAAcF30u6vLvyhv4fpQBsm2tbc0282OFYqhBJ_9HyjyIssqAQ9gxi9jxt8WpCsuk90gf7BMu76ta4xphO_-n8so3fRLEF3SdntVBlmhfNb8dmAtOZ8J3u4vIDAyF7DDvAnPEhDECyXPXoAemWEyc0eGXOeLGhTSTpJvi1DyWHaViH3TXjgzS7hnAQ\",\"width\":4128}],\"place_id\":\"ChIJ5TZMFmrRzRIRm6zGwMizVg0\",\"rating\":3.6,\"reference\":\"CmRbAAAA7aWFSpIVTJlGbZSw5r4DjutPMpND9V4O_wS5XU9NC2d4kBD7MnlGEpw40ZlCzq4IVSxGcacBz398RQfIx0GT137GojreXKba2Mq9zM2S68zgbi-uobzd0g1ZR0gSUW5REhA14M1qC8H8f5B-Ec7HZlGSGhRpqW3tOEb5iX6H7WNEcLuS0obxnA\",\"scope\":\"GOOGLE\",\"types\":[\"lodging\",\"restaurant\",\"food\",\"point_of_interest\",\"establishment\"],\"vicinity\":\"190 Avenue Georges Guynemer, Saint-Laurent-du-Var\"},{\"geometry\":{\"location\":{\"lat\":43.6587928,\"lng\":7.194790599999999},\"viewport\":{\"northeast\":{\"lat\":43.66005702989272,\"lng\":7.196152879892722},\"southwest\":{\"lat\":43.65735737010728,\"lng\":7.193453220107278}}},\"icon\":\"https://maps.gstatic.com/mapfiles/place_api/icons/lodging-71.png\",\"id\":\"5882d0dd04da20cbb8a284f8175abf11255a53ab\",\"name\":\"Hôtel Novotel Nice Aéroport Cap 3000\",\"opening_hours\":{\"open_now\":true,\"weekday_text\":[]},\"photos\":[{\"height\":1552,\"html_attributions\":[\"<a href=\\\"https://maps.google.com/maps/contrib/112731672171736560429/photos\\\">Hôtel Novotel Nice Aéroport Cap 3000</a>\"],\"photo_reference\":\"CmRaAAAABL1WVS5IXL82qHcAZrHeaWXxEnfy1OERMoRLh5IdhrIFqy9s3hq1OSiutiorKUW3z7SsAkWPfeHbsN2UYDbdz6XFJ_f0EIy1IPZ0TvyQn4KPC7CEi6inSz_hmMAKz37PEhAUi1NxYEmsgUojbjcP6thoGhRlZxblsp_7J2c6UH1sxhuL8HsPPg\",\"width\":1557}],\"place_id\":\"ChIJ29ciIULRzRIRtOGEK5D8HKY\",\"rating\":3.8,\"reference\":\"CmRbAAAAGqgigi8Hf9i2YDCGVUYwEftiBt8w6LQUKJvSQt6lkMj94r2IiJn-sF8huLZgBYEbcMSDK0wHrrosB1KGpqN2_ln441MRcyhaplixho6D_uplpVcWnLFDinRM8gyl1Yh-EhDfLVCqeGv0skXFe4pydS21GhS1NpNUhjDz6FXecYA9wBX-KMmmqQ\",\"scope\":\"GOOGLE\",\"types\":[\"lodging\",\"restaurant\",\"food\",\"point_of_interest\",\"establishment\"],\"vicinity\":\"40 Avenue de Verdun, Saint-Laurent-du-Var\"},{\"geometry\":{\"location\":{\"lat\":43.6595854,\"lng\":7.193241200000001},\"viewport\":{\"northeast\":{\"lat\":43.66100912989272,\"lng\":7.194674079892724},\"southwest\":{\"lat\":43.65830947010727,\"lng\":7.19197442010728}}},\"icon\":\"https://maps.gstatic.com/mapfiles/place_api/icons/restaurant-71.png\",\"id\":\"7078ef251530dbfbbc8199342f30728274190191\",\"name\":\"Chez Mme Phok\",\"opening_hours\":{\"open_now\":false,\"weekday_text\":[]},\"photos\":[{\"height\":4128,\"html_attributions\":[\"<a href=\\\"https://maps.google.com/maps/contrib/104766217153829022075/photos\\\">André RIHANI</a>\"],\"photo_reference\":\"CmRaAAAAj5yURbxeyA0Nn-E-JlBUEOrufvfxmBk2t_hRjE2y0uS5EmISYTw5Tex3RFNpMVqXUXlJIEbl7ht4QSZPfSX71C6QKQ_zVEMqpnqwAXbl_6qiux3zv-f-T7W7xx6xNJE8EhDjjmw6wcpSt2PumBjjfUDSGhTV096H4W5tx_ccBCHll5rj44jPvg\",\"width\":3096}],\"place_id\":\"ChIJ45_pJenTzRIRFk8q2hl0cTc\",\"rating\":4.3,\"reference\":\"CmRbAAAATIlS4IQoEMIpsbA3U6qedYOmaRcQb6aZYu9m_ukvY2a4KCIxeMdcm_hCFt8SEf_9wDAxMCTctFJUc3kPSNf86nUC7tmNlV2qbDHCKA6LvU1jTfWSYQs3NInSo7FBgwUlEhCRrAzWg5JqfbvnCc4b71OBGhSN-DKBXaY8gXVH5iYdhfa4zav2qg\",\"scope\":\"GOOGLE\",\"types\":[\"restaurant\",\"food\",\"point_of_interest\",\"establishment\"],\"vicinity\":\"1 Avenue Léon Bérenger, Saint-Laurent-du-Var\"},{\"geometry\":{\"location\":{\"lat\":43.6573917,\"lng\":7.1969381},\"viewport\":{\"northeast\":{\"lat\":43.65902092989272,\"lng\":7.197673649999995},\"southwest\":{\"lat\":43.65632127010728,\"lng\":7.194731450000001}}},\"icon\":\"https://maps.gstatic.com/mapfiles/place_api/icons/restaurant-71.png\",\"id\":\"60e70f1fc92313dca3c952788c5f4262173ffdfc\",\"name\":\"Panasia\",\"opening_hours\":{\"open_now\":false,\"weekday_text\":[]},\"photos\":[{\"height\":1688,\"html_attributions\":[\"<a href=\\\"https://maps.google.com/maps/contrib/100917195381685169864/photos\\\">HD Media 06</a>\"],\"photo_reference\":\"CmRaAAAAmN6NUVydikudhpHaR4J2-lcr69yin6dUuX3p2Exd_VSRXy2oOroHVKNfIv-H52AgO9Bg_ZwdYRWASwZmtr3wGQi7oK0l9b3gTJY-lbw9VHTlwr9z5X7NrN74T5RCg4UvEhDoySLaW1e4j7c7t4UnOfPpGhTIsYMbZj2vvpHl__R3_wi-0UI0zA\",\"width\":3000}],\"place_id\":\"ChIJt2Yb4kDRzRIRV7GY0HvBNdQ\",\"rating\":3.6,\"reference\":\"CmRbAAAAY0LbcmaQmvYKFn7QMFOTtzN7yKoBEeGB4KsAc2TqWIz244CBEF4cXj1cAYRNkIMQIoA6pYEhtNwJKE3FIUtSbnYQrkAXJPgmxLtZNQPe2CBoLtvhR9hP8SqkrBLzcblVEhAxgc3XlBTlnQiTedfDM_PJGhQ7ycHWzmE1J0SGaEj4W3JEjQBbqw\",\"scope\":\"GOOGLE\",\"types\":[\"restaurant\",\"food\",\"point_of_interest\",\"establishment\"],\"vicinity\":\"Centre commercial Cap 30000, Avenue Eugène Donadeï, Saint-Laurent-du-Var\"},{\"geometry\":{\"location\":{\"lat\":43.6623708,\"lng\":7.193944800000001},\"viewport\":{\"northeast\":{\"lat\":43.66370702989273,\"lng\":7.195302929892724},\"southwest\":{\"lat\":43.66100737010728,\"lng\":7.19260327010728}}},\"icon\":\"https://maps.gstatic.com/mapfiles/place_api/icons/restaurant-71.png\",\"id\":\"4b77d665572b11fd7a6621b07f694bd91285985c\",\"name\":\"Bar Restaurant Mediterranee\",\"place_id\":\"ChIJe00Wn2nRzRIR_IyYptmjHKA\",\"rating\":3,\"reference\":\"CmRbAAAAzga5kt2O5dB5kkiR0p3Mwj9E2U5fhx1nIX_TzRHpQECJIujRVwAzFw6LEvxfWGcdanBFOUMGUf62FSOPCKr5kUbzCdilW88hvFazYc0aigdD15gkXp8PugnBKPufRR7WEhBqYgGfjvrVJKfLJZaw87khGhQ7R8lllpkHt6sFAPRidGchp5gFDw\",\"scope\":\"GOOGLE\",\"types\":[\"bar\",\"restaurant\",\"food\",\"point_of_interest\",\"establishment\"],\"vicinity\":\"24 Boulevard Jean Ossola, Saint-Laurent-du-Var\"},{\"geometry\":{\"location\":{\"lat\":43.6590137,\"lng\":7.1977307},\"viewport\":{\"northeast\":{\"lat\":43.66036352989272,\"lng\":7.19908052989272},\"southwest\":{\"lat\":43.65766387010727,\"lng\":7.196380870107276}}},\"icon\":\"https://maps.gstatic.com/mapfiles/place_api/icons/restaurant-71.png\",\"id\":\"cbc39a20ff9945c51bc3509662926556dd3e0009\",\"name\":\"O'Sushi\",\"opening_hours\":{\"open_now\":true,\"weekday_text\":[]},\"place_id\":\"ChIJiw1K50HRzRIRwCbg9z6WukE\",\"rating\":2,\"reference\":\"CmRbAAAA4MGjS5YMYvn91shlIN5sujvDJpPTgqwmacwnUMwpzyJe0qnoHYfU3UO4juVNH1Mj_kzUF_ifaWAAQWuUPsEMRfVQ8iYQb4vNLxVzBqpGDowJ6juHIWOqp5WVvmM9xnHjEhCTOlpHRqaKqwLlcr7co6GXGhQjFL1CWwn3KEZnjLORBEzZaBj-zA\",\"scope\":\"GOOGLE\",\"types\":[\"meal_takeaway\",\"restaurant\",\"food\",\"point_of_interest\",\"establishment\"],\"vicinity\":\"Centre Commercial Cap 3000, Avenue Eugène Donadeï, Saint-Laurent-du-Var\"},{\"geometry\":{\"location\":{\"lat\":43.65979799999999,\"lng\":7.197964999999999},\"viewport\":{\"northeast\":{\"lat\":43.66114782989273,\"lng\":7.199314829892721},\"southwest\":{\"lat\":43.65844817010728,\"lng\":7.196615170107277}}},\"icon\":\"https://maps.gstatic.com/mapfiles/place_api/icons/restaurant-71.png\",\"id\":\"8ef87cf030355944acdeb3f6ba9493266bc234c0\",\"name\":\"Jardin Du Cap\",\"opening_hours\":{\"open_now\":false,\"weekday_text\":[]},\"photos\":[{\"height\":1152,\"html_attributions\":[\"<a href=\\\"https://maps.google.com/maps/contrib/106610580586493453393/photos\\\">Jardin Du Cap</a>\"],\"photo_reference\":\"CmRaAAAA3Z-f0A4oEymb4R_2qT5FEvvB4w_ADP7OQSwcuDtus4B2ldsDYy5zAymROegrum9fiAMPtJZXZP1lyi6U-WH65VbmfTQ2HQOamDozPXg-ChrnDOTi2Di0mZ4cyLB1o2GSEhBhmAT_U-MrwgdE3oJfxYT_GhRfmYgvqign8Jvg5yvF0ww64ZgO5g\",\"width\":2048}],\"place_id\":\"ChIJP09--EHRzRIRTjtGjMKtk8Q\",\"rating\":4.2,\"reference\":\"CmRbAAAAUgwz0Dr5nBka-yzryj90KIxPGvCR9mwAzP1bSj0Tw2UyFLBpZU23T62GGBsIQz5b_vl9S5QRxDXRR9sEgDxyW4m5gsh3_JQFPsI5Z54m0TGTcIiBeaY-wGCll_AKFAjqEhDi1STc8XCSyU3aBlPsYzEpGhR_BI0ISpeS72lp7-OGWtNcw7PsMw\",\"scope\":\"GOOGLE\",\"types\":[\"bar\",\"store\",\"restaurant\",\"food\",\"point_of_interest\",\"establishment\"],\"vicinity\":\"Avenue Eugène Donadeï, Saint-Laurent-du-Var\"},{\"geometry\":{\"location\":{\"lat\":43.6593149,\"lng\":7.197624699999999},\"viewport\":{\"northeast\":{\"lat\":43.66066472989272,\"lng\":7.198974529892721},\"southwest\":{\"lat\":43.65796507010727,\"lng\":7.196274870107277}}},\"icon\":\"https://maps.gstatic.com/mapfiles/place_api/icons/restaurant-71.png\",\"id\":\"6df050db8b4463f2b2303521fa477a2656504205\",\"name\":\"Evasion\",\"opening_hours\":{\"open_now\":false,\"weekday_text\":[]},\"photos\":[{\"height\":3840,\"html_attributions\":[\"<a href=\\\"https://maps.google.com/maps/contrib/103747353171486656461/photos\\\">Pierre Cormeau</a>\"],\"photo_reference\":\"CmRaAAAAIehdYjgdofvZJRzDy3znoM63OreFt9xMYIZAAXhbgdRoYwum32qGcUYl9Rxo50iqw2iR62K24Znf0x1p01Rgs8P7eLWULlaYbT7NPA1_Bdm713iB8eAPmQ2N-Wf8LQPFEhDUH7oDBmyQK7DN5Kh5scFMGhQBHGRRvXgzzV0PWxlxkUVkPaSenw\",\"width\":5760}],\"place_id\":\"ChIJP09--EHRzRIR50ysVNnGxTQ\",\"rating\":2.7,\"reference\":\"CmRbAAAA4JpqDqKO_DxVQI-WTxS1FnHKxBvxLoOpfQx11wNlpODxSKLQuQKcck3RCuvXWOz_yUOiAbS3XgRLNyiRkxFxE_pzwYKEersbtP8w0dJR4vtuV4HriCBr22zG6Cbat6COEhB7UFWVMeHDiUm1RbKZba8FGhRHFvu0xhX-3FmnbeiJiOIzN_CFXw\",\"scope\":\"GOOGLE\",\"types\":[\"restaurant\",\"bar\",\"food\",\"point_of_interest\",\"establishment\"],\"vicinity\":\"Avenue Eugène Donadeï, Saint-Laurent-du-Var\"},{\"geometry\":{\"location\":{\"lat\":43.662289,\"lng\":7.19336},\"viewport\":{\"northeast\":{\"lat\":43.66357567989272,\"lng\":7.194681679892721},\"southwest\":{\"lat\":43.66087602010728,\"lng\":7.191982020107277}}},\"icon\":\"https://maps.gstatic.com/mapfiles/place_api/icons/restaurant-71.png\",\"id\":\"48c30068943b9fd5d7c65f135a70aa1e451ff646\",\"name\":\"Saint Laurent Pizza\",\"opening_hours\":{\"open_now\":false,\"weekday_text\":[]},\"photos\":[{\"height\":500,\"html_attributions\":[\"<a href=\\\"https://maps.google.com/maps/contrib/112015733112808401550/photos\\\">Saint Laurent Pizza</a>\"],\"photo_reference\":\"CmRaAAAAGAX9nN5ijjBl_mhbvFwvURjn95nkJ5xzjkoBu86bCzose2lx4yNMYGznv8bxDS5wSCNJbTIutg4FcMlVhOv_lc7vL37eOaT4DzdMweeG9jJCLOLJDwcBpVUPd1ww-KqHEhAXAmKIBQApgYQpqrvsbfEhGhRD8TCGzBd3KlYf11t8oI4UGRZSfw\",\"width\":500}],\"place_id\":\"ChIJAQA0pGnRzRIR4EyHHnh9ScU\",\"rating\":4.5,\"reference\":\"CmRbAAAAtvAZ7_T3QT_9Osq7j1W1S8kmLhYKRH7kopWCChRp32A_PQDGhHGvdR1IRXpzxuW1-U6IEKclHPhSd_LF8-pPjndvyskbNaxCt2S04haGePP3TYbJGhAKYmKpu2Ghe5hnEhClAmX4PbwefR3C3MjZTyJQGhTPOW7LgBUEs_r4URIUxq7rp0BxxA\",\"scope\":\"GOOGLE\",\"types\":[\"restaurant\",\"food\",\"point_of_interest\",\"establishment\"],\"vicinity\":\"66 Boulevard Jean Ossola, Saint-Laurent-du-Var\"},{\"geometry\":{\"location\":{\"lat\":43.659645,\"lng\":7.194166},\"viewport\":{\"northeast\":{\"lat\":43.66091487989272,\"lng\":7.195479679892721},\"southwest\":{\"lat\":43.65821522010727,\"lng\":7.192780020107278}}},\"icon\":\"https://maps.gstatic.com/mapfiles/place_api/icons/restaurant-71.png\",\"id\":\"b7d6dd29d93e4a6e0dbd0627e22b150e771418f7\",\"name\":\"McDonald's\",\"opening_hours\":{\"open_now\":true,\"weekday_text\":[]},\"photos\":[{\"height\":288,\"html_attributions\":[\"<a href=\\\"https://maps.google.com/maps/contrib/101245177780964053364/photos\\\">McDonald&#39;s</a>\"],\"photo_reference\":\"CmRaAAAAaEiqcS-CGev5J1kj8ae_GQReqCfunLNsPRTjAvwfmKbULH0ps-PsXw3tEtB2jC7NielDseD4S88IS7OnHCj-cy2phDKy4Ug1vNN8q6KT92-6JUE13Wgc1lrcq2lvVfwhEhAov-b3ngSiFpFrUwILOGuWGhTgXya7yi9N8vU0SBSRby7rx7R4Zw\",\"width\":512}],\"place_id\":\"ChIJGxiYEULRzRIRw7NO0c_twKA\",\"price_level\":1,\"rating\":3.1,\"reference\":\"CmRbAAAAP2wV_UcqW-dUfAF0nZ--iDUi8ZIKPehAUAAUTnnKAhRzO9hMalbv_fSVAYTLYaJUvzh-U-UgO81SkCPbhvCPDaeEPqtZX5tvY4C_6pu0-x0acSKB7dYt8e1CMNRs5BPfEhBLLsfcmPhjYrmDPBEzqN9vGhSeKZyo92MHei_fo24z89QYSQ2zUQ\",\"scope\":\"GOOGLE\",\"types\":[\"restaurant\",\"food\",\"point_of_interest\",\"establishment\"],\"vicinity\":\"58 Avenue Léon Bérenger, Saint-Laurent-du-Var\"},{\"geometry\":{\"location\":{\"lat\":43.658026,\"lng\":7.189718300000001},\"viewport\":{\"northeast\":{\"lat\":43.65927682989272,\"lng\":7.191075679892721},\"southwest\":{\"lat\":43.65657717010728,\"lng\":7.188376020107277}}},\"icon\":\"https://maps.gstatic.com/mapfiles/place_api/icons/restaurant-71.png\",\"id\":\"43d972ed24c91703f2a1d374f18bfc4bee80036d\",\"name\":\"La Cabane\",\"opening_hours\":{\"open_now\":true,\"weekday_text\":[]},\"photos\":[{\"height\":3456,\"html_attributions\":[\"<a href=\\\"https://maps.google.com/maps/contrib/104643104582113529066/photos\\\">Sylvain Tournaire</a>\"],\"photo_reference\":\"CmRaAAAA5LaoNTkSIlQj7YHSAVCgOXky_1RtzPhWq7wJpf5V2tTdYxgIK3YsVz4l8uVBippD6uWUxPukpZ5LtwV9-NYMHJXa3Re8NadrOOCSeVKYqvIL3zAg_LGaE8vsb1oJ0TqtEhBvqVk8U7ZeRu3tO9zQbAJPGhQtCxPd7Vm7Lx-5MDC_L35reKvtkQ\",\"width\":4608}],\"place_id\":\"ChIJh42D5lzRzRIRGb9eQqFKbkA\",\"rating\":4.2,\"reference\":\"CmRbAAAAHCRBReb-YJ-FZ5zmiuFuMVlIwJozmf4OyWxvbAMoSXZEMl4KVJD6Zo2JxVUlpagKtbwSF_Eore3aSO1fhuliM1VbHnC-4m5UDUAmbuk4JVf42tkojeW_zNaMpgQqKn3SEhBg0yF_k3YLgk31cxAciwXzGhTwILuWm1UWrf3tGfkIKVrRbo_jdA\",\"scope\":\"GOOGLE\",\"types\":[\"restaurant\",\"bar\",\"food\",\"point_of_interest\",\"establishment\"],\"vicinity\":\"167 Prom. des Flots Bleus, Saint-Laurent-du-Var\"}]";
            String responseString = intent.getStringExtra(GPSService.RESPONSE_STRING);
            System.out.println(responseString);
            try {
                JSONArray array = new JSONArray(responseString);
                for(int i = 0; i < array.length(); i++){
                    JSONObject obj = array.getJSONObject(i);
                    String name = obj.getString("name");
                    String iconPath = obj.getString("icon");
                    Double lat = obj.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                    Double lon = obj.getJSONObject("geometry").getJSONObject("location").getDouble("lng");
                    String adresse = obj.getString("vicinity");
                    Place p = new Place(name,lat,lon,iconPath,adresse);
                    adapter.notifyDataSetChanged();

                    listView.invalidateViews();
                    //Place place = new Place("Estelle","18 rue acchiardi de saint léger");
                    db.placesDao().insertAll(p);
                    places.clear();
                    places.addAll(db.placesDao().getAll());
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    public class RoutineServiceReceiver extends BroadcastReceiver {

        public static final String PROCESS_RESPONSE = "com.pepin_prestini.elim.myapplication.PROCESS_RESPONSE_ROUTINE";

        @Override
        public void onReceive(Context context, Intent intent) {

            String allPlaces = intent.getStringExtra(GPSService.RESPONSE_STRING);

            //Toast.makeText(context, toto, Toast.LENGTH_LONG).show();
            /*adapter.notifyDataSetChanged();

            listView.invalidateViews();
            Place place = new Place(toto,"18 rue acchiardi de saint léger");
            db.placesDao().insertAll(place);
            places.clear();
            places.addAll(db.placesDao().getAll());*/
        }
    }

    private class MyTask extends AsyncTask<Void, Void, Void> {
        String query;
        public MyTask(String s) {
            query = s;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            inAnimation = new AlphaAnimation(0f, 1f);
            inAnimation.setDuration(200);
            progressBarHolder.setAnimation(inAnimation);
            progressBarHolder.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            outAnimation = new AlphaAnimation(1f, 0f);
            outAnimation.setDuration(200);
            progressBarHolder.setAnimation(outAnimation);
            progressBarHolder.setVisibility(View.GONE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            Intent intent = new Intent(getApplicationContext(), SearchService.class);
            intent.putExtra("searchWord", query);
            startService(intent);
            return null;
        }
    }

}
