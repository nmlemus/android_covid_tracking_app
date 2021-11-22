package com.goblob.covid.ui.cluster;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.Date;

public class CaseClusterItem extends CovidClusterItem {
    private Date createdAt;
    private String title;
    private String snippet;
    private int casesConfirmed;
    private int casesDeaths;
    private int casesRecovered;
    private boolean infoWindowShow;
    private Marker marker;
    private String countryCode;

    public CaseClusterItem(LatLng position, String title, String snippet, int casesConfirmed, int casesRecovered, int casesDeaths, Date createdAt, String countryCode, GoogleMap googleMap){
        super(position, googleMap);
        this.title = title;
        this.snippet = snippet;
        this.casesConfirmed = casesConfirmed;
        this.casesRecovered = casesRecovered;
        this.casesDeaths = casesDeaths;
        this.createdAt = createdAt;
        this.countryCode = countryCode;
    }

    public int getCasesConfirmed() {
        return casesConfirmed;
    }

    public int getCasesRecovered() {
        return casesRecovered;
    }

    public int getCasesDeaths() {
        return casesDeaths;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getSnippet() {
        return snippet;
    }

    public void setInfoWindowShow(boolean infoWindowShow) {
        this.infoWindowShow = infoWindowShow;
    }

    public boolean getInfoWindowShow() {
        return infoWindowShow;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public String getCountryCode() {
        return countryCode;
    }
}
