package com.toilet.gottago.ui;

import android.Manifest;
import android.app.FragmentManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.toilet.gottago.R;
import com.toilet.gottago.util.Toilet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


public class MainActivity extends AppCompatActivity implements
        OnMapReadyCallback {
    private static final String TAG = "MainActivity";
    private LocationManager manager;
    private GoogleMap mMap;
    private FirebaseFirestore mDatabase = FirebaseFirestore.getInstance();
//    private SearchView searchView;
//    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
//    private DatabaseReference ref;

    private boolean isMoved = false;

    public LatLng curPoint = new LatLng(37.4979,127.028);
    public String cityName_gu = "강남구";
    public String cityName_dong = "역삼동";

    public List<Toilet> itemList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager fragmentManager = getFragmentManager();
        MapFragment mapFragment = (MapFragment) fragmentManager
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Button btn = findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMarker();
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(curPoint, 17));
            }
        });

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

        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, gpsListener);

        Toast.makeText(getApplicationContext(),"Location Service started", Toast.LENGTH_SHORT).show();
    }

    private class GPSListener implements LocationListener{
//        public Double distance = (6371 * Math.acos( Math.cos( Math.toRadians( curPoint.latitude ) ) * Math.cos( Math.toRadians( 위도) )
//                * Math.cos( Math.toRadians( 경도) - Math.toRadians(curPoint.longitude)) + Math.sin( Math.toRadians(curPoint.latitude) ) * Math.sin( Math.toRadians(위도) ) ) );

        @Override
        public void onLocationChanged(Location location){
            Double lat = location.getLatitude();
            Double lng = location.getLongitude();
            isMoved = checkMoving(lat, lng);
            if(isMoved){
                String msg = "Lattitude:" + lat + "Longitude:" + lng;
                Log.i("GPSListener",msg);
                itemList.clear();
                showCurrentLocation(lat, lng);
                getItemInfo();
            }
        }

        private void showCurrentLocation(Double lat, Double lng) {
            //움직였으면 curpoint 최신화
            curPoint = new LatLng(lat, lng);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(curPoint, 17));
        }

        private boolean checkMoving(Double lat, Double lng) {
            if(curPoint.latitude != lat || curPoint.longitude != lng){
                //초기위치와 현재위치가 다르면 움직인것
                return true;
            } else {
                return false;
            }
        }

        private void getCityName(Double lat, Double lng) {
            Geocoder geocoder = new Geocoder(getBaseContext(), Locale.getDefault());
            List<Address> addresses;
            try{
                addresses = geocoder.getFromLocation(lat, lng, 1);
//                cityName = addresses.get(0).getAddressLine(0);
                cityName_gu = addresses.get(0).getSubLocality();
                cityName_dong = addresses.get(0).getThoroughfare();
                String msg = "current City Name:" + cityName_gu + " " + cityName_dong;
                Log.i("GPSListener/", msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void getItemInfo() {
            getCityName(curPoint.latitude, curPoint.longitude);
            mDatabase.collection("log_info")
                    .whereEqualTo("gu_nm", cityName_gu)
//                    .whereEqualTo("hnr_nam", cityName_dong)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
//                                    Double item_lat = document.getDouble("lat");
//                                    Double item_lng = document.getDouble("lng");
                                    Toilet toilet = document.toObject(Toilet.class);
                                    //반경
                                    double distance = (6371 * Math.acos(Math.cos(Math.toRadians(curPoint.latitude)) * Math.cos(Math.toRadians(toilet.lat))
                                            * Math.cos(Math.toRadians(toilet.lng) - Math.toRadians(curPoint.longitude)) + Math.sin(Math.toRadians(curPoint.latitude)) * Math.sin(Math.toRadians(toilet.lat))));
                                    if (distance < 0.3) {
                                        itemList.add(toilet);
                                        Log.d(TAG, "item lat lng: " + toilet.lat + " / " + toilet.lng);
                                    } else {
                                        Log.d(TAG, "getItenInfo():Not match");
                                    }
                                }
                            } else {
                                Log.w(TAG, "Error getting documents.", task.getException());
                            }
                        }
                    });
        }


        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) { }
        @Override
        public void onProviderEnabled(String provider) { }
        @Override
        public void onProviderDisabled(String provider) { }

    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        enableMyLocation();
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

    public void setMarker() {
        for(Toilet toilet : itemList){
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions
                    .position(new LatLng(toilet.lat, toilet.lng))
                    .title("화장실" + toilet.objectid);
            //show marker
            mMap.addMarker(markerOptions);
        }
    }



}
