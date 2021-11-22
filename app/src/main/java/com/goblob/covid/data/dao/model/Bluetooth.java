package com.goblob.covid.data.dao.model;

import com.goblob.covid.geolocation.Maths;
import com.goblob.covid.ui.cluster.BluetoothClusterItem;
import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.List;

@ParseClassName("Bluetooth")
public class Bluetooth extends ParseObject {
    private BluetoothClusterItem bluetoothClusterItem;
    private List<ParseGeoPoint> locations = new ArrayList<>();
    private int crosses = 0;

    public BluetoothClusterItem getBluetoothClusterItem() {
        return bluetoothClusterItem;
    }

    public void setBluetoothClusterItem(BluetoothClusterItem bluetoothClusterItem) {
        this.bluetoothClusterItem = bluetoothClusterItem;
    }

    public ParseGeoPoint getLocation() {
        return getParseGeoPoint("location");
    }

    public String getMacAddress() {
        return getString("device2Address");
    }

    public List<ParseGeoPoint> getLocations() {
        return locations;
    }

    public int getCrosses() {
        return crosses;
    }

    public void addLocation(ParseGeoPoint location) {
        boolean e = false;
        for (ParseGeoPoint l : locations) {
            double distanceTravelled = Maths.calculateDistance(l.getLatitude(), l.getLongitude(), location.getLatitude(), location.getLongitude());
            if (distanceTravelled <= 100) {
                e = true;
                break;
            }
        }
        if (!e) {
            locations.add(location);
        }
        crosses++;
    }
}
