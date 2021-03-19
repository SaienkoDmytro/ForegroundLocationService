package com.example.foregroundlocationservice;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public final static String PARAM_LATITUDE = "LATI";
    public final static String PARAM_LONGITUDE = "LONGI";
    public final static String LOCATIONS_KEY = "LOCATIONS";
    public final static String BROADCAST_ACTION = "com.example.foregroundlocationservice";

    private BroadcastReceiver broadcastReceiver;
    private RecyclerAdapter locationAdapter;
    private ArrayList<DataModel> locations = new ArrayList<>();

    private final Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPref.init(getApplicationContext());

        RecyclerView mRecyclerView = findViewById(R.id.recycler_set_coordinates_main_activity);
        locationAdapter = new RecyclerAdapter(locations);
        if (loadLocations() != null) {
            locations = loadLocations();
            locationAdapter.notifyData(locations);
        }
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(locationAdapter);

        if (grantPermission()) {
            if (checkLocationEnableOrNot()) {
                Intent intent = new Intent(this, LocationService.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent);
                } else {
                    startService(intent);
                }
            } else new AlertDialog.Builder(this)
                    .setTitle(R.string.alert)
                    .setCancelable(false)
                    .setPositiveButton(R.string.ok, (dialog, which) -> grantPermission()).setNegativeButton(R.string.cancel, null)
                    .show();
        }

        // create BroadcastReceiver
        broadcastReceiver = new BroadcastReceiver() {
            // on receive massages
            public void onReceive(Context context, Intent intent) {
                double latitude = intent.getDoubleExtra(PARAM_LATITUDE, 0);
                double longitude = intent.getDoubleExtra(PARAM_LONGITUDE, 0);
                String lati = "" + latitude;
                String longi = "" + longitude;

                if (locations.size() <= 50) {
                    DataModel obj = new DataModel(lati, longi);
                    locations.add(obj);
                } else {
                    locations.remove(1);
                    DataModel obj = new DataModel(lati, longi);
                    locations.add(obj);
                }
                locationAdapter.notifyData(locations);
             }
        };
        // create BroadcastReceiver filter
        IntentFilter intentFilter = new IntentFilter(BROADCAST_ACTION);
        // start BroadcastReceiver
        registerReceiver(broadcastReceiver, intentFilter);

        }


private boolean checkLocationEnableOrNot() {
    LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    boolean gpsEnable = false;
    boolean netEnable = false;
    try {
        gpsEnable = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    } catch (Exception e){
        e.printStackTrace();
    }
    try {
        netEnable = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    } catch (Exception e){
        e.printStackTrace();
    }

    if (!gpsEnable && !netEnable) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.alerttitle)
                .setCancelable(false)
                .setPositiveButton(R.string.alert_positive, (dialog, which) -> startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))).setNegativeButton(R.string.alert_negative, null)
                .show();
        return false;
    } else return true;
}

    private boolean grantPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
            return false;
        } else {
            return true;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveLocations();
        if (!checkLocationEnableOrNot()) {
            stopService(new Intent(this, LocationService.class));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (loadLocations() != null) {
            locations = loadLocations();
            locationAdapter.notifyData(locations);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveLocations();
        if (!checkLocationEnableOrNot()) {
            stopService(new Intent(this, LocationService.class));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveLocations();
        unregisterReceiver(broadcastReceiver);
        if (!checkLocationEnableOrNot()) {
            stopService(new Intent(this, LocationService.class));
        }
    }

    private void saveLocations(){
        String locationsJson = gson.toJson(locations);
        SharedPref.write(LOCATIONS_KEY, locationsJson);
    }

    private ArrayList<DataModel> loadLocations(){
        String locationsJson = SharedPref.read(LOCATIONS_KEY, "");
        return gson.fromJson(locationsJson, new TypeToken<ArrayList<DataModel>>(){}.getType());
    }

}