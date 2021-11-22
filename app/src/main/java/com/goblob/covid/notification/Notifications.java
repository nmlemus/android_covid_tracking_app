package com.goblob.covid.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.util.Pair;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.RemoteInput;

import com.goblob.covid.BuildConfig;
import com.goblob.covid.R;
import com.goblob.covid.app.CovidApp;


import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Notifications {

    public static final String COM_ADOBE_PHONEGAP_PUSH = "com.adobe.phonegap.push";
    public static final String REGISTRATION_ID = "registrationId";
    public static final String FOREGROUND = "foreground";
    public static final String TITLE = "title";
    public static final String NOT_ID = "notId";
    public static final String PUSH_BUNDLE = "pushBundle";
    public static final String ICON = "icon";
    public static final String ICON_COLOR = "iconColor";
    public static final String SOUND = "sound";
    public static final String VIBRATE = "vibrate";
    public static final String ACTIONS = "actions";
    public static final String CALLBACK = "callback";
    public static final String DRAWABLE = "drawable";
    public static final String MSGCNT = "msgcnt";
    public static final String VIBRATION_PATTERN = "vibrationPattern";
    public static final String STYLE = "style";
    public static final String SUMMARY_TEXT = "summaryText";
    public static final String PICTURE = "picture";
    public static final String GCM_NOTIFICATION = "gcm.notification";
    public static final String MESSAGE = "message";
    public static final String BODY = "body";
    public static final String SOUNDNAME = "soundname";
    public static final String LED_COLOR = "ledColor";
    public static final String PRIORITY = "priority";
    public static final String IMAGE = "image";
    public static final String STYLE_INBOX = "inbox";
    public static final String STYLE_PICTURE = "picture";
    public static final String STYLE_TEXT = "text";
    public static final String STYLE_MESSAGE = "style_message";
    public static final String BADGE = "badge";
    public static final String INITIALIZE = "init";
    public static final String UNREGISTER = "unregister";
    public static final String EXIT = "exit";
    public static final String ANDROID = "android";
    public static final String SENDER_ID = "senderID";
    public static final String CLEAR_NOTIFICATIONS = "clearNotifications";
    public static final String COLDSTART = "coldstart";
    public static final String ADDITIONAL_DATA = "additionalData";
    public static final String COUNT = "count";
    public static final String FROM = "from";
    public static final String COLLAPSE_KEY = "collapse_key";


    private static final String LOG_TAG = "PushPlugin";
    private static HashMap<Integer, ArrayList<Pair<String, String>>> messageMap = new HashMap<Integer, ArrayList<Pair<String, String>>>();
    private HashMap<String, String> users = new HashMap<>();

    private static Notifications instance;
    private NotificationUtils notificationUtils;

    public static Notifications getInstance() {
        if (instance == null)
            instance = new Notifications();
        return instance;
    }

    public void setNotification(int notId, String messageId, String message) {
        ArrayList<Pair<String, String>> messageList = messageMap.get(notId);
        if (messageList == null) {
            messageList = new ArrayList<>();
            messageMap.put(notId, messageList);
        }

        if (message.isEmpty()) {
            messageList.clear();
        } else {
            messageList.add(new Pair(messageId, message));
        }
    }

    public void createNotification(Context context, Bundle extras) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "hhhhh")
                .setSmallIcon(R.drawable.app_logo)
                .setContentTitle("Notification")
                .setContentText("Notification text")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(111, builder.build());

    }

    public void createNotification(Context context, Bundle extras, boolean newNotification, boolean delete) {
        if(notificationUtils == null) {
            notificationUtils = new NotificationUtils(context);
        }

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String appName = getAppName(context);
        String packageName = context.getPackageName();
        Resources resources = context.getResources();

        /*int notId;
        if (extras.getString("provider").equalsIgnoreCase("group")) {
            notId = 555;
        } else {
            notId = 777;
        }*/
        int notId = parseInt(NOT_ID, extras);
        // int notId = (int) System.currentTimeMillis();

        // Intent region
        //Intent notificationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://goblob.com/chatkitnew"));
        Intent notificationIntent = new Intent();
        notificationIntent.setClassName(BuildConfig.APPLICATION_ID, "com.goblob.covid.ui.chatkit.view.ChatKitActivity");
        notificationIntent.putExtras(extras);

        notificationIntent.setPackage(context.getPackageName());
        notificationIntent.addCategory(Intent.CATEGORY_BROWSABLE);

        if(true) {
            //notificationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://goblob.com/map"));
            notificationIntent = new Intent();
            notificationIntent.setClassName(BuildConfig.APPLICATION_ID, "com.goblob2.map.MapsActivity");
            notificationIntent.putExtra("showProfile", true);
            notificationIntent.putExtra("profileID", extras.getString("contactId"));
        } else if (true) {
            if (extras.getString("message").equalsIgnoreCase("changeBaseLocation")) {
                //notificationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://goblob.com/map"));
                notificationIntent = new Intent();
                notificationIntent.setClassName(BuildConfig.APPLICATION_ID, "com.goblob2.map.MapsActivity");
                notificationIntent.putExtra("showProfile", true);
                notificationIntent.putExtra("profileID", extras.getString("contactId"));
            } else if (extras.getString("provider").equalsIgnoreCase("group")) {
                //notificationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://goblob.com/viewgroup"));
                notificationIntent = new Intent();
                notificationIntent.setClassName(BuildConfig.APPLICATION_ID, "com.goblob2.group.GroupProfileActivity");
            } else {
                //Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://goblob.com/profileview"));
                notificationIntent = new Intent();
                notificationIntent.setClassName(BuildConfig.APPLICATION_ID, "com.goblob2.profile.ViewProfileActivity");
                //notificationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://goblob.com/profileview"));
            }
            notificationIntent.putExtra("contactId", extras.getString("contactId"));
            // notificationIntent.putExtra("userID", profileBasic.getUserID());
            notificationIntent.putExtra("contactName", extras.getString("contactName"));
            notificationIntent.putExtra("contactPhoto", extras.getString("image"));
            notificationIntent.putExtra("currentProfileId", extras.getString("currentProfileId"));
            notificationIntent.putExtra("contactFriendStatus", extras.getString("contactFriendStatus"));
            notificationIntent.putExtra("isMe", false);
            notificationIntent.putExtra("friendsPosition", 0);
        } else if((users.size() > 0 && !users.containsKey(extras.getString("contactName"))) || users.size() > 1){
            //notificationIntent = new Intent(context, MapsActivity2017.class);
        }

        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        notificationIntent.putExtra(PUSH_BUNDLE, extras);
        notificationIntent.putExtra(NOT_ID, notId);

        int requestCode = new Random().nextInt();
        PendingIntent contentIntent = PendingIntent.getActivity(context, requestCode, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // Notification builder
        Notification.Builder mBuilder = notificationUtils.getAndroidNotification();

        mBuilder.setWhen(System.currentTimeMillis());
        mBuilder.setContentTitle(getString(extras, TITLE));
        mBuilder.setTicker(getString(extras, TITLE));
        mBuilder.setSmallIcon(R.drawable.app_logo);
        mBuilder.setColor(context.getResources().getColor(R.color.colorPrimary));
        mBuilder.setContentIntent(contentIntent);
        mBuilder.setAutoCancel(true);
        // mBuilder.setGroup(extras.getString("contactId"));
        mBuilder.setOngoing(false);
        mBuilder.setLights(Color.GREEN, 500, 500);
        mBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.app_logo));

        String localIcon = null;
        String localIconColor = null;
        boolean soundOption = true;
        boolean vibrateOption = true;
        Log.d(LOG_TAG, "stored icon=" + localIcon);
        Log.d(LOG_TAG, "stored iconColor=" + localIconColor);
        Log.d(LOG_TAG, "stored sound=" + soundOption);
        Log.d(LOG_TAG, "stored vibrate=" + vibrateOption);

        /*
         * Notification Vibration
         */

        if (newNotification && !delete) {
            setNotificationVibration(extras, vibrateOption, mBuilder);
        }

        /*
         * Notification Icon Color
         *
         * Sets the small-icon background color of the notification.
         * To use, add the `iconColor` key to plugin android options
         *
         */
        // setNotificationIconColor(getString(extras, "color"), mBuilder, localIconColor);

        /*
         * Notification Icon
         *
         * Sets the small-icon of the notification.
         *
         * - checks the plugin options for `icon` key
         * - if none, uses the application icon
         *
         * The icon value must be a string that maps to a drawable resource.
         * If no resource is found, falls
         *
         */
        // setNotificationSmallIcon(context, extras, packageName, resources, mBuilder, localIcon);

        /*
         * Notification Large-Icon
         *
         * Sets the large-icon of the notification
         *
         * - checks the gcm data for the `image` key
         * - checks to see if remote image, loads it.
         * - checks to see if assets image, Loads It.
         * - checks to see if resource image, LOADS IT!
         * - if none, we don't set the large icon
         *
         */
        setNotificationLargeIcon(extras, packageName, resources, mBuilder);

        /*
         * Notification Sound
         */
        if(newNotification && !delete) {
            if (soundOption) {
                setNotificationSound(context, extras, mBuilder);
            }
        }

        /*
         *  LED Notification
         */
        setNotificationLedColor(extras, mBuilder);

        /*
         *  Priority Notification
         */
        setNotificationPriority(extras, mBuilder);

        /*
         * Notification message
         */
        boolean deleteNotification = setNotificationMessage(notId, extras.getString("messageId"), extras, mBuilder, context, delete);

        /*
         * Notification count
         */
        setNotificationCount(extras, mBuilder);

        /*
         * Notication add actions
         */
        createActions(context, extras, mBuilder, resources, packageName);

        if (deleteNotification){
            mNotificationManager.cancel(appName, notId);
        } else {
            mNotificationManager.notify(appName, notId, mBuilder.build());
        }
    }

    // To create a notification like whatsapp, and answer in the notification without go to the chat interface.
    public Notification createAutoResponseNotification(Context context, Bundle extras, boolean newNotification){
        RemoteInput remoteInput = new RemoteInput.Builder("KEY_TEXT_REPLY")
                .setLabel("Response to...")
                .build();
        //Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://goblob.com/chatkitnew"));
        Intent intent = new Intent();
        intent.setClassName(com.goblob.covid.BuildConfig.APPLICATION_ID, "com.goblob.covid.ui.notification.NotificationFragment");
        PendingIntent replyPendingIntent = PendingIntent.getService(context, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Action action =
                new NotificationCompat.Action.Builder(R.drawable.app_logo,
                        "URGeo", replyPendingIntent)
                        .addRemoteInput(remoteInput)
                        .build();
        return new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.app_logo)
                .setContentTitle(getString(extras, TITLE))
                .setContentText(getString(extras, MESSAGE))
                .addAction(action)
                .build();
    }

    private void createActions(Context context, Bundle extras, Notification.Builder mBuilder, Resources resources, String packageName) {
        Log.d(LOG_TAG, "create actions");
        String actions = getString(extras, ACTIONS);
        if (actions != null) {
            try {
                JSONArray actionsArray = new JSONArray(actions);
                for (int i = 0; i < actionsArray.length(); i++) {
                   /* Log.d(LOG_TAG, "adding action");
                    JSONObject action = actionsArray.getJSONObject(i);
                    Log.d(LOG_TAG, "adding callback = " + action.getString(CALLBACK));
                    Intent intent = new Intent(context, MapsActivity2017.class);
                    intent.putExtra(CALLBACK, action.getString(CALLBACK));
                    intent.putExtra(PUSH_BUNDLE, extras);
                    PendingIntent pIntent = PendingIntent.getActivity(context, i, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                    mBuilder.addAction(resources.getIdentifier(action.getString(ICON), DRAWABLE, packageName),
                            action.getString(TITLE), pIntent);*/
                }
            } catch (JSONException e) {
                // nope
            }
        }
    }

    private void setNotificationCount(Bundle extras, Notification.Builder mBuilder) {
        String msgcnt = getString(extras, MSGCNT);
        if (msgcnt == null) {
            msgcnt = getString(extras, BADGE);
        }
        if (msgcnt != null) {
            mBuilder.setNumber(Integer.parseInt(msgcnt));
        }
    }

    private void setNotificationVibration(Bundle extras, Boolean vibrateOption, Notification.Builder mBuilder) {
        String vibrationPattern = getString(extras, VIBRATION_PATTERN);
        if (vibrationPattern != null) {
            String[] items = vibrationPattern.replaceAll("\\[", "").replaceAll("\\]", "").split(",");
            long[] results = new long[items.length];
            for (int i = 0; i < items.length; i++) {
                try {
                    results[i] = Long.parseLong(items[i]);
                } catch (NumberFormatException nfe) {
                }
            }
            mBuilder.setVibrate(results);
        } else {
            if (vibrateOption) {
                mBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
            }
        }
    }

    private boolean setNotificationMessage(int notId, String messageId, Bundle extras, Notification.Builder mBuilder, Context context, boolean delete) {
        String message = getMessageText(extras);

        String user = extras.getString("contactName");
        if (!users.containsKey(user)) {
            //ContactsManager contactsManager = new ContactsManager(context);
            //String username = contactsManager.getContactName(context, user);
            users.put(user, user);
        }

        /*if (message.equals("changeBaseLocation")) {
            message = context.getResources().getString(R.string.change_base_location);
        } else if (message.equals("changeProfilePhoto")) {
            message = context.getResources().getString(R.string.change_profile_photo);
        }*/

        // message = users.get(user) + ": " + message;

        String style = getString(extras, STYLE, STYLE_TEXT);
        if (STYLE_INBOX.equals(style)) {
            if (!delete) {
                setNotification(notId, messageId, message);
                mBuilder.setContentText(message);
            }

            ArrayList<Pair<String, String>> messageList = messageMap.get(notId);
            Integer sizeList = messageList == null?0:messageList.size();
            if (sizeList > 1) {
                String sizeListMessage = sizeList.toString();
                String stacking = sizeList + " more";
                if (getString(extras, SUMMARY_TEXT) != null) {
                    stacking = getString(extras, SUMMARY_TEXT);
                    stacking = stacking.replace("%n%", sizeListMessage);
                }
                Notification.InboxStyle notificationInbox = new Notification.InboxStyle()
                        .setBigContentTitle(getString(extras, TITLE))
                        .setSummaryText(stacking);

                for (int i = 0; i < messageList.size(); i++) {
                    notificationInbox.addLine(Html.fromHtml(messageList.get(i).second));
                    if (delete && i == messageList.size() -1){
                        mBuilder.setContentText(messageList.get(i).second);
                    }
                }

                mBuilder.setStyle(notificationInbox);
            } else {
                if (delete && sizeList == 0) {
                    return true;
                }

                if (delete && sizeList == 1){
                    message = messageList.get(0).second;
                }
                Notification.BigTextStyle bigText = new Notification.BigTextStyle();
                if (message != null) {
                    bigText.bigText(message);
                    bigText.setBigContentTitle(getString(extras, TITLE));
                    mBuilder.setStyle(bigText);
                }
            }
        } else if (STYLE_PICTURE.equals(style)) {
            setNotification(notId, messageId, "");

            Notification.BigPictureStyle bigPicture = new Notification.BigPictureStyle();
            bigPicture.bigPicture(getBitmapFromURL(getString(extras, PICTURE)));
            bigPicture.setBigContentTitle(getString(extras, TITLE));
            bigPicture.setSummaryText(message);

            mBuilder.setContentTitle(getString(extras, TITLE));
            mBuilder.setContentText(message);

            mBuilder.setStyle(bigPicture);
        } else if (STYLE_MESSAGE.equals(style)) {

        } else {
            mBuilder.setContentTitle(getString(extras, TITLE));
            mBuilder.setContentText(message);
            /*setNotification(notId, "");

            Notification.BigTextStyle bigText = new Notification.BigTextStyle();

            if (message != null) {
                mBuilder.setContentText(Html.fromHtml(message));

                bigText.bigText(message);
                bigText.setBigContentTitle(getString(extras, TITLE));

                String summaryText = getString(extras, SUMMARY_TEXT);
                if (summaryText != null) {
                    bigText.setSummaryText(summaryText);
                }

                mBuilder.setStyle(bigText);
            }*/
            /*
            else {
                mBuilder.setContentText("<missing message content>");
            }
            */
        }
        return false;
    }

    private String getString(Bundle extras, String key) {
        String message = extras.getString(key);
        if (message == null) {
            message = extras.getString(GCM_NOTIFICATION + "." + key);
        }
        return message;
    }

    private String getString(Bundle extras, String key, String defaultString) {
        String message = extras.getString(key);
        if (message == null) {
            message = extras.getString(GCM_NOTIFICATION + "." + key, defaultString);
        }
        return message;
    }

    private String getMessageText(Bundle extras) {
        String message = getString(extras, MESSAGE);
        if (message == null) {
            message = getString(extras, BODY);
        }
        return message;
    }

    private void setNotificationSound(Context context, Bundle extras, Notification.Builder mBuilder) {
        String soundname = getString(extras, SOUNDNAME);
        if (soundname == null) {
            soundname = getString(extras, SOUND);
        }
        if (soundname != null) {
            Uri sound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                    + "://" + context.getPackageName() + "/raw/" + soundname);
            Log.d(LOG_TAG, sound.toString());
            mBuilder.setSound(sound);
        } else {
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            Uri uri = android.provider.Settings.System.DEFAULT_NOTIFICATION_URI;

            if(sharedPrefs.contains("notificationmessage") && !sharedPrefs.getString("notificationmessage", "").equalsIgnoreCase("")){
                uri = Uri.parse(sharedPrefs.getString("notificationmessage", ""));
            }

            mBuilder.setSound(uri);
        }
    }

    private void setNotificationLedColor(Bundle extras, Notification.Builder mBuilder) {
        String ledColor = getString(extras, LED_COLOR);
        if (ledColor != null) {
            // Converts parse Int Array from ledColor
            String[] items = ledColor.replaceAll("\\[", "").replaceAll("\\]", "").split(",");
            int[] results = new int[items.length];
            for (int i = 0; i < items.length; i++) {
                try {
                    results[i] = Integer.parseInt(items[i]);
                } catch (NumberFormatException nfe) {
                }
            }
            if (results.length == 4) {
                mBuilder.setLights(Color.argb(results[0], results[1], results[2], results[3]), 500, 500);
            } else {
                Log.e(LOG_TAG, "ledColor parameter must be an array of length == 4 (ARGB)");
            }
        }
    }

    private void setNotificationPriority(Bundle extras, Notification.Builder mBuilder) {
        String priorityStr = getString(extras, PRIORITY);
        if (priorityStr != null) {
            try {
                Integer priority = Integer.parseInt(priorityStr);
                if (priority >= Notification.PRIORITY_MIN && priority <= Notification.PRIORITY_MAX) {
                    mBuilder.setPriority(priority);
                } else {
                    Log.e(LOG_TAG, "Priority parameter must be between -2 and 2");
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
    }

    private void setNotificationLargeIcon(Bundle extras, String packageName, Resources resources, Notification.Builder mBuilder) {
        String gcmLargeIcon = getString(extras, IMAGE); // from gcm
        if (gcmLargeIcon != null) {
            if (gcmLargeIcon.startsWith("http://") || gcmLargeIcon.startsWith("https://")) {
                mBuilder.setLargeIcon(getBitmapFromURL(gcmLargeIcon));
                Log.d(LOG_TAG, "using remote large-icon from gcm");
            } else {
                AssetManager assetManager = CovidApp.getInstance().getAssets();
                InputStream istr;
                try {
                    istr = assetManager.open(gcmLargeIcon);
                    Bitmap bitmap = BitmapFactory.decodeStream(istr);
                    mBuilder.setLargeIcon(bitmap);
                    Log.d(LOG_TAG, "using assets large-icon from gcm");
                } catch (IOException e) {
                    int largeIconId = 0;
                    largeIconId = resources.getIdentifier(gcmLargeIcon, DRAWABLE, packageName);
                    if (largeIconId != 0) {
                        Bitmap largeIconBitmap = BitmapFactory.decodeResource(resources, largeIconId);
                        mBuilder.setLargeIcon(largeIconBitmap);
                        Log.d(LOG_TAG, "using resources large-icon from gcm");
                    } else {
                        Log.d(LOG_TAG, "Not setting large icon");
                    }
                }
            }
        }
    }

    private void setNotificationSmallIcon(Context context, Bundle extras, String packageName, Resources resources, Notification.Builder mBuilder, String localIcon) {
        /*int iconId = 0;
        String icon = getString(extras, ICON);
        if (icon != null) {
            iconId = resources.getIdentifier(icon, DRAWABLE, packageName);
            Log.d(LOG_TAG, "using icon from plugin options");
        } else if (localIcon != null) {
            iconId = resources.getIdentifier(localIcon, DRAWABLE, packageName);
            Log.d(LOG_TAG, "using icon from plugin options");
        }
        if (iconId == 0) {
            Log.d(LOG_TAG, "no icon resource found - using application icon");
            iconId = context.getApplicationInfo().icon;
        }*/
        mBuilder.setSmallIcon(R.drawable.app_logo);
    }

    private void setNotificationIconColor(String color, Notification.Builder mBuilder, String localIconColor) {
        int iconColor = 0;
        if (color != null) {
            try {
                iconColor = Color.parseColor(color);
            } catch (IllegalArgumentException e) {
                Log.e(LOG_TAG, "couldn't parse color from android options");
            }
        } else if (localIconColor != null) {
            try {
                iconColor = Color.parseColor(localIconColor);
            } catch (IllegalArgumentException e) {
                Log.e(LOG_TAG, "couldn't parse color from android options");
            }
        }
        if (iconColor != 0) {
            mBuilder.setColor(iconColor);
        }
    }

    public Bitmap getBitmapFromURL(String strURL) {
        try {
            URL url = new URL(strURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getAppName(Context context) {
        CharSequence appName = context.getPackageManager().getApplicationLabel(context.getApplicationInfo());
        return (String) appName;
    }


    private int parseInt(String value, Bundle extras) {
        int retval = 0;

        try {
            retval = Integer.parseInt(getString(extras, value));
        } catch (NumberFormatException e) {
            Log.e(LOG_TAG, "Number format exception - Error parsing " + value + ": " + e.getMessage());
        } catch (Exception e) {
            Log.e(LOG_TAG, "Number format exception - Error parsing " + value + ": " + e.getMessage());
        }

        return retval;
    }

    public void resetNotifications() {
        messageMap = new HashMap<Integer, ArrayList<Pair<String, String>>>();
        users = new HashMap<>();
    }

    public void removeNotification(String id) {
        for (Integer key: messageMap.keySet()){
            ArrayList<Pair<String, String>> messages = messageMap.get(key);

            boolean find = false;

            for (Pair<String, String> m : messages){
                if(m.first.equalsIgnoreCase(id)){
                    messages.remove(m);
                    if (messages.size() == 0){
                        messageMap.remove(key);
                    }
                    find = true;
                    break;
                }
            }

            if (find){
                break;
            }
        }
    }
}
