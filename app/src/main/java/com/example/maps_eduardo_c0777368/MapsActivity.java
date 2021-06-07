package com.example.maps_eduardo_c0777368;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.maps_eduardo_c0777368.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private final int REQUEST_CODE = 1;

    Polyline line;
    Polygon shape;

    private final int POLYGON_POINTS = 4;
    List<Marker> markers = new ArrayList<>();
    List<Polyline> lines = new ArrayList<>();

    LocationManager locationManager;
    LocationListener locationListener;

    Location homelocation = null;

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // set the home location
                homelocation = location;
                LatLng userLocation = new LatLng(homelocation.getLatitude(), homelocation.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 9));
            }
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}
            @Override
            public void onProviderEnabled(String provider) {}
            @Override
            public void onProviderDisabled(String provider) {}
        };
        // permission check
        if (!checkPermission()){
            requestPermission();
        } else{
            setupMap();
        }
    }

    @SuppressLint("MissingPermission")
    private void setupMap() {
        mMap.setMyLocationEnabled(true);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 100, locationListener);
        // marker drag
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(@NonNull Marker marker) {}
            @Override
            public void onMarkerDrag(@NonNull Marker marker) {}
            @Override
            public void onMarkerDragEnd(@NonNull Marker marker) {
                markers.remove(marker);
                marker.remove();
                Location location = new Location("Your Destination");
                location.setLatitude(marker.getPosition().latitude);
                location.setLongitude(marker.getPosition().longitude);
                // set Marker
                setMarker(location);
            }
        });
        // long press on map
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                Location location = new Location("Your Destination");
                location.setLatitude(latLng.latitude);
                location.setLongitude(latLng.longitude);
                // set Marker
                setMarker(location);
            }
        });
        // line tap
        mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(@NonNull Polyline polyline) {
                for (Polyline pl : lines) {
                    pl.setWidth(5);
                }
                polyline.setWidth(10);
            }
        });
        // polygon tap


    }
//
    private void setMarker(Location location) {
//        mMap.clear();
        LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        MarkerOptions options = new MarkerOptions().position(userLatLng)
                .title("Your Destination")
                .snippet(String.valueOf(location.distanceTo(homelocation)))
                .draggable(true);

        /*
         * this checks if there are already the same number of markers as
         * the polygon points, so we clear the map
         * */
//        if (markers.size() == POLYGON_POINTS)
//            for (Marker marker : markers){
//                marker.remove();
//            }
//
//        markers.clear();
//        if (shape != null){
//            shape.remove();
//            shape = null;
//        }

        markers.add(mMap.addMarker(options));

        /*
         * this check is when we reach the number of markers needed for drawing polygon
         * */
//        if (markers.size() == POLYGON_POINTS)
//            drawShape();


    }
//    private void drawLine(Marker markerA, Marker markerB) {
//        PolylineOptions options = new PolylineOptions()
//                .add(markerA.getPosition(), markerB.getPosition())
//                .color(Color.RED)
//                .width(5);
//        lines.add(mMap.addPolyline(options));
//    }
//
//    private void drawShape() {
//        PolygonOptions options = new PolygonOptions()
//                .fillColor(0x330000FF)
//                .strokeWidth(1)
//                .strokeColor(0x330000FF);
//
//        for (int i=0; i<POLYGON_POINTS; i++){
//            options.add(markers.get(i).getPosition());
//        }
//
//        shape = mMap.addPolygon(options);
//    }


    // ------------------------------------- PERMISSIONS -------------------------------------
    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
    }
    private boolean checkPermission() {
        int permissionStatus = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionStatus == PackageManager.PERMISSION_GRANTED;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (REQUEST_CODE == requestCode) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
            }
        }
    }
}