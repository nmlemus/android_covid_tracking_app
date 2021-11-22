package com.goblob.covid.utils;

/**
 * Created by nmlemus on 11/01/18.
 */

public class SexItems implements Listable {

    String sexItem;

    public SexItems(String sexItem){
        this.sexItem = sexItem;
    }


    @Override
    public String getLabel() {
        return this.sexItem;
    }
}
