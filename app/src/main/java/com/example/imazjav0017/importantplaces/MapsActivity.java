package com.example.imazjav0017.importantplaces;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,GoogleMap.OnMapLongClickListener{

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
        {
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED)
            {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                Location last=locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                centerOnLocation(last,"Your Location");
            }
        }
    }
    public void permission()
    {
        if(Build.VERSION.SDK_INT<23)
        {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
        }
        else
        {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
            }
            else
            {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                Location last=locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                centerOnLocation(last,"Your Location");
            }
        }
    }
    public void centerOnLocation(Location location,String title)
    {
        LatLng place=new LatLng(location.getLatitude(),location.getLongitude());
        mMap.clear();
        if(title!="Your Location") {
            mMap.addMarker(new MarkerOptions().position(place).title(title));
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place,13));
    }
    public void centerOnLocation(LatLng location,String title)
    {
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(location).title(title));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location,13));
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Intent intent=getIntent();
        mMap.setOnMapLongClickListener(this);
        int index=intent.getIntExtra("placeNumber",0);
        locationManager=(LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        if(index==0)
        {
            locationListener=new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    centerOnLocation(location,"Your Location");
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };
            permission();
        }
        else {
            centerOnLocation(MainActivity.locations.get(index),MainActivity.places.get(index));
        }
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        Intent i=getIntent();
        int flag=i.getIntExtra("placeNumber",0);
        if(flag==0) {
            Log.i("long click", "t");
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            String result = "";
            try {
                List<Address> address = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                if (address != null && address.size() > 0) {
                    if (address.get(0).getSubThoroughfare() != null) {
                        result += address.get(0).getSubThoroughfare() + ", ";
                    }
                    if (address.get(0).getThoroughfare() != null) {
                        result += address.get(0).getThoroughfare() + ", ";
                    }
                    if (address.get(0).getLocality() != null) {
                        result += address.get(0).getLocality() + ", ";
                    }
                    if (address.get(0).getPostalCode() != null) {
                        result += address.get(0).getPostalCode() + ", ";
                    }
                    if (address.get(0).getCountryName() != null) {
                        result += address.get(0).getCountryName();
                    }
                }

                if (result == "") {
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd-MM-yyyy");
                    result = sdf.format(new Date());
                }
                centerOnLocation(latLng, result);
                Toast.makeText(this, "Location Added", Toast.LENGTH_SHORT).show();
                MainActivity.places.add(result);
                MainActivity.locations.add(latLng);
                MainActivity.arrayAdapter.notifyDataSetChanged();
                SharedPreferences sharedPreferences=this.getSharedPreferences("com.example.imazjav0017.importantplaces",Context.MODE_PRIVATE);
                ArrayList<String>latitudes=new ArrayList<>();
                ArrayList<String> longitudes=new ArrayList<>();
                for(LatLng coordinates:MainActivity.locations) {
                    latitudes.add(Double.toString(coordinates.latitude));
                    longitudes.add(Double.toString(coordinates.longitude));
                }
                sharedPreferences.edit().putString("places",ObjectSerializer.serialize(MainActivity.places)).apply();
                sharedPreferences.edit().putString("latitudes",ObjectSerializer.serialize(latitudes)).apply();
                sharedPreferences.edit().putString("longitudes",ObjectSerializer.serialize(longitudes)).apply();


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
