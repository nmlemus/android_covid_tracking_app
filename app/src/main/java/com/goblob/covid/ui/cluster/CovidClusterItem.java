package com.goblob.covid.ui.cluster;

import android.graphics.Color;

import com.goblob.covid.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.clustering.ClusterItem;

import java.util.ArrayList;
import java.util.List;

public abstract class CovidClusterItem implements ClusterItem {
    private LatLng position;
    private GoogleMap googleMap;
    private LatLng newPosition;
    private Polyline polyline;
    private Marker endPolyLine;

    public CovidClusterItem(LatLng position, GoogleMap googleMap){
        this.position = position;
        this.googleMap = googleMap;
    }

    public void removePolyline(){
        this.newPosition = null;
        if (polyline != null){
            polyline.remove();
        }

        if (endPolyLine != null){
            endPolyLine.remove();
        }
    }

    public LatLng getCurrentLocation() {
        return position;
    }


    @Override
    public LatLng getPosition() {
        if (newPosition != null)
            return newPosition;
        return position;
    }

    public LatLng getNewPosition() {
        return newPosition;
    }


    public void setNewPosition(LatLng newPosition) {
        this.newPosition = null;
        if (polyline != null){
            polyline.remove();
        }

        if (endPolyLine != null){
            endPolyLine.remove();
        }

        PolylineOptions options = new PolylineOptions();
        options.color(Color.BLUE);
        options.width(2);
        polyline = googleMap.addPolyline(options);
        List<LatLng> latLngs = new ArrayList<>();
        latLngs.add(getPosition());
        latLngs.add(newPosition);
        polyline.setPoints(latLngs);

        endPolyLine = googleMap.addMarker(new MarkerOptions()
                .position(latLngs.get(0))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.endpolyline)));

        this.newPosition = newPosition;
    }
}
