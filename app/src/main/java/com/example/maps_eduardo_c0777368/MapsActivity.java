package com.example.maps_eduardo_c0777368;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.maps_eduardo_c0777368.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.maps.android.ui.IconGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;




public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private final int REQUEST_CODE = 1;

    Polygon shape;


    List<Marker> markers = new ArrayList<>();
    List<Marker> polyMarkers = new ArrayList<>();
    List<Polyline> lines = new ArrayList<>();
    private final String[] markerLetters = {"A", "B", "C", "D"};
    private final int POLYGON_POINTS = markerLetters.length;

    // Fused location provider client
    private FusedLocationProviderClient mClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private static final int UPDATE_INTERVAL = 5000; // 5 seconds
    private static final int FASTEST_INTERVAL = 3000; // 3 seconds

    Location homelocation = null;
    SupportMapFragment mapFragment;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        mClient = LocationServices.getFusedLocationProviderClient(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // permission check
        if (!checkPermission()) {
            requestPermission();// request permissions
        } else {
            startUpdateLocation();

        }
    }

    @SuppressLint("MissingPermission")
    private void startUpdateLocation() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(1000);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                mMap.clear();
                if (locationResult != null) {
                    homelocation = locationResult.getLastLocation();
                    LatLng userLocation = new LatLng(homelocation.getLatitude(), homelocation.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(userLocation).title("My current location"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 10));
                }
            }
        };


        mClient.requestLocationUpdates(locationRequest, locationCallback, null);
        setupMap();

    }
    @SuppressLint("MissingPermission")
    private void setupMap() {
        mMap.setMyLocationEnabled(true);
        // map tap
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                removePolyMarkers();
            }
        });
        // marker tap
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {

                if (marker.getTag() != null) {
                    String address = "Could not find the address";
                    Geocoder geocoder = new Geocoder(getBaseContext(), Locale.getDefault());
                    try {
                        List<Address> addressList = geocoder.getFromLocation(marker.getPosition().latitude, marker.getPosition().longitude, 1);
                        if (addressList != null && addressList.size() > 0) {
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

                            removePolyMarkers();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                        Toast.makeText(mapFragment.getContext(), address, Toast.LENGTH_SHORT).show();
                    }else{
                        Snackbar.make(mapFragment.getView(), address, Snackbar.LENGTH_SHORT).show();

                    }
                }

                return false;
            }
        });
        // marker drag
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(@NonNull Marker marker) {
            }

            @Override
            public void onMarkerDrag(@NonNull Marker marker) {
            }

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
                    if (newLocation.distanceTo(itLocation) / 1000.0 < 5.0) {
                        removePolyLines();
                        removePolyMarkers();
                        removePolygon();
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
                removePolyMarkers();
                for (Polyline pl : lines) {

                    LatLng LatLngA = pl.getPoints().get(0);
                    Location locationA = new Location("locationA");
                    locationA.setLatitude(LatLngA.latitude);
                    locationA.setLongitude(LatLngA.longitude);
                    LatLng LatLngB = pl.getPoints().get(1);
                    Location locationB = new Location("locationB");
                    locationB.setLatitude(LatLngB.latitude);
                    locationB.setLongitude(LatLngB.longitude);

                    LatLng LatLngMiddle = new LatLng((LatLngA.latitude + LatLngB.latitude) / 2, (LatLngA.longitude + LatLngB.longitude) / 2);
                    polyMarkers.add(mMap.addMarker(
                            createMarkerOptions(String.format("Line Distance\n%.2f km", locationA.distanceTo(locationB) / 1000.0),
                                    "#000000", null, null, LatLngMiddle, false)));
                }


            }
        });
        // polygon tap
        mMap.setOnPolygonClickListener(new GoogleMap.OnPolygonClickListener() {
            @Override
            public void onPolygonClick(@NonNull Polygon polygon) {
                removePolyMarkers();

                double totalDistance = 0.0;
                for (int i = 0; i < POLYGON_POINTS; i++) {
                    Location locationA = new Location("locationA");
                    locationA.setLatitude(markers.get(i).getPosition().latitude);
                    locationA.setLongitude(markers.get(i).getPosition().longitude);
                    if (i > 0) {
                        Location locationB = new Location("locationB");
                        locationB.setLatitude(markers.get(i - 1).getPosition().latitude);
                        locationB.setLongitude(markers.get(i - 1).getPosition().longitude);
                        totalDistance += locationA.distanceTo(locationB);
                    }
                    if (i == POLYGON_POINTS - 1) {
                        Location locationC = new Location("locationC");
                        locationC.setLatitude(markers.get(i - 1).getPosition().latitude);
                        locationC.setLongitude(markers.get(i - 1).getPosition().longitude);
                        totalDistance += locationA.distanceTo(locationC);
                    }
                }

                polyMarkers.add(mMap.addMarker(createMarkerOptions(String.format("A-B-C-D \n %.2f km", totalDistance / 1000.0), "#000000", null, null,
                        centerOfMass(markers), false)));
            }

        });
    }


    private void setMarker(Location location) {
        if (markers.size() >= POLYGON_POINTS) {
            for (Marker marker : markers)
                marker.remove();
            markers.clear();
        }

        String error = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addressList != null && addressList.size() > 0 && addressList.get(0).getCountryName() != null) {
                if (addressList.get(0).getCountryName().equalsIgnoreCase("Canada")) {

                    LatLng selectedLatLang = new LatLng(location.getLatitude(), location.getLongitude());
                    String letter = getCurrentMarkerLetter();
                    Marker newMarker = mMap.addMarker(createMarkerOptions(letter, "#202146", "Marker " + letter,
                            String.format("%.2f km to your location", location.distanceTo(homelocation) / 1000.0), selectedLatLang, true));

                    newMarker.setTag(letter);
                    markers.add(newMarker);
                } else
                    error = "This location is outside Canada, please try again";
            } else
                error = "Error at creating marker";


        } catch (Exception e) {
            e.printStackTrace();
            error = "Error at creating marker";
        }
        if (!error.isEmpty()){
            if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                Toast.makeText(mapFragment.getContext(), error, Toast.LENGTH_SHORT).show();
            }else{
                Snackbar.make(mapFragment.getView(), error, Snackbar.LENGTH_SHORT).show();

            }
        }


        removePolyMarkers();
        removePolyLines();
        removePolygon();

        if (markers.size() == POLYGON_POINTS) {
            // TAKEN FROM GITHUB PROJECT
            // https://github.com/bkiers/GrahamScan
            List<Marker> tempMarkers = GrahamScan.getSortedPointSet(markers);
            if (tempMarkers.size() == POLYGON_POINTS) {
                markers = tempMarkers;
                drawShape();
            }
        }

    }


    private void drawShape() {
        PolygonOptions polygon = new PolygonOptions()
                .fillColor(0x5900AA00)
                .strokeColor(0x5900AA00)
                .strokeWidth(1);
        for (int i = 0; i < POLYGON_POINTS; i++) {

            if (i > 0) {

                PolylineOptions polyline = new PolylineOptions()
                        .color(Color.RED)
                        .width(5)
                        .add(markers.get(i - 1).getPosition(), markers.get(i).getPosition());
                Polyline line = mMap.addPolyline(polyline);
                line.setClickable(true);

                lines.add(line);

            }

            if (i == POLYGON_POINTS - 1) {
                PolylineOptions polyline = new PolylineOptions()
                        .color(Color.RED)
                        .width(5)
                        .add(markers.get(i).getPosition(), markers.get(0).getPosition());
                Polyline line = mMap.addPolyline(polyline);
                line.setClickable(true);
                lines.add(line);


            }
            polygon.add(markers.get(i).getPosition());
        }
        shape = mMap.addPolygon(polygon);
        shape.setClickable(true);
    }


    public MarkerOptions createMarkerOptions(String iconText, String color, String title, String snippet, LatLng latLng, boolean dragable) {
        IconGenerator icnGenerator = new IconGenerator(getBaseContext());
        icnGenerator.setColor(Color.parseColor(color));
        Bitmap iconBitmap = icnGenerator.makeIcon(iconText);

        MarkerOptions options = new MarkerOptions().position(latLng)
                .title(title)
                .snippet(snippet)
                .icon(BitmapDescriptorFactory.fromBitmap(iconBitmap))
                .draggable(dragable);
        return options;

    }


    public void removePolyMarkers() {
        for (Marker plMkr : polyMarkers)
            plMkr.remove();
        polyMarkers.clear();
    }

    public void removePolyLines() {
        for (Polyline line : lines)
            line.remove();
        lines.clear();
    }

    public void removePolygon() {
        if (shape != null) {
            shape.remove();
            shape = null;
        }
    }


    public static LatLng centerOfMass(List<Marker> markers){
        double totalLat = 0.0,totalLong = 0.0;
        for (Marker mk: markers) {
            totalLat += mk.getPosition().latitude;
            totalLong += mk.getPosition().longitude;
        }
        return new LatLng(totalLat/ (double) markers.size(), totalLong/ (double) markers.size());
    }

    // gets the current map letter to be set
    public String getCurrentMarkerLetter() {
        List<String> arrayLet = markers.stream().map(marker -> {
            return marker.getTag().toString();
        }).collect(Collectors.toList());
        for (String ch : markerLetters) {
            if (!arrayLet.contains(ch)) {
                return ch;
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
        if (requestCode == REQUEST_CODE) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setMessage("The permission is mandatory")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
                            }
                        }).create().show();
            } else
                startUpdateLocation();
        }
    }
}