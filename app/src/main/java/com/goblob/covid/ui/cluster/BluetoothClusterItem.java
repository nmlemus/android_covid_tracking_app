package com.goblob.covid.ui.cluster;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

public class BluetoothClusterItem extends CovidClusterItem {
    private String macAddress;
    private int crosses;


    public BluetoothClusterItem(LatLng position, int crosses, String macAddress, GoogleMap googleMap){
        super(position, googleMap);
        this.crosses = crosses;
        this.macAddress = macAddress;
    }

    public int getCrosses() {
        return crosses;
    }

    public String getMacAddress() {
        return macAddress;
    }

    @Override
    public String getTitle() {
        return macAddress;
    }

    @Override
    public String getSnippet() {
        return "Crosses: "+crosses;
    }
}
