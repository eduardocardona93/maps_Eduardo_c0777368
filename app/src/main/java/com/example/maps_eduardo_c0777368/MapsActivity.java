package com.example.maps_eduardo_c0777368;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorSpace;
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
import com.google.maps.android.ui.IconGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private final int REQUEST_CODE = 1;

    Polyline line;
    Polygon shape;


    List<Marker> markers = new ArrayList<>();
    List<Marker> polyMarkers = new ArrayList<>();
    List<Polyline> lines = new ArrayList<>();
    private final String markerLetters[] = {"A","B","C","D"};
    private final int POLYGON_POINTS = markerLetters.length;
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
                for(Polyline line : lines)
                    line.remove();
                lines.clear();
                for (Marker plMkr : polyMarkers)
                    plMkr.remove();
                polyMarkers.clear();
                if (shape != null){
                    shape.remove();
                    shape = null;
                }
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
                        for(Polyline line : lines)
                            line.remove();
                        lines.clear();
                        for (Marker plMkr : polyMarkers)
                            plMkr.remove();
                        polyMarkers.clear();

                        if (shape != null){
                            shape.remove();
                            shape = null;
                        }
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

                for (Marker mk : polyMarkers)
                        mk.remove();

                for (Polyline pl : lines) {
                    LatLng LatLngA = pl.getPoints().get(0);
                    Location locationA = new Location("locationA");
                    locationA.setLatitude(LatLngA.latitude);
                    locationA.setLongitude(LatLngA.longitude);
                    LatLng LatLngB = pl.getPoints().get(1);
                    Location locationB = new Location("locationB");
                    locationB.setLatitude(LatLngB.latitude);
                    locationB.setLongitude(LatLngB.longitude);

                    LatLng LatLngMiddle = new LatLng((LatLngA.latitude + LatLngB.latitude)/2, (LatLngA.longitude + LatLngB.longitude)/2);

                    IconGenerator icnGenerator = new IconGenerator(getBaseContext());
                    icnGenerator.setColor(Color.parseColor("#000000"));
                    Bitmap iconBitmap = icnGenerator.makeIcon(String.format(" %.2f km", locationA.distanceTo(locationB)/1000.0));
                    MarkerOptions options = new MarkerOptions().position(LatLngMiddle)
                            .icon(BitmapDescriptorFactory.fromBitmap(iconBitmap));
                    polyMarkers.add(mMap.addMarker(options));
                }


            }
        });
        // polygon tap
        mMap.setOnPolygonClickListener(new GoogleMap.OnPolygonClickListener() {
            @Override
            public void onPolygonClick(@NonNull Polygon polygon) {
                for (Marker mk : polyMarkers)
                        mk.remove();

                double totalDistance = 0.0;
                for(int i=0;  i<POLYGON_POINTS; i++){
                    Location locationA = new Location("locationA");
                    locationA.setLatitude(markers.get(i).getPosition().latitude);
                    locationA.setLongitude(markers.get(i).getPosition().longitude);
                    if(i > 0 ) {
                        Location locationB = new Location("locationB");
                        locationB.setLatitude(markers.get(i-1).getPosition().latitude);
                        locationB.setLongitude(markers.get(i-1).getPosition().longitude);
                        totalDistance += locationA.distanceTo(locationB);
                    }
                    if (i == POLYGON_POINTS - 1 ){
                        Location locationC = new Location("locationC");
                        locationC.setLatitude(markers.get(i-1).getPosition().latitude);
                        locationC.setLongitude(markers.get(i-1).getPosition().longitude);
                        totalDistance += locationA.distanceTo(locationC);
                    }
                }
                IconGenerator icnGenerator = new IconGenerator(getBaseContext());
                icnGenerator.setColor(Color.parseColor("#000000"));
                Bitmap iconBitmap = icnGenerator.makeIcon(String.format("A-B-C-D \n %.2f km", totalDistance/1000.0));
                MarkerOptions options = new MarkerOptions().position(new LatLng(homelocation.getLatitude(), homelocation.getLongitude()))
                        .icon(BitmapDescriptorFactory.fromBitmap(iconBitmap));
                polyMarkers.add(mMap.addMarker(options));
            }

        });
    }

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

                    Marker newMarker = mMap.addMarker(options);

                    markers.add(newMarker);
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


        for (Marker plMkr : polyMarkers)
            plMkr.remove();
        for(Polyline line : lines)
            line.remove();
        lines.clear();
        polyMarkers.clear();
        if (shape != null){
            shape.remove();
            shape = null;
        }
        if (markers.size() == POLYGON_POINTS)
            markerSorter();
            drawShape();
    }


    private void drawShape() {
        PolygonOptions polygon = new PolygonOptions()
                .fillColor(0x59008800)
                .strokeColor(0x59008800)
                .strokeWidth(1);
        for(int i=0;  i<POLYGON_POINTS; i++){

            if(i > 0) {

                PolylineOptions polyline = new PolylineOptions()
                        .color(Color.RED)
                        .width(5)
                        .add(markers.get(i-1).getPosition(), markers.get(i).getPosition());
                line = mMap.addPolyline(polyline);
                lines.add(line);
                line.setClickable(true);
            }

            if (i == POLYGON_POINTS - 1 ){
                PolylineOptions polyline = new PolylineOptions()
                        .color(Color.RED)
                        .width(5)
                        .add(markers.get(i).getPosition(), markers.get(0).getPosition());
                line =mMap.addPolyline(polyline);
                lines.add(line);
                line.setClickable(true);

            }
            polygon.add(markers.get(i).getPosition());
        }
        shape = mMap.addPolygon(polygon);
        shape.setClickable(true);
    }
    public void markerSorter(){

    }

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