package com.goblob.covid.data.dao.model;

import com.goblob.covid.ui.cluster.CaseClusterItem;
import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

@ParseClassName("Corregimientos_pa")
public class CorregimientosPA extends ParseObject {
    private CaseClusterItem caseClusterItem;

    public String getSearchName() {
        return getString("name");
    }

    public String getName() {
        return getString("name");
    }

    public int getConfirmed(){
        return getInt("confirmed");
    }

    public ParseGeoPoint getLocation(){
        return getParseGeoPoint("location");
    }

    public void setCaseClusterItem(CaseClusterItem caseClusterItem) {
        this.caseClusterItem = caseClusterItem;
    }

    public CaseClusterItem getCaseClusterItem() {
        return caseClusterItem;
    }
}
