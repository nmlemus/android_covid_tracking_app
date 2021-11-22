package com.goblob.covid.geolocation;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

import com.goblob.covid.events.LocationProvidersChanged;

import de.greenrobot.event.EventBus;

/**
 * Created by edel on 30/10/16.
 */

public class GpsLocationReceiver extends BroadcastReceiver {
    @Override public void onReceive(final Context context, Intent intent) {
        if (intent.getAction().matches("android.location.PROVIDERS_CHANGED")) {
            EventBus.getDefault().post(new LocationProvidersChanged(Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF)));
        }
    }
}