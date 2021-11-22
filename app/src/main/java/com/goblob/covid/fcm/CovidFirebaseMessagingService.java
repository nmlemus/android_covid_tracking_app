package com.goblob.covid.fcm;


import android.os.Bundle;
import android.util.Log;

import com.goblob.covid.notification.NotificationManager;
import com.google.firebase.messaging.RemoteMessage;
import com.parse.PLog;
import com.parse.ParsePushBroadcastReceiver;
import com.parse.fcm.ParseFirebaseMessagingService;

import org.json.JSONException;
import org.json.JSONObject;

public class CovidFirebaseMessagingService extends ParseFirebaseMessagingService {

    private static final String TAG = CovidFirebaseMessagingService.class.getSimpleName();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        String dataString = remoteMessage.getData().get("data");

        JSONObject data = null;
        if (dataString != null) {
            try {
                data = new JSONObject(dataString);
            } catch (JSONException e) {
                PLog.e(TAG, "Ignoring push because of JSON exception while processing: " + dataString, e);
                return;
            }
        }

        final Bundle extras = new Bundle();
        extras.putString(ParsePushBroadcastReceiver.KEY_PUSH_DATA, data.toString());

        NotificationManager.get().notificationReceived(extras);
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }
}
