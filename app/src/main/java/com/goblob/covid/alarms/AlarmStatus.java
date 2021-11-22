package com.goblob.covid.alarms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.content.ContextCompat;

import com.goblob.covid.app.CovidApp;
import com.goblob.covid.geolocation.GpsLoggingService;
import com.goblob.covid.geolocation.IntentConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlarmStatus extends BroadcastReceiver {

    private static final Logger LOG = LoggerFactory.getLogger(AlarmStatus.class);

    @Override
    public void onReceive(Context context, Intent intent) {
        LOG.debug("Alarm received");
        Intent serviceIntent = new Intent(CovidApp.getInstance(), GpsLoggingService.class);
        serviceIntent.putExtra(IntentConstants.HOW_ARE_YOU, true);
        ContextCompat.startForegroundService(context, serviceIntent);
    }
}