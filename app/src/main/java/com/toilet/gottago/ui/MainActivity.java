package com.toilet.gottago.ui;

import android.Manifest;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.toilet.gottago.R;

import java.io.IOException;
import java.util.List;

import static com.toilet.gottago.util.Constants.ERROR_DIALOG_REQUEST;
import static com.toilet.gottago.util.Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
import static com.toilet.gottago.util.Constants.PERMISSIONS_REQUEST_ENABLE_GPS;

public class MainActivity extends AppCompatActivity implements
        OnMapReadyCallback
        , GoogleMap.OnMyLocationButtonClickListener {
    private static final String TAG = "MainActivity";
    private boolean mLocationPermissionGranted = false;
    private LocationManager manager;
    private LatLng curPoint;
    private SearchView searchView;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        FragmentManager fragmentManager = getFragmentManager();
        MapFragment mapFragment = (MapFragment) fragmentManager
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        startLocationService();

//        initSearchView(savedInstanceState);
    }


    private void startLocationService() {
        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        String msg = "Last Known Location -> Latitude :" + location.getLatitude() +
                "\nLongitude:" + location.getLongitude();
        Log.i("sampleLocation", msg);
        Toast.makeText(getApplicationContext(),msg, Toast.LENGTH_SHORT).show();

        GPSListener gpsListener = new GPSListener();
        long minTime = 5000;
        float minDistance = 0;

        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                minTime, minDistance, gpsListener);

        Toast.makeText(getApplicationContext(),"Location Service started", Toast.LENGTH_SHORT).show();
    }

    private class GPSListener implements LocationListener{

        @Override
        public void onLocationChanged(Location location){
            Double lat = location.getLatitude();
            Double lng = location.getLongitude();
            String msg = "Lattitude:" + lat + "\nLongitude:" + lng;
            Log.i("GPSListener",msg);
            showCurrentLocation(lat, lng);
        }

        private void showCurrentLocation(Double lat, Double lng) {
            LatLng curPoint = new LatLng(lat, lng);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(curPoint, 15));
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
    }
    
//    private void initSearchView(Bundle savedInstanceState) {
//        searchView = findViewById(R.id.sv_location);
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            //검색어 입력시
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                String location = searchView.getQuery().toString();
//                List<Address> addressList = null;
//
//                if (location != null || !location.equals("")) {
//                    Geocoder geocoder = new Geocoder((MainActivity.this));
//                    try {
//                        addressList = geocoder.getFromLocationName(location, 1);
//                    } catch (IOException e) {
////                        Log.d(TAG, "isServicesOK: an error occured but we can fix it");
//                        e.printStackTrace();
//                    }
//                    Address address = addressList.get(0);
//                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
////                    mMap.addMarker
//                }
//                return false;
//            }
//
//            //검색어 완료시
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                return false;
//            }
//        });
//    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(curPoint, 15));
        enableMyLocation();
        mMap.setOnMyLocationButtonClickListener(this);
    }

    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    //현재위치 버튼 클릭시 내위치 표시
    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

}
