package com.goblob.covid.geolocation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StartupReceiver extends BroadcastReceiver {

    private static final Logger LOG = LoggerFactory.getLogger((StartupReceiver.class));

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            Intent serviceIntent = new Intent(context, GpsLoggingService.class);
            if (GoblobLocationManager.getInstance().isStarted()) {
                serviceIntent.putExtra(IntentConstants.IMMEDIATE_START, true);
            }
            context.startService(serviceIntent);
        } catch (Exception ex) {
            LOG.error("StartupReceiver", ex);
        }
    }

}