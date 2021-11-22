package com.goblob.covid.data.dao.model;

import com.goblob.covid.ui.cluster.CaseClusterItem;
import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

@ParseClassName("DailyReportAll")
public class DailyReportAll extends ParseObject {
    private CaseClusterItem caseClusterItem;

    public ParseGeoPoint getLocation(){
        return getParseGeoPoint("location");
    }

    public int getConfirmed(){
        return getInt("confirmed");
    }

    public int getRecovered(){
        return getInt("recovered");
    }

    public int getDeaths() {
        return getInt("deaths");
    }

    public String getCountry() {
        return getString("country");
    }

    public String getCountryCode() {
        return getString("country_code");
    }

    public String getProvince() {
        return getString("province");
    }

    public String getCounty() {
        return getString("county");
    }

    public String getSearchName() {
        return getString("country") + " " + getString("province");
    }

    public void setCaseClusterItem(CaseClusterItem caseClusterItem) {
        this.caseClusterItem = caseClusterItem;
    }

    public CaseClusterItem getCaseClusterItem() {
        return caseClusterItem;
    }
}
