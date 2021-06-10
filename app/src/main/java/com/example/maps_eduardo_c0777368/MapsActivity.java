package com.example.maps_eduardo_c0777368;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
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
    //-----------------Global Variables definition-------------------------
    Polygon shape;

    // Lists
    List<Marker> markers = new ArrayList<>();
    List<Marker> polyMarkers = new ArrayList<>();
    List<Polyline> lines = new ArrayList<>();

    // CONSTANTS
    private final String[] MARKER_LETTERS = {"A", "B", "C", "D"};
    private final int POLYGON_POINTS = MARKER_LETTERS.length;
    private static final int UPDATE_INTERVAL = 5000; // 5 seconds
    private static final int FASTEST_INTERVAL = 3000; // 3 seconds
    private static final int SMALLEST_DISPLACEMENT = 1000; // 1Km

    // Fused location provider client
    private FusedLocationProviderClient mClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    // Another globals
    Location homelocation = null;
    SupportMapFragment mapFragment;
    private GoogleMap mMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        // Set the client
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

    // starts the update location
    @SuppressLint("MissingPermission")
    private void startUpdateLocation() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        locationRequest.setSmallestDisplacement(SMALLEST_DISPLACEMENT);
        locationCallback = new LocationCallback() { // callback method to be called for evey location update if required
            @Override
            public void onLocationResult(LocationResult locationResult) {
                mMap.clear();// clears the map
                if (locationResult != null) { // if the location result exists
                    homelocation = locationResult.getLastLocation(); // sets the home location
                    LatLng userlatlang = new LatLng(homelocation.getLatitude(), homelocation.getLongitude()); // sets the latlang object
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userlatlang, 10)); // moves the camera to the current location with a zoom of 10
                }
            }
        };


        mClient.requestLocationUpdates(locationRequest, locationCallback, null); // sets the request and callback to the client
        setupMap(); // setups the map and the handling event methods

    }
    @SuppressLint("MissingPermission")
    private void setupMap() {
        mMap.setMyLocationEnabled(true);
        // map tap
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                removePolyMarkers(); // removes the markers for distances
            }
        });
        // marker tap
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {

                if (marker.getTag() != null) { // if the marker is a letter marker
                    String address = "Could not find the address"; // default address message
                    Geocoder geocoder = new Geocoder(getBaseContext(), Locale.getDefault()); // sets the geocoder object
                    try {
                        //gets the address list
                        List<Address> addressList = geocoder.getFromLocation(marker.getPosition().latitude, marker.getPosition().longitude, 1);
                        if (addressList != null && addressList.size() > 0) { // if the addressList gets a result
                            address = ""; // empty the address message
                            // street name
                            if (addressList.get(0).getThoroughfare() != null) // if there is a street name
                                address += addressList.get(0).getThoroughfare() + ", "; // add the street name
                            if (addressList.get(0).getPostalCode() != null)  // if there is a postal code name
                                address += addressList.get(0).getPostalCode() + ", "; // add the postal code name
                            if (addressList.get(0).getLocality() != null)  // if there is a city name
                                address += addressList.get(0).getLocality() + ", "; // add the city name
                            if (addressList.get(0).getAdminArea() != null)  // if there is a province name
                                address += addressList.get(0).getAdminArea(); // add the province name

                            removePolyMarkers(); // removes the markers for distances
                        }
                    } catch (Exception e) {
                        e.printStackTrace(); // catch the error
                    }
                    if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) { // validates the version if lesser or equal than 28
                        Toast.makeText(mapFragment.getContext(), address, Toast.LENGTH_SHORT).show(); // show a toast
                    }else{
                        Snackbar.make(mapFragment.getView(), address, Snackbar.LENGTH_SHORT).show(); // show a snackbar
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
            public void onMarkerDragEnd(@NonNull Marker marker) { // when the marker is dropped
                markers.remove(marker); // remove the previous marker from the list
                marker.remove(); // removes the marker from the map
                Location dropLocation = new Location("location"); // sets the drop location object
                dropLocation.setLatitude(marker.getPosition().latitude); // sets the drop latitude
                dropLocation.setLongitude(marker.getPosition().longitude); // sets the drop longitude
                setMarker(dropLocation);// set dropLocation
            }
        });
        // long press on map
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {

                Location newLocation = new Location("newLocation"); // creates the new location object
                newLocation.setLatitude(latLng.latitude); // sets the new latitude
                newLocation.setLongitude(latLng.longitude); // sets the new longitude
                boolean isNew = true; // new marker flag
                for (Marker itMark : markers) { // iterates the previous markers
                    Location itLocation = new Location("itLocation");// creates the iterated marker location object
                    itLocation.setLatitude(itMark.getPosition().latitude); // sets the iterated marker latitude
                    itLocation.setLongitude(itMark.getPosition().longitude); // sets the iterated marker longitude
                    if (newLocation.distanceTo(itLocation) / 1000.0 < 5.0) { // if the new location is within 5km to another existing marker
                        removePolyLines(); // removes the polylines
                        removePolyMarkers(); // removes the markers for distances
                        removePolygon(); // removes the polygon
                        markers.remove(itMark); // removes the iterated marker from the list
                        itMark.remove(); // removes the iterated marker from the map
                        isNew = false; // set the flag as false
                        break; // break the for loop
                    }
                }
                if (isNew) // if the flag is true
                    setMarker(newLocation); // set a new Marker
            }
        });
        // line tap
        mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(@NonNull Polyline pl) { // when a polyline is clicked
                removePolyMarkers();  // removes the markers for distances
                LatLng LatLngA = pl.getPoints().get(0); // gets the coordinates for location a
                Location locationA = new Location("locationA");// creates the location a object
                locationA.setLatitude(LatLngA.latitude);// sets the location a latitude
                locationA.setLongitude(LatLngA.longitude);// sets the location a longitude
                LatLng LatLngB = pl.getPoints().get(1); // gets the coordinates for location b
                Location locationB = new Location("locationB"); // creates the location b object
                locationB.setLatitude(LatLngB.latitude); // sets the location b latitude
                locationB.setLongitude(LatLngB.longitude); // sets the location b longitude
                // calculates the middle point of the line
                LatLng LatLngMiddle = new LatLng((LatLngA.latitude + LatLngB.latitude) / 2, (LatLngA.longitude + LatLngB.longitude) / 2);
                polyMarkers.add( // adds the  marker to the polyline marker list
                        mMap.addMarker( // adds the marker to the map
                        createMarkerOptions(// creates the custom marker option showing the distance and set in the middle point
                                String.format("Distance\n%.2f km", locationA.distanceTo(locationB) / 1000.0),
                                "#000000", null, null, LatLngMiddle, false)
                        )
                );
            }
        });
        // polygon tap
        mMap.setOnPolygonClickListener(new GoogleMap.OnPolygonClickListener() {
            @Override
            public void onPolygonClick(@NonNull Polygon polygon) {
                removePolyMarkers();// removes the markers for distances

                double totalDistance = 0.0;
                // iterates the marker points starting from the 2nd marker
                for (int i = 1; i < POLYGON_POINTS; i++) {
                    /*
                    *  Point A = currently iterated point
                    *  Point B = previous iterated point
                    *  Point C = first point of the array
                    */

                    Location locationA = new Location("locationA"); // creates the location a object
                    locationA.setLatitude(markers.get(i).getPosition().latitude);// sets the location a latitude
                    locationA.setLongitude(markers.get(i).getPosition().longitude); // sets the location a longitude

                    Location locationB = new Location("locationB"); // sets the location b longitude
                    locationB.setLatitude(markers.get(i - 1).getPosition().latitude); // sets the location b longitude
                    locationB.setLongitude(markers.get(i - 1).getPosition().longitude); // sets the location b longitude
                    totalDistance += locationA.distanceTo(locationB); // calculates the distance from point a to point b

                    if (i == POLYGON_POINTS - 1) { // if this is the last iteration
                        Location locationC = new Location("locationC"); // sets the location c longitude
                        locationC.setLatitude(markers.get(i - 1).getPosition().latitude); // sets the location c longitude
                        locationC.setLongitude(markers.get(i - 1).getPosition().longitude); // sets the location c longitude
                        totalDistance += locationA.distanceTo(locationC); // calculates the distance from point a to point c
                    }
                }
                LatLng center = centerOfMass(markers); // gets the center of the polygon a.k.a center of mass
                polyMarkers.add( // adds the  marker to the polyline marker list
                        mMap.addMarker(  // adds the marker to the map
                                createMarkerOptions( // creates the custom marker option showing the total distance and set in the center of the polygon
                                        String.format("Distance \n A-B-C-D \n %.2f km", totalDistance / 1000.0), "#000000", null, null,
                                        center, false)
                        )
                );
            }

        });
    }

    // sets the spot marker
    private void setMarker(Location location) {
        if (markers.size() >= POLYGON_POINTS) { // if the markers are already creating a polygon
            for (Marker marker : markers) //iterate markers
                marker.remove();// removes the iterated marker
            markers.clear();// clear the markers array
        }

        String error = ""; // empty the error message
        Geocoder geocoder = new Geocoder(this, Locale.getDefault()); // sets the geocoder object
        try {
            //gets the address list
            List<Address> addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addressList != null && addressList.size() > 0 && addressList.get(0).getCountryName() != null) { // if the addressList gets a result
                if (addressList.get(0).getCountryName().equalsIgnoreCase("Canada")) { // if the location is within canada

                    LatLng selectedLatLang = new LatLng(location.getLatitude(), location.getLongitude()); // gets the latlang object
                    String letter = getCurrentMarkerLetter(); // gets the letter to be assigned
                    Marker newMarker = mMap.addMarker( // adds the marker to the map
                            createMarkerOptions( // creates the custom marker option showing the letter assigned, as well as the distance to the current location in a snippet
                                    letter, "#202146", "Marker " + letter,
                            String.format("%.2f km to your location", location.distanceTo(homelocation) / 1000.0), selectedLatLang, true));

                    newMarker.setTag(letter); // sets the marker tag
                    markers.add(newMarker);  // adds the marker to the list
                } else
                    error = "This location is outside Canada, please try again"; // sets the error message
            } else
                error = "Error at creating marker"; // sets the error message


        } catch (Exception e) {
            e.printStackTrace();
            error = "Error at creating marker"; // sets the error message
        }
        if (!error.isEmpty()){
            if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) { // validates the version if lesser or equal than 28
                Toast.makeText(mapFragment.getContext(), error, Toast.LENGTH_SHORT).show(); // show a toast
            }else{
                Snackbar.make(mapFragment.getView(), error, Snackbar.LENGTH_SHORT).show(); // show a snackbar

            }
        }

        removePolyMarkers(); // removes the polylines
        removePolyLines(); // removes the markers for distances
        removePolygon(); // removes the polygon

        if (markers.size() == POLYGON_POINTS) { // if the markers are enough to draw the polygon
            // TAKEN FROM GITHUB PROJECT
            // https://github.com/bkiers/GrahamScan
            List<Marker> tempMarkers = GrahamScan.getSortedPointSet(markers); // sorts the markers clockwise
            markers = tempMarkers; // temporal marker assignation
            drawShape(); // draws the polygon as well as the polylines
        }

    }

    //draws the polygon and the polylines
    private void drawShape() {
        // sets the polygon options object with a fill green color 35% transparency as well as a stroke same color and width 1
        PolygonOptions polygon = new PolygonOptions()
                .fillColor(0x59005500)
                .strokeColor(0x59005500)
                .strokeWidth(1);

        for (int i = 0; i < POLYGON_POINTS; i++) { // iterates the marker points
            /*
             *  Point A = currently iterated point
             *  Point B = previous iterated point
             *  Point C = first point of the array
             */
            LatLng pointA = markers.get(i).getPosition(); //gets point a

            if( i > 0){
                LatLng pointB = markers.get(i - 1).getPosition(); //gets point b

                // sets the polyline between point a and point b colouring it Red
                Polyline lineAB = mMap.addPolyline(new PolylineOptions()
                        .color(Color.RED)
                        .width(5)
                        .add(pointB, pointA));
                lineAB.setClickable(true); // sets the polyline clickable

                lines.add(lineAB); // adds the line to the array
                if (i == POLYGON_POINTS - 1) {

                    LatLng pointC = markers.get(0).getPosition(); //gets point c
                    // sets the polyline between point a and point c
                    Polyline lineCA = mMap.addPolyline(new PolylineOptions()
                            .color(Color.RED)
                            .width(5)
                            .add(pointA, pointC));
                    lineCA.setClickable(true); // sets the polyline clickable
                    lines.add(lineCA); // adds the line to the array
                }
            }
            polygon.add(pointA); // adds the point a to the polygon
        }

        shape = mMap.addPolygon(polygon); // adds the polygon to the map and sets the global variable
        shape.setClickable(true); // sets the polygon clickable
    }

    // creates the custom marker
    public MarkerOptions createMarkerOptions(String iconText, String color, String title, String snippet, LatLng latLng, boolean draggable) {
        IconGenerator icnGenerator = new IconGenerator(getBaseContext()); // defines the icon generator object
        icnGenerator.setColor(Color.parseColor(color)); // sets the color according to the HEX value assigned
        Bitmap iconBitmap = icnGenerator.makeIcon(iconText); // creates the Bitmap icon
        MarkerOptions options = new MarkerOptions() // creates the marker option object
                .position(latLng) // sets the marker position
                .title(title) // sets the marker title
                .snippet(snippet) // sets the marker snippet content
                .icon(BitmapDescriptorFactory.fromBitmap(iconBitmap)) // sets the bitmap icon as marker icon
                .draggable(draggable); // sets the draggable property
        return options;
    }


    public void removePolyMarkers() {
        for (Marker plMkr : polyMarkers) // iterates all the
            plMkr.remove(); // removes all the polyline markers from the map
        polyMarkers.clear();
    }

    public void removePolyLines() {
        for (Polyline line : lines) // iterates all the
            line.remove(); // removes all the lines from the map
        lines.clear(); // clears the line array
    }

    public void removePolygon() {
        if (shape != null) { // if the polygon exists
            shape.remove(); // removes it from the map
            shape = null; // sets the object as null
        }
    }

    // gets the center of the polygon a.k.a center of mass
    public static LatLng centerOfMass(List<Marker> markers){
        double totalLat = 0.0,totalLong = 0.0; // initialize the accumulators
        for (Marker mk: markers) { // iterates the marker points
            totalLat += mk.getPosition().latitude; // accumulates the latitudes
            totalLong += mk.getPosition().longitude; // accumulates the longitudes
        }
        // returns the center of mass
        return new LatLng(totalLat/ (double) markers.size(), totalLong/ (double) markers.size());
    }

    // gets the current map letter to be set
    public String getCurrentMarkerLetter() {
        // gets all the markers tags
        List<String> arrayLet = markers.stream().map(marker -> {return marker.getTag().toString();}).collect(Collectors.toList());
        for (String ch : MARKER_LETTERS) { // iterates the marker letters
            if (!arrayLet.contains(ch)) { // if the letter is not found
                return ch; // return the missing letter for creation
            }
        }
        return "A"; // return A
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
                new AlertDialog.Builder(this) // show a dialog
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