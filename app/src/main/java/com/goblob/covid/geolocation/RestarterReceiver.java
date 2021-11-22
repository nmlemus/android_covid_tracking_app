package com.goblob.covid.geolocation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.content.ContextCompat;

import com.goblob.covid.app.CovidApp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestarterReceiver extends BroadcastReceiver {

    private static final Logger LOG = LoggerFactory.getLogger(RestarterReceiver.class);

    @Override
    public void onReceive(Context context, Intent intent) {
        LOG.warn("GPSLogger service is being killed, broadcast received. Attempting to restart");
        boolean wasRunning = intent.getBooleanExtra("was_running", false);

        Intent serviceIntent = new Intent(CovidApp.getInstance(), GpsLoggingService.class);

        if(wasRunning){
            serviceIntent.putExtra(IntentConstants.IMMEDIATE_START, true);
        }
        else {
            serviceIntent.putExtra(IntentConstants.IMMEDIATE_STOP, true);
        }

        ContextCompat.startForegroundService(context, serviceIntent);
    }
}
