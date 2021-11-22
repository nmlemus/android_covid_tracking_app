package com.goblob.covid.fcm;


import android.content.Context;
import android.content.Intent;

import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONObject;

public class CovidPushBroadcastReceiver extends ParsePushBroadcastReceiver {

    @Override
    public void onPushReceive(final Context context, final Intent intent) {

    }

    @Override
    protected void onPushOpen(Context context, Intent intent) {
        super.onPushOpen(context, intent);
        JSONObject pushData = getPushData(intent);
        if (pushData == null) {
            return;
        }
    }

    @Override
    protected void onPushDismiss(Context context, Intent intent) {
        super.onPushDismiss(context, intent);
        JSONObject pushData = getPushData(intent);
        if (pushData == null) {
            return;
        }
    }
}
