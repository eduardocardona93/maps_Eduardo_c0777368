package com.example.maps_eduardo_c0777368;


import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuickHullMarker {

    public ArrayList<Marker> quickHull(List<Marker> markers) {

        final ArrayList<Marker> convexHull = new ArrayList<>();

        int minPoint = -1;
        int maxPoint = -1;
        double minX = Integer.MAX_VALUE;
        double maxX = Integer.MIN_VALUE;

        for (int i = 0; i < markers.size(); i++) {
            if (markers.get(i).getPosition().longitude < minX) {
                minX = markers.get(i).getPosition().longitude;
                minPoint = i;
            }
            if (markers.get(i).getPosition().longitude > maxX) {
                maxX = markers.get(i).getPosition().longitude;
                maxPoint = i;
            }
        }

        final Marker a = markers.get(minPoint);
        final Marker b = markers.get(maxPoint);
        convexHull.add(a);
        convexHull.add(b);
        markers.remove(a);
        markers.remove(b);

        ArrayList<Marker> leftSet = new ArrayList<>();
        ArrayList<Marker> rightSet = new ArrayList<>();

        for(Marker p : markers){
            if (pointLocation(a, b, p) == -1){
                leftSet.add(p);
            }else {
                rightSet.add(p);
            }
        }

        hullSet(a, b, rightSet, convexHull);
        hullSet(b, a, leftSet, convexHull);

        return convexHull;
    }

    private static void hullSet(Marker a, Marker b, ArrayList<Marker> set, ArrayList<Marker> convexHull) {
        final int insertPosition = convexHull.indexOf(b);
        if (set.size() == 0) return;
        if (set.size() == 1) {
            final Marker p = set.get(0);
            set.remove(p);
            convexHull.add(insertPosition, p);
            return;
        }
        double dist = Integer.MIN_VALUE;
        int furthestPoint = -1;
        for(int i = 0 ; i < set.size() ; i++){
            Marker p = set.get(i);
            double distance = distance(a, b, p);
            if (distance > dist) {
                dist = distance;
                furthestPoint = i;
            }
        }

        final Marker p = set.get(furthestPoint);
        set.remove(furthestPoint);
        convexHull.add(insertPosition, p);

        // Determine who's to the left of AP
        final ArrayList<Marker> leftSetAP = new ArrayList<>();
        for(Marker m : set){
            if (pointLocation(a, p, m) == 1) {
                leftSetAP.add(m);
            }
        }

        // Determine who's to the left of PB
        final ArrayList<Marker> leftSetPB = new ArrayList<>();
        for(Marker m : set){
            if (pointLocation(p, b, m) == 1) {
                leftSetPB.add(m);
            }
        }

        hullSet(a, p, leftSetAP, convexHull);
        hullSet(p, b, leftSetPB, convexHull);
    }

    private static double distance(Marker a, Marker b, Marker c) {
        final double ABx = b.getPosition().longitude - a.getPosition().longitude;
        final double ABy = b.getPosition().latitude - a.getPosition().latitude;
        double dist = ABx * (a.getPosition().latitude - c.getPosition().latitude) - ABy * (a.getPosition().longitude - c.getPosition().longitude);
        if (dist < 0) dist = -dist;
        return dist;
    }

    private static int pointLocation(Marker a, Marker b, Marker p) {
        double cp1 = (b.getPosition().latitude - a.getPosition().latitude) * (p.getPosition().longitude - a.getPosition().longitude) - (b.getPosition().longitude - a.getPosition().longitude) * (p.getPosition().latitude - a.getPosition().latitude);
        return (cp1 > 0) ? 1 : -1;
    }
}