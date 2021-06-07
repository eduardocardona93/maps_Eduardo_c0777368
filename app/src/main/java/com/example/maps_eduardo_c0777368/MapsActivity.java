package com.example.maps_eduardo_c0777368;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
import java.util.Locale;
import java.util.stream.Collectors;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private final int REQUEST_CODE = 1;

    Polyline line;
    Polygon shape;

    private final int POLYGON_POINTS = 4;
    List<Marker> markers = new ArrayList<>();
    List<Polyline> lines = new ArrayList<>();
    String markerLetters[] = {"A","B","C","D"};

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
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 10));
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
        // marker tap
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                String address = "Could not find the address";
                Geocoder geocoder = new Geocoder(getBaseContext(), Locale.getDefault());
                try {
                    List<Address> addressList = geocoder.getFromLocation(marker.getPosition().latitude, marker.getPosition().longitude, 1);
                    if (addressList != null && addressList.size() > 0  ) {
                        address = "";
                        // street name
                        if (addressList.get(0).getThoroughfare() != null)
                            address += addressList.get(0).getThoroughfare() + ", ";
                        if (addressList.get(0).getPostalCode() != null)
                            address += addressList.get(0).getPostalCode() + ", ";
                        if (addressList.get(0).getLocality() != null)
                            address += addressList.get(0).getLocality() + ", ";
                        if (addressList.get(0).getAdminArea() != null)
                            address += addressList.get(0).getAdminArea();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Toast.makeText(getApplicationContext(),address, Toast.LENGTH_SHORT).show();
                return false;
            }
        });
        // marker drag
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(@NonNull Marker marker) {
                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
            }
            @Override
            public void onMarkerDrag(@NonNull Marker marker) {}
            @Override
            public void onMarkerDragEnd(@NonNull Marker marker) {
                markers.remove(marker);
                marker.remove();
                Location location = new Location("location");
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
                Location newLocation = new Location("newLocation");
                newLocation.setLatitude(latLng.latitude);
                newLocation.setLongitude(latLng.longitude);
                boolean isNew = true;
                for (Marker itMark : markers) {
                    Location itLocation = new Location("itLocation");
                    itLocation.setLatitude(itMark.getPosition().latitude);
                    itLocation.setLongitude(itMark.getPosition().longitude);
                    if(newLocation.distanceTo(itLocation)/1000.0 < 5.0){
                        markers.remove(itMark);
                        itMark.remove();
                        isNew = false;
                        break;
                    }
                }
                // set Marker
                if (isNew)
                    setMarker(newLocation);
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
                LatLng markA = polyline.getPoints().get(0);
                Location locationA = new Location("locationA");
                locationA.setLatitude(markA.latitude);
                locationA.setLongitude(markA.longitude);
                LatLng markB = polyline.getPoints().get(1);
                Location locationB = new Location("locationB");
                locationB.setLatitude(markB.latitude);
                locationB.setLongitude(markB.longitude);

            }
        });
        // polygon tap


    }
//
    private void setMarker(Location location) {
        if (markers.size() == POLYGON_POINTS){

            for (Marker marker : markers)
                marker.remove();
            markers.clear();
        }

        String error = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addressList != null && addressList.size() > 0 && addressList.get(0).getCountryName() != null ) {
                if (addressList.get(0).getCountryName().equalsIgnoreCase("Canada") ){
                    LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    MarkerOptions options = new MarkerOptions().position(userLatLng)
                            .title("Marker " + getCurrentMarkerLetter())
                            .snippet(String.format("%.2f km to your location",location.distanceTo(homelocation)/1000.0))
                            .draggable(true);

                    markers.add(mMap.addMarker(options));
                }else
                    error = "This location is outside Canada, please try again";
            }else
                error = "Error at creating marker";


        } catch (Exception e) {
            e.printStackTrace();
            error = "Error at creating marker";
        }
        if(!error.isEmpty())
            Toast.makeText(this,error, Toast.LENGTH_SHORT).show();
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
    public String getCurrentMarkerLetter(){
        List<String> arrayLet = markers .stream().map(marker -> { return marker.getTitle(); }).collect(Collectors.toList());
        for (String ch : markerLetters) {
            if(!arrayLet.contains("Marker " + ch)){
               return  ch;
            }
        }
        return "A";
    }

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