package com.goblob.covid.geolocation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.goblob.covid.app.CovidApp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlarmVisibility extends BroadcastReceiver {

    private static final Logger LOG = LoggerFactory.getLogger(AlarmVisibility.class);

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            LOG.debug("Alarm received");
            Intent serviceIntent = new Intent(CovidApp.getInstance(), GpsLoggingService.class);
            serviceIntent.putExtra(IntentConstants.GET_NEXT_VISIBILITY, true);
            context.startService(serviceIntent);
        } catch (Exception ex) {
            LOG.error("AlarmReceiver", ex);
        }
    }
}