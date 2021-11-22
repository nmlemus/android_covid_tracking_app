package com.goblob.covid.notification;

import android.app.DownloadManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.goblob.covid.app.CovidApp;
import com.goblob.covid.dagger.Injectable;
import com.goblob.covid.data.dao.GetCallback;
import com.goblob.covid.data.dao.factory.DAOFactory;
import com.goblob.covid.data.dao.model.Notification;
import com.parse.PLog;
import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class NotificationManager {

    private final Executor executor = Executors.newFixedThreadPool(2);

    private static NotificationManager instance;
    private DownloadManager downloadmanager;
    private Context context;
    private DAOFactory daoFactory;

    public static NotificationManager get() {
        if (instance == null) {
            instance = new NotificationManager();
        }
        return instance;
    }

    public NotificationManager() {
        this.context = CovidApp.getInstance();
        daoFactory = Injectable.get().getDaoFactory();
        // downloadmanager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        // context.registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }


    public void notificationReceived(final Bundle extras) {
        String pushDataStr = extras.getString(ParsePushBroadcastReceiver.KEY_PUSH_DATA);
        if (pushDataStr == null) {
            PLog.e(TAG, "Can not get push data from intent.");
            // listener.done(false);
            return;
        }

        try {
            JSONObject pushData = new JSONObject(pushDataStr);
            if (pushData != null) {

                Log.d("Notification: ", pushData.toString());

                String notificationText = pushData.optString("message");
                String image = pushData.optString("image");
                String notificationTitle = pushData.optString("title");
                String alert = pushData.optString("alert");
                String remoteFile = pushData.optString("url");
                String sender = pushData.optString("sender");
                String created_at = pushData.optString("created_at");
                String sent_at = pushData.optString("sentAt");
                String received_at = pushData.optString("received_at");
                String notificationId = pushData.optString("messageId");

                // long uid = Long.parseLong(pushData.optString("uid"));

                Notification notification = new Notification();
                notification.setMessageText(notificationText);
                notification.setName(notificationTitle);
                notification.setCreated_at(created_at);
                notification.setSent_at(sent_at);
                notification.setRemoteFile(remoteFile);
                notification.setLocalFile(image);

                notification.setStatus("UNREAD");

                daoFactory.getNotificationDao().insert(notification);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getLastNotifications(final int limit, final GetCallback<List<Notification>> callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                List<Notification> notificationList = daoFactory.getNotificationDao().getLastNotifications(limit);
                callback.done(notificationList, null);
            }
        });
    }

    public void updateNotification(final Notification notification) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                daoFactory.getNotificationDao().update(notification);
            }
        });
    }

    public void getUnreadNotifications(final GetCallback<Integer> callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                int notificationCount = daoFactory.getNotificationDao().countUnreadNotifications();
                callback.done(notificationCount, null);
            }
        });
    }
}
