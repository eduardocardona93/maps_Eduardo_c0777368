package com.example.maps_eduardo_c0777368;


import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import java.util.TreeSet;

// TAKEN FROM GITHUB PROJECT
// https://github.com/bkiers/GrahamScan

public class GrahamScan {


    /**
     * Returns the points with the lowest y coordinate. In case more than 1 such
     * point exists, the one with the lowest x coordinate is returned.
     *
     * @param points the list of points to return the lowest point from.
     * @return       the points with the lowest y coordinate. In case more than
     *               1 such point exists, the one with the lowest x coordinate
     *               is returned.
     */
    protected static Marker getLowestPoint(List<Marker> points) {

        Marker lowest = points.get(0);

        for(int i = 1; i < points.size(); i++) {

            Marker temp = points.get(i);

            if(temp.getPosition().latitude < lowest.getPosition().latitude || (temp.getPosition().latitude == lowest.getPosition().latitude && temp.getPosition().longitude < lowest.getPosition().longitude)) {
                lowest = temp;
            }
        }

        return lowest;
    }

    /**
     * Returns a sorted set of points from the list <code>points</code>. The
     * set of points are sorted in increasing order of the angle they and the
     * lowest point <tt>P</tt> make with the x-axis. If tow (or more) points
     * form the same angle towards <tt>P</tt>, the one closest to <tt>P</tt>
     * comes first.
     *
     * @param points the list of points to sort.
     * @return       a sorted set of points from the list <code>points</code>.
     * @see GrahamScan#getLowestPoint(java.util.List)
     */
    protected static List<Marker> getSortedPointSet(List<Marker> points) {

        final Marker lowest = getLowestPoint(points);
        List<Marker> setMarker = new ArrayList<>();
        TreeSet<Marker> set = new TreeSet<Marker>(new Comparator<Marker>() {
            @Override
            public int compare(Marker a, Marker b) {

                if(a == b || a.equals(b)) {
                    return 0;
                }

                // use longs to guard against int-underflow
                double thetaA = Math.atan2(a.getPosition().latitude - lowest.getPosition().latitude, a.getPosition().longitude - lowest.getPosition().longitude);
                double thetaB = Math.atan2(b.getPosition().latitude - lowest.getPosition().latitude, b.getPosition().longitude - lowest.getPosition().longitude);

                if(thetaA < thetaB) {
                    return -1;
                }
                else if(thetaA > thetaB) {
                    return 1;
                }
                else {
                    // collinear with the 'lowest' point, let the point closest to it come first

                    // use longs to guard against int-over/underflow
                    double distanceA = Math.sqrt(((lowest.getPosition().longitude - a.getPosition().longitude) * (lowest.getPosition().longitude - a.getPosition().longitude)) +
                            ((lowest.getPosition().latitude - a.getPosition().latitude) * ((long)lowest.getPosition().latitude - a.getPosition().latitude)));
                    double distanceB = Math.sqrt(((lowest.getPosition().longitude - b.getPosition().longitude) * (lowest.getPosition().longitude - b.getPosition().longitude)) +
                            (((long)lowest.getPosition().latitude - b.getPosition().latitude) * ((long)lowest.getPosition().latitude - b.getPosition().latitude)));

                    if(distanceA < distanceB) {
                        return -1;
                    }
                    else {
                        return 1;
                    }
                }
            }
        });

        set.addAll(points);
        for (Marker mk :
                set) {

            setMarker.add(mk);
        }
        return setMarker;
    }

}
