package com.goblob.covid.geolocation;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.text.Html;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.goblob.covid.BuildConfig;
import com.goblob.covid.R;
import com.goblob.covid.alarms.AlarmStatus;
import com.goblob.covid.app.CovidApp;
import com.goblob.covid.data.dao.model.Bluetooth;
import com.goblob.covid.ui.Main2Activity;
import com.goblob.covid.utils.Systems;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import de.greenrobot.event.EventBus;

public class GpsLoggingService extends Service  {
    private static final String TAG = GpsLoggingService.class.getSimpleName();
    private static NotificationManager notificationManager;
    private static int NOTIFICATION_ID = 8675309;
    private static int NOTIFICATION_HOW_ID = 86753091;
    private final IBinder binder = new GpsLoggingBinder();
    AlarmManager nextPointAlarmManager;
    private NotificationCompat.Builder nfc;

    private static final Logger LOG = LoggerFactory.getLogger(GpsLoggingService.class);

    // ---------------------------------------------------
    // Helpers and managers
    // ---------------------------------------------------
    private PreferenceHelper preferenceHelper = PreferenceHelper.getInstance();
    private GoblobLocationManager session = GoblobLocationManager.getInstance();
    protected LocationManager gpsLocationManager;
    private LocationManager passiveLocationManager;
    private LocationManager towerLocationManager;
    private GeneralLocationListener gpsLocationListener;
    private GeneralLocationListener towerLocationListener;
    private GeneralLocationListener passiveLocationListener;
    private Intent alarmIntent;
    private Handler handler = new Handler();

    PendingIntent activityRecognitionPendingIntent;
    private boolean stopByStill;
    // ---------------------------------------------------
    private BluetoothAdapter bluetoothAdapter;
    private Intent alarmDiscoveryIntent;
    private Intent alarmVisibilityIntent;
    private boolean singlePointMode;
    private Intent alarmStatusIntent;
    private NotificationCompat.Builder nfcStatus;
    private long howAreYouTime;

    public static void startService(Context context) {
        Intent serviceIntent = new Intent(CovidApp.getInstance(), GpsLoggingService.class);
        if (GoblobLocationManager.getInstance().isHowAreYou() || !GoblobLocationManager.getInstance().isHowAreYouToday()){
            serviceIntent.putExtra(IntentConstants.HOW_ARE_YOU, true);
        }
        if (GoblobLocationManager.getInstance().isStarted()) {
            serviceIntent.putExtra(IntentConstants.IMMEDIATE_START, true);
        }
        context.startService(serviceIntent);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return binder;
    }

    @Override
    public void onCreate() {
        Log.e(TAG, "***********onCreate************");

        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "my_channel_01";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("")
                    .setContentText("").build();

            if (GoblobLocationManager.getInstance().isStarted()) {
                try {
                    startForeground(NOTIFICATION_ID, getNotification());
                } catch (Exception ex) {
                    LOG.error("Could not start GPSLoggingService in foreground. ", ex);
                }
            } else if (GoblobLocationManager.getInstance().isHowAreYou() || !GoblobLocationManager.getInstance().isHowAreYouToday()){
                try {
                    startForeground(NOTIFICATION_HOW_ID, getNotificationHow());
                } catch (Exception ex) {
                    LOG.error("Could not start GPSLoggingService in foreground. ", ex);
                }
            } else {
                startForeground(NOTIFICATION_HOW_ID, notification);
            }
        }

        nextPointAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        bluetoothAdapter.setName(CovidApp.getInstance().getAndroidDeviceId());

        if (!bluetoothAdapter.isEnabled()){
            bluetoothAdapter.enable();
        }

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);

        registerEventBus();

        registerStatusAlarm();
    }


    /** Starts looking for other players using Nearby Connections. */
    private void startDiscovery() {
        if (session.getCurrentLocationInfo() == null) {
            logOnce();
        }
        if (!bluetoothAdapter.isEnabled()){
            bluetoothAdapter.enable();
        }
        Log.d(TAG, "bluetoothAdapter---startDiscovery");
        if (bluetoothAdapter != null && !bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.startDiscovery();
        }
        startAlarmForNextDiscovery();
    }

    @TargetApi(23)
    private void startAlarmForNextDiscovery() {
        LOG.debug("bluetoothAdapter---startAlarmForNextDiscovery");

        long triggerTime = System.currentTimeMillis() + (long) (5 * 60 * 1000);

        alarmDiscoveryIntent = new Intent(this, AlarmDiscovery.class);
        cancelAlarmDiscovery();

        PendingIntent sender = PendingIntent.getBroadcast(this, 0, alarmDiscoveryIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (Systems.isDozing(this)) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, sender);
        } else {
            am.set(AlarmManager.RTC_WAKEUP, triggerTime, sender);
        }
        LOG.debug("Autosend alarm has been set");
    }

    @TargetApi(23)
    private void startAlarmForNextVisibility() {
        LOG.debug("bluetoothAdapter---startAlarmForNextVisibility");

        long triggerTime = System.currentTimeMillis() + (long) (10 * 60 * 1000);

        alarmVisibilityIntent = new Intent(this, AlarmVisibility.class);
        cancelAlarmVisibility();

        PendingIntent sender = PendingIntent.getBroadcast(this, 0, alarmVisibilityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (Systems.isDozing(this)) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, sender);
        } else {
            am.set(AlarmManager.RTC_WAKEUP, triggerTime, sender);
        }
        LOG.debug("Autosend alarm has been set");
    }

    private void startVisibility(){
        Log.d(TAG, "bluetoothAdapter---startVisibility");
         /*Intent discoverableIntent =
                new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);*/
        try {
            Method method = bluetoothAdapter.getClass().getMethod("setScanMode", int.class, int.class);
            method.invoke(bluetoothAdapter, BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE, 300);
            Log.e(TAG, "method invoke successfully");
        } catch (Exception e) {
            e.printStackTrace();
        }
        startAlarmForNextVisibility();
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive");
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                if(deviceName != null) {
                    Log.i(TAG, "onEndpointFound: endpoint found, connecting my device: " + CovidApp.getInstance().getAndroidDeviceId() + " with " + deviceName);
                    ParseUser user = ParseUser.getCurrentUser();
                    ParseObject bluetooth = ParseObject.create(Bluetooth.class);
                    bluetooth.put("device1", CovidApp.getInstance().getAndroidDeviceId());
                    bluetooth.put("device2", deviceName);
                    if (deviceHardwareAddress != null) {
                        bluetooth.put("device2Address", deviceHardwareAddress);
                    }
                    if (session.getCurrentLocationInfo() != null) {
                        ParseGeoPoint latlng = new ParseGeoPoint(session.getCurrentLocationInfo().getLatitude(), session.getCurrentLocationInfo().getLongitude());
                        bluetooth.put("location", latlng);
                    }
                    bluetooth.put("user", user);
                    bluetooth.saveInBackground();
                }

                if(deviceHardwareAddress != null) {
                    Log.d(TAG, deviceHardwareAddress);
                }
            }
        }
    };

    private void requestActivityRecognitionUpdates() {

        //if(preferenceHelper.shouldNotLogIfUserIsStill()){
        LOG.debug("Requesting activity recognition updates");
        Intent intent = new Intent(getApplicationContext(), GpsLoggingService.class);
        activityRecognitionPendingIntent = PendingIntent.getService(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        ActivityRecognitionClient arClient = ActivityRecognition.getClient(getApplicationContext());
        arClient.requestActivityUpdates(preferenceHelper.getMinimumLoggingInterval() * 1000, activityRecognitionPendingIntent);
        //}

    }

    private void stopActivityRecognitionUpdates(){
        try{
            if (activityRecognitionPendingIntent != null){
                LOG.debug("Stopping activity recognition updates");
                ActivityRecognitionClient arClient = ActivityRecognition.getClient(getApplicationContext());
                arClient.removeActivityUpdates(activityRecognitionPendingIntent);
            }
        } catch (Exception ex){
            LOG.error("Could not stop activity recognition service", ex);
        }
    }

    private void registerEventBus() {
        EventBus.getDefault().registerSticky(this);
    }

    private void unregisterEventBus(){
        try {
            EventBus.getDefault().unregister(this);
        } catch (Throwable t){
            //this may crash if registration did not go through. just be safe
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        handleIntent(intent);
        if(!session.isStarted() && !session.isHowAreYou()){
            stopForeground(true);
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "***********onDestroy************");
        LOG.warn(SessionLogcatAppender.MARKER_INTERNAL, "GpsLoggingService is being destroyed by Android OS.");
        unregisterEventBus();
        unregisterReceiver(receiver);
        //removeNotification();

        restartService();
        super.onDestroy();
    }

    private void restartService(){
        if(session.isStarted()) {
            long triggerTime = System.currentTimeMillis() + (long) (1000);

            Intent restartService = new Intent(this, RestarterReceiver.class);
            restartService.putExtra("was_running", session.isStarted());

            PendingIntent sender = PendingIntent.getBroadcast(this, 0, restartService, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (Systems.isDozing(this)) {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, sender);
            } else {
                am.set(AlarmManager.RTC_WAKEUP, triggerTime, sender);
            }
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.e(TAG, "***********onTaskRemoved************");
        restartService();
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onLowMemory() {
        LOG.error("Android is low on memory!");
        super.onLowMemory();
    }

    private void handleIntent(Intent intent) {

        ActivityRecognitionResult arr = ActivityRecognitionResult.extractResult(intent);
        if(arr != null){
            EventBus.getDefault().post(new ServiceEvents.ActivityRecognitionEvent(arr));
            return;
        }

        if (!stopByStill && session.getLatestDetectedActivityName().equalsIgnoreCase("STILL")) {
            if (session.getUserStillSinceTimeStamp() > 0 && System.currentTimeMillis() - session.getUserStillSinceTimeStamp() >= 60 * 1000 * 5) {
                stopByStill = true;
                stopGpsManager();
            }
        }

        if (intent != null) {
            Bundle bundle = intent.getExtras();

            if (bundle != null) {


                if(!Systems.locationPermissionsGranted(this)){
                    LOG.error("User has not granted permission to access location services. Will not continue!");
                    stopLogging();
                    return;
                }

                boolean needToStartGpsManager = false;

                if (bundle.getBoolean(IntentConstants.IMMEDIATE_START)) {
                    LOG.info("Intent received - Start Logging Now");
                    EventBus.getDefault().post(new CommandEvents.RequestStartStop(true));
                }

                if (bundle.getBoolean(IntentConstants.CHECK_LOCATION_SETTINGS)) {
                    LOG.info("Intent received - CHECK_LOCATION_SETTINGS");
                    EventBus.getDefault().post(new CommandEvents.CheckLocationSettings(bundle.getString(IntentConstants.ACTION)));
                }

                if (bundle.getBoolean(IntentConstants.IMMEDIATE_STOP)) {
                    LOG.info("Intent received - Stop logging now");
                    EventBus.getDefault().post(new CommandEvents.RequestStartStop(false));
                }

                if (bundle.getBoolean(IntentConstants.GET_NEXT_DISCOVERY)) {
                    LOG.info("Intent received - Stop logging now");
                    startDiscovery();
                    return;
                }

                if (bundle.getBoolean(IntentConstants.HOW_ARE_YOU)) {
                    LOG.info("Intent received - Stop logging now");
                    howAreYou();
                    return;
                }

                if (intent.hasExtra(IntentConstants.GOOD)) {
                    LOG.info("Intent received - Stop logging now");
                    status(bundle.getBoolean(IntentConstants.GOOD));
                    return;
                }

                if (bundle.getBoolean(IntentConstants.GET_NEXT_VISIBILITY)) {
                    LOG.info("Intent received - Stop logging now");
                    startVisibility();
                    return;
                }

                if (bundle.getBoolean(IntentConstants.GET_STATUS)) {
                    LOG.info("Intent received - Sending Status by broadcast");
                    EventBus.getDefault().post(new CommandEvents.GetStatus());
                }

                if (bundle.getBoolean(IntentConstants.AUTOSEND_NOW)) {
                    LOG.info("Intent received - Send Email Now");
                    EventBus.getDefault().post(new CommandEvents.AutoSend(null));
                }

                if (bundle.getBoolean(IntentConstants.GET_NEXT_POINT)) {
                    LOG.info("Intent received - Get Next Point");
                    needToStartGpsManager = true;
                }

                if (bundle.getString(IntentConstants.SET_DESCRIPTION) != null) {
                    LOG.info("Intent received - Set Next Point Description: " + bundle.getString(IntentConstants.SET_DESCRIPTION));
                    EventBus.getDefault().post(new CommandEvents.Annotate(bundle.getString(IntentConstants.SET_DESCRIPTION)));
                }

                if(bundle.getString(IntentConstants.SWITCH_PROFILE) != null){
                    LOG.info("Intent received - switch profile: " + bundle.getString(IntentConstants.SWITCH_PROFILE));
                    EventBus.getDefault().post(new ProfileEvents.SwitchToProfile(bundle.getString(IntentConstants.SWITCH_PROFILE)));
                }

                if (bundle.get(IntentConstants.PREFER_CELLTOWER) != null) {
                    boolean preferCellTower = bundle.getBoolean(IntentConstants.PREFER_CELLTOWER);
                    LOG.debug("Intent received - Set Prefer Cell Tower: " + String.valueOf(preferCellTower));

                    if(preferCellTower){
                        preferenceHelper.setShouldLogNetworkLocations(true);
                        preferenceHelper.setShouldLogSatelliteLocations(false);
                    } else {
                        preferenceHelper.setShouldLogSatelliteLocations(true);
                        preferenceHelper.setShouldLogNetworkLocations(false);
                    }

                    needToStartGpsManager = true;
                }

                if (bundle.get(IntentConstants.TIME_BEFORE_LOGGING) != null) {
                    int timeBeforeLogging = bundle.getInt(IntentConstants.TIME_BEFORE_LOGGING);
                    LOG.debug("Intent received - logging interval: " + String.valueOf(timeBeforeLogging));
                    preferenceHelper.setMinimumLoggingInterval(timeBeforeLogging);
                    needToStartGpsManager = true;
                }

                if (bundle.get(IntentConstants.DISTANCE_BEFORE_LOGGING) != null) {
                    int distanceBeforeLogging = bundle.getInt(IntentConstants.DISTANCE_BEFORE_LOGGING);
                    LOG.debug("Intent received - Set Distance Before Logging: " + String.valueOf(distanceBeforeLogging));
                    preferenceHelper.setMinimumDistanceInMeters(distanceBeforeLogging);
                    needToStartGpsManager = true;
                }

                if (bundle.get(IntentConstants.GPS_ON_BETWEEN_FIX) != null) {
                    boolean keepBetweenFix = bundle.getBoolean(IntentConstants.GPS_ON_BETWEEN_FIX);
                    LOG.debug("Intent received - Set Keep Between Fix: " + String.valueOf(keepBetweenFix));
                    preferenceHelper.setShouldKeepGPSOnBetweenFixes(keepBetweenFix);
                    needToStartGpsManager = true;
                }

                if (bundle.get(IntentConstants.RETRY_TIME) != null) {
                    int retryTime = bundle.getInt(IntentConstants.RETRY_TIME);
                    LOG.debug("Intent received - Set duration to match accuracy: " + String.valueOf(retryTime));
                    preferenceHelper.setLoggingRetryPeriod(retryTime);
                    needToStartGpsManager = true;
                }

                if (bundle.get(IntentConstants.ABSOLUTE_TIMEOUT) != null) {
                    int absoluteTimeout = bundle.getInt(IntentConstants.ABSOLUTE_TIMEOUT);
                    LOG.debug("Intent received - Set absolute timeout: " + String.valueOf(absoluteTimeout));
                    preferenceHelper.setAbsoluteTimeoutForAcquiringPosition(absoluteTimeout);
                    needToStartGpsManager = true;
                }

                if(bundle.get(IntentConstants.LOG_ONCE) != null){
                    boolean logOnceIntent = bundle.getBoolean(IntentConstants.LOG_ONCE);
                    LOG.debug("Intent received - Log Once: " + String.valueOf(logOnceIntent));
                    needToStartGpsManager = false;
                    logOnce();
                }

                try {
                    if(bundle.get(Intent.EXTRA_ALARM_COUNT) != "0"){
                        needToStartGpsManager = true;
                    }
                }
                catch (Throwable t){
                    LOG.warn(SessionLogcatAppender.MARKER_INTERNAL, "Received a weird EXTRA_ALARM_COUNT value. Cannot continue.");
                    needToStartGpsManager = false;
                }


                if (needToStartGpsManager && session.isStarted()) {
                    startGpsManager();
                }
            }
        } else {
            // A null intent is passed in if the service has been killed and restarted.
            LOG.debug("Service restarted with null intent. Were we logging previously - " + session.isStarted());
            if(session.isStarted()){
                startLogging(false);
            }

        }
    }

    private void status(boolean isGood){
        session.setHowAreYou(false);

        stopForeground(true);

        if(session.isStarted()) {
           startForeground(NOTIFICATION_ID, getNotification());
        }

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_HOW_ID);

        EventBus.getDefault().post(new CommandEvents.HowAreYou(false, isGood));

        if (isGood) {
            saveFineStatus();
        } else {
            Intent intent = new Intent();
            intent.setClassName(BuildConfig.APPLICATION_ID, "com.goblob.covid.ui.Main2Activity");
            intent.setPackage(getBaseContext().getPackageName());
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("BAD", true);
            startActivity(intent);
        }
    }


    private void saveFineStatus() {
        // TODO: Ver donde esta almacaneda la lastLocation para leerla aqui y crear al ParseGeoPoint
        ParseObject object = new ParseObject("Symptom");
        ParseUser user = ParseUser.getCurrentUser();
        object.put("user", user);
        if (session.getCurrentLocationInfo() != null) {
            ParseGeoPoint parseGeoPoint = new ParseGeoPoint(session.getCurrentLocationInfo().getLatitude(), session.getCurrentLocationInfo().getLongitude());
            object.put("location", parseGeoPoint);
        }
    }

    private void howAreYou() {
        if(!session.isHowAreYouToday()) {
            session.setHowAreYou(true);
            showNotificationHowAreYou();
            EventBus.getDefault().post(new CommandEvents.HowAreYou(true, true));
        }
        registerStatusAlarm();
    }


    private void registerStatusAlarm() {
        LOG.debug("registerStatusAlarm");

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 7);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (Calendar.getInstance().after(calendar)) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        howAreYouTime = calendar.getTimeInMillis();

        alarmStatusIntent = new Intent(this, AlarmStatus.class);

        PendingIntent sender = PendingIntent.getBroadcast(this, 0, alarmStatusIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (Systems.isDozing(this)) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, howAreYouTime, sender);
        } else {
            am.set(AlarmManager.RTC_WAKEUP, howAreYouTime, sender);
        }
    }

    private void showNotificationHowAreYou(){
        Notification notif = getNotificationHow();

        if(!session.isStarted()) {
            try {
                startForeground(NOTIFICATION_HOW_ID, notif);
            } catch (Exception ex) {
                LOG.error("Could not start GPSLoggingService in foreground. ", ex);
            }
        }

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_HOW_ID, notif);
    }

    /**
     * Shows a notification icon in the status bar for GPS Logger
     */
    private Notification getNotificationHow() {

        Intent stopLoggingIntent = new Intent(this, GpsLoggingService.class);
        stopLoggingIntent.setAction("NotificationButton_GOOD");
        stopLoggingIntent.putExtra(IntentConstants.GOOD, true);
        PendingIntent piStop = PendingIntent.getService(this, 0, stopLoggingIntent, 0);

        Intent badLoggingIntent = new Intent(this, GpsLoggingService.class);
        badLoggingIntent.setAction("NotificationButton_BAD");
        badLoggingIntent.putExtra(IntentConstants.GOOD, false);
        PendingIntent piBad = PendingIntent.getService(this, 1, badLoggingIntent, 0);

        // What happens when the notification item is clicked
        Intent contentIntent = new Intent(this, Main2Activity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(contentIntent);

        PendingIntent pending = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        CharSequence contentTitle = "Good Morning";
        CharSequence contentText = "How are you today?";
        long notificationTime = System.currentTimeMillis();

        if (nfcStatus == null) {

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                NotificationChannel channel = new NotificationChannel("gpslogger", getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT);
                channel.enableLights(false);
                channel.enableVibration(false);
                channel.setSound(null,null);
                channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

                channel.setShowBadge(true);
                manager.createNotificationChannel(channel);

            }

            nfcStatus = new NotificationCompat.Builder(getApplicationContext(),"gpslogger")
                    .setSmallIcon(R.drawable.app_logo)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.app_logo))
                    .setPriority( preferenceHelper.shouldHideNotificationFromStatusBar() ? NotificationCompat.PRIORITY_MIN : NotificationCompat.PRIORITY_LOW)
                    .setCategory(NotificationCompat.CATEGORY_SERVICE)
                    .setVisibility(NotificationCompat.VISIBILITY_SECRET) //This hides the notification from lock screen
                    .setContentTitle(contentTitle)
                    .setContentText(contentText)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText).setBigContentTitle(contentTitle))
                    .setOngoing(true)
                    .setContentIntent(pending);

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                nfcStatus.setPriority(NotificationCompat.PRIORITY_LOW);
            }

            if(!preferenceHelper.shouldHideNotificationButtons()){
                nfcStatus.addAction(android.R.drawable.ic_menu_close_clear_cancel, getString(R.string.good), piStop)
                        .addAction(android.R.drawable.ic_menu_close_clear_cancel, getString(R.string.bad), piBad);
            }
        }

        nfcStatus.setContentTitle(contentTitle);
        nfcStatus.setContentText(contentText);
        nfcStatus.setStyle(new NotificationCompat.BigTextStyle().bigText(contentText).setBigContentTitle(contentTitle));
        nfcStatus.setWhen(notificationTime);

        //notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //notificationManager.notify(NOTIFICATION_ID, nfcStatus.build());
        return nfcStatus.build();
    }

    /**
     * Sets up the auto email timers based on user preferences.
     */
    @TargetApi(23)
    public void setupAutoSendTimers() {
        LOG.debug("Setting up autosend timers. Auto Send Enabled - " + String.valueOf(preferenceHelper.isAutoSendEnabled())
                + ", Auto Send Delay - " + String.valueOf(session.getAutoSendDelay()));

        if (preferenceHelper.isAutoSendEnabled() && session.getAutoSendDelay() > 0) {
            long triggerTime = System.currentTimeMillis() + (long) (session.getAutoSendDelay() * 60 * 1000);

            alarmIntent = new Intent(this, AlarmReceiver.class);
            cancelAlarm();

            PendingIntent sender = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
            if(Systems.isDozing(this)) {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, sender);
            }
            else {
                am.set(AlarmManager.RTC_WAKEUP, triggerTime, sender);
            }
            LOG.debug("Autosend alarm has been set");

        } else {
            if (alarmIntent != null) {
                LOG.debug("alarmIntent was null, canceling alarm");
                cancelAlarm();
            }
        }
    }


    public void logOnce() {
        session.setSinglePointMode(true);

        if (session.isStarted()) {
            startGpsManager();
        } else {
            startLogging(true);
        }
    }

    private void cancelAlarmDiscovery() {
        if (alarmDiscoveryIntent != null) {
            AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
            PendingIntent sender = PendingIntent.getBroadcast(this, 0, alarmDiscoveryIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            am.cancel(sender);
        }
    }

    private void cancelAlarmVisibility() {
        if (alarmVisibilityIntent != null) {
            AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
            PendingIntent sender = PendingIntent.getBroadcast(this, 0, alarmVisibilityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            am.cancel(sender);
        }
    }

    private void cancelAlarm() {
        if (alarmIntent != null) {
            AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
            PendingIntent sender = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            am.cancel(sender);
        }
    }

    /**
     * Method to be called if user has chosen to auto email log files when he
     * stops logging
     */
    private void autoSendLogFileOnStop() {
        if (preferenceHelper.isAutoSendEnabled() && preferenceHelper.shouldAutoSendOnStopLogging()) {
            autoSendLogFile(null);
        }
    }

    /**
     * Calls the Auto Senders which process the files and send it.
     */
    private void autoSendLogFile(@Nullable String formattedFileName) {

        LOG.debug("Filename: " + formattedFileName);

        if ( !Strings.isNullOrEmpty(formattedFileName) || !Strings.isNullOrEmpty(Strings.getFormattedFileName()) ) {
            String fileToSend = Strings.isNullOrEmpty(formattedFileName) ? Strings.getFormattedFileName() : formattedFileName;
            FileSenderFactory.autoSendFiles(fileToSend);
            setupAutoSendTimers();
        }
    }

    private void resetAutoSendTimersIfNecessary() {

        if (session.getAutoSendDelay() != preferenceHelper.getAutoSendInterval()) {
            session.setAutoSendDelay(preferenceHelper.getAutoSendInterval());
            setupAutoSendTimers();
        }
    }

    /**
     * Resets the form, resets file name if required, reobtains preferences
     */
    protected void startLogging(boolean singlePointMode) {
        LOG.debug(".");

        this.singlePointMode = singlePointMode;

        session.setAddNewTrackSegment(true);

        session.setUserStillSinceTimeStamp(0);

        try {
            startForeground(NOTIFICATION_ID, getNotification());
        } catch (Exception ex) {
            LOG.error("Could not start GPSLoggingService in foreground. ", ex);
        }

        session.setStarted(true);

        resetAutoSendTimersIfNecessary();
        showNotification();
        setupAutoSendTimers();
        resetCurrentFileName(true);
        notifyClientsStarted(true);
        startPassiveManager();
        startGpsManager();
        requestActivityRecognitionUpdates();
        startDiscovery();
        startVisibility();
    }

    private void notifyByBroadcast(boolean loggingStarted) {
        LOG.debug("Sending a custom broadcast");
        String event = (loggingStarted) ? "started" : "stopped";
        Intent sendIntent = new Intent();
        sendIntent.setAction("com.mendhak.gpslogger.EVENT");
        sendIntent.putExtra("gpsloggerevent", event);
        sendIntent.putExtra("filename", session.getCurrentFormattedFileName());
        sendIntent.putExtra("startedtimestamp", session.getStartTimeStamp());
        sendBroadcast(sendIntent);
    }

    /**
     * Informs main activity and broadcast listeners whether logging has started/stopped
     */
    private void notifyClientsStarted(boolean started) {
        LOG.info((started)? getString(R.string.started) : getString(R.string.stopped));
        notifyByBroadcast(started);
        EventBus.getDefault().post(new ServiceEvents.LoggingStatus(started));
    }

    /**
     * Notify status of logger
     */
    private void notifyStatus(boolean started) {
        LOG.info((started)? getString(R.string.started) : getString(R.string.stopped));
        notifyByBroadcast(started);
    }

    /**
     * Stops logging, removes notification, stops GPS manager, stops email timer
     */
    public void stopLogging() {
        LOG.debug(".");
        singlePointMode = false;
        session.setAddNewTrackSegment(true);
        session.setTotalTravelled(0);
        session.setPreviousLocationInfo(null);
        session.setStarted(false);
        session.setUserStillSinceTimeStamp(0);
        session.setLatestTimeStamp(0);
        stopAbsoluteTimer();
        // Email log file before setting location info to null
        autoSendLogFileOnStop();
        cancelAlarm();
        session.setCurrentLocationInfo(null);
        session.setSinglePointMode(false);

        cancelAlarmDiscovery();

        cancelAlarmVisibility();

        if (bluetoothAdapter != null && !bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }

        removeNotification();
        stopAlarm();
        stopGpsManager();
        stopPassiveManager();
        stopActivityRecognitionUpdates();
        notifyClientsStarted(false);
        session.setCurrentFileName("");
        session.setCurrentFormattedFileName("");
    }

    /**
     * Hides the notification icon in the status bar if it's visible.
     */
    private void removeNotification() {
        stopForeground(true);

        if(session.isHowAreYou()) {
            startForeground(NOTIFICATION_HOW_ID, getNotificationHow());
        }
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    /**
     * Shows a notification icon in the status bar for GPS Logger
     */
    private Notification getNotification() {

        Intent stopLoggingIntent = new Intent(this, GpsLoggingService.class);
        stopLoggingIntent.setAction("NotificationButton_STOP");
        stopLoggingIntent.putExtra(IntentConstants.IMMEDIATE_STOP, true);
        PendingIntent piStop = PendingIntent.getService(this, 0, stopLoggingIntent, 0);

        Intent annotateIntent = new Intent(this, NotificationAnnotationActivity.class);
        annotateIntent.setAction("com.mendhak.gpslogger.NOTIFICATION_BUTTON");
        PendingIntent piAnnotate = PendingIntent.getActivity(this,0, annotateIntent,0);

        // What happens when the notification item is clicked
        Intent contentIntent = new Intent(this, Main2Activity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(contentIntent);

        PendingIntent pending = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        CharSequence contentTitle = getString(R.string.gpslogger_still_running);
        CharSequence contentText = getString(R.string.app_name);
        long notificationTime = System.currentTimeMillis();

        if (session.hasValidLocation()) {
            contentTitle = Strings.getFormattedLatitude(session.getCurrentLatitude()) + ", "
                    + Strings.getFormattedLongitude(session.getCurrentLongitude());

            contentText = Html.fromHtml("<b>" + getString(R.string.txt_altitude) + "</b> " + Strings.getDistanceDisplay(this,session.getCurrentLocationInfo().getAltitude(), preferenceHelper.shouldDisplayImperialUnits(), false)
                    + "  "
                    + "<b>" + getString(R.string.txt_travel_duration) + "</b> "  + Strings.getDescriptiveDurationString((int) (System.currentTimeMillis() - session.getStartTimeStamp()) / 1000, this)
                    + "  "
                    + "<b>" + getString(R.string.txt_accuracy) + "</b> "  + Strings.getDistanceDisplay(this, session.getCurrentLocationInfo().getAccuracy(), preferenceHelper.shouldDisplayImperialUnits(), true));

            notificationTime = session.getCurrentLocationInfo().getTime();
        }

        if (nfc == null) {

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                NotificationChannel channel = new NotificationChannel("gpslogger", getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT);
                channel.enableLights(false);
                channel.enableVibration(false);
                channel.setSound(null,null);
                channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

                channel.setShowBadge(true);
                manager.createNotificationChannel(channel);

            }

            nfc = new NotificationCompat.Builder(getApplicationContext(),"gpslogger")
                    .setSmallIcon(R.drawable.app_logo)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.app_logo))
                    .setPriority( preferenceHelper.shouldHideNotificationFromStatusBar() ? NotificationCompat.PRIORITY_MIN : NotificationCompat.PRIORITY_LOW)
                    .setCategory(NotificationCompat.CATEGORY_SERVICE)
                    .setVisibility(NotificationCompat.VISIBILITY_SECRET) //This hides the notification from lock screen
                    .setContentTitle(contentTitle)
                    .setContentText(contentText)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText).setBigContentTitle(contentTitle))
                    .setOngoing(true)
                    .setContentIntent(pending);

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                nfc.setPriority(NotificationCompat.PRIORITY_LOW);
            }

            if(!preferenceHelper.shouldHideNotificationButtons()){
                nfc.addAction(R.drawable.annotate2, getString(R.string.menu_annotate), piAnnotate)
                        .addAction(android.R.drawable.ic_menu_close_clear_cancel, getString(R.string.shortcut_stop), piStop);
            }
        }



        nfc.setContentTitle(contentTitle);
        nfc.setContentText(contentText);
        nfc.setStyle(new NotificationCompat.BigTextStyle().bigText(contentText).setBigContentTitle(contentTitle));
        nfc.setWhen(notificationTime);

        //notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //notificationManager.notify(NOTIFICATION_ID, nfc.build());
        return nfc.build();
    }

    private void showNotification(){
        Notification notif = getNotification();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notif);
    }

    @SuppressWarnings("ResourceType")
    private void startPassiveManager() {
        if(preferenceHelper.shouldLogPassiveLocations()){
            LOG.debug("Starting passive location listener");
            if(passiveLocationListener== null){
                passiveLocationListener = new GeneralLocationListener(this, BundleConstants.PASSIVE);
            }
            passiveLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            passiveLocationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 1000, 0, passiveLocationListener);
        }
    }

    /**
     * Starts the location manager. There are two location managers - GPS and
     * Cell Tower. This code determines which manager to request updates from
     * based on user preference and whichever is enabled. If GPS is enabled on
     * the phone, that is used. But if the user has also specified that they
     * prefer cell towers, then cell towers are used. If neither is enabled,
     * then nothing is requested.
     */
    @SuppressWarnings("ResourceType")
    private void startGpsManager() {

        //If the user has been still for more than the minimum seconds
        if(userHasBeenStillForTooLong()) {
            LOG.info("No movement detected in the past interval, will not log");
            setAlarmForNextPoint();
            return;
        }

        if (gpsLocationListener == null) {
            gpsLocationListener = new GeneralLocationListener(this, "GPS");
        }

        if (towerLocationListener == null) {
            towerLocationListener = new GeneralLocationListener(this, "CELL");
        }

        gpsLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        towerLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        checkTowerAndGpsStatus();

        if (session.isGpsEnabled() && preferenceHelper.shouldLogSatelliteLocations()) {
            LOG.info("Requesting GPS location updates");
            // gps satellite based
            gpsLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, gpsLocationListener);
            gpsLocationManager.addGpsStatusListener(gpsLocationListener);
            gpsLocationManager.addNmeaListener(gpsLocationListener);

            session.setUsingGps(true);
            startAbsoluteTimer();
        }

        if (session.isTowerEnabled() &&  ( preferenceHelper.shouldLogNetworkLocations() || !session.isGpsEnabled() ) ) {
            LOG.info("Requesting cell and wifi location updates");
            session.setUsingGps(false);
            // Cell tower and wifi based
            towerLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, towerLocationListener);

            startAbsoluteTimer();
        }

        if(!session.isTowerEnabled() && !session.isGpsEnabled()) {
            LOG.error("No provider available!");
            session.setUsingGps(false);
            LOG.error(getString(R.string.gpsprovider_unavailable));
            stopLogging();
            setLocationServiceUnavailable();
            return;
        }

        if(!preferenceHelper.shouldLogNetworkLocations() && !preferenceHelper.shouldLogSatelliteLocations() && !preferenceHelper.shouldLogPassiveLocations()){
            LOG.error("No location provider selected!");
            session.setUsingGps(false);
            stopLogging();
            return;
        }

        EventBus.getDefault().post(new ServiceEvents.WaitingForLocation(true));
        session.setWaitingForLocation(true);

        stopByStill = false;
    }

    private boolean userHasBeenStillForTooLong() {
        return !session.hasDescription() && !session.isSinglePointMode() &&
                (session.getUserStillSinceTimeStamp() > 0 && (System.currentTimeMillis() - session.getUserStillSinceTimeStamp()) > (preferenceHelper.getMinimumLoggingInterval() * 1000));
    }

    private void startAbsoluteTimer() {
        if (preferenceHelper.getAbsoluteTimeoutForAcquiringPosition() >= 1) {
            handler.postDelayed(stopManagerRunnable, preferenceHelper.getAbsoluteTimeoutForAcquiringPosition() * 1000);
        }
    }

    private Runnable stopManagerRunnable = new Runnable() {
        @Override
        public void run() {
            LOG.warn("Absolute timeout reached, giving up on this point");
            stopManagerAndResetAlarm();
        }
    };

    private void stopAbsoluteTimer() {
        handler.removeCallbacks(stopManagerRunnable);
    }

    /**
     * This method is called periodically to determine whether the cell tower /
     * gps providers have been enabled, and sets class level variables to those
     * values.
     */
    private void checkTowerAndGpsStatus() {
        session.setTowerEnabled(towerLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
        session.setGpsEnabled(gpsLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
    }

    /**
     * Stops the location managers
     */
    @SuppressWarnings("ResourceType")
    private void stopGpsManager() {

        if (towerLocationListener != null) {
            LOG.debug("Removing towerLocationManager updates");
            towerLocationManager.removeUpdates(towerLocationListener);
        }

        if (gpsLocationListener != null) {
            LOG.debug("Removing gpsLocationManager updates");
            gpsLocationManager.removeUpdates(gpsLocationListener);
            gpsLocationManager.removeGpsStatusListener(gpsLocationListener);
        }

        session.setWaitingForLocation(false);
        EventBus.getDefault().post(new ServiceEvents.WaitingForLocation(false));

    }

    @SuppressWarnings("ResourceType")
    private void stopPassiveManager(){
        if(passiveLocationManager!=null){
            LOG.debug("Removing passiveLocationManager updates");
            passiveLocationManager.removeUpdates(passiveLocationListener);
        }
    }

    /**
     * Sets the current file name based on user preference.
     */
    private void resetCurrentFileName(boolean newLogEachStart) {

        String oldFileName = session.getCurrentFormattedFileName();

        /* Update the file name, if required. (New day, Re-start service) */
        if (preferenceHelper.shouldCreateCustomFile()) {
            if(Strings.isNullOrEmpty(Strings.getFormattedFileName())){
                session.setCurrentFileName(preferenceHelper.getCustomFileName());
            }

            LOG.debug("Should change file name dynamically: " + preferenceHelper.shouldChangeFileNameDynamically());

            if(!preferenceHelper.shouldChangeFileNameDynamically()){
                session.setCurrentFileName(Strings.getFormattedFileName());
            }

        } else if (preferenceHelper.shouldCreateNewFileOnceAMonth()) {
            // 201001.gpx
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
            session.setCurrentFileName(sdf.format(new Date()));
        } else if (preferenceHelper.shouldCreateNewFileOnceADay()) {
            // 20100114.gpx
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            session.setCurrentFileName(sdf.format(new Date()));
        } else if (newLogEachStart) {
            // 20100114183329.gpx
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            session.setCurrentFileName(sdf.format(new Date()));
        }

        if(!Strings.isNullOrEmpty(oldFileName)
                && !oldFileName.equalsIgnoreCase(Strings.getFormattedFileName())
                && session.isStarted()){
            LOG.debug("New file name, should auto upload the old one");
            EventBus.getDefault().post(new CommandEvents.AutoSend(oldFileName));
        }

        session.setCurrentFormattedFileName(Strings.getFormattedFileName());

        LOG.info("Filename: " + Strings.getFormattedFileName());
        EventBus.getDefault().post(new ServiceEvents.FileNamed(Strings.getFormattedFileName()));

    }



    void setLocationServiceUnavailable(){
        EventBus.getDefault().post(new ServiceEvents.LocationServicesUnavailable());
    }

    /**
     * Stops location manager, then starts it.
     */
    void restartGpsManagers() {
        LOG.debug("Restarting location managers");
        stopGpsManager();
        startGpsManager();
    }

    /**
     * This event is raised when the GeneralLocationListener has a new location.
     * This method in turn updates notification, writes to file, reobtains
     * preferences, notifies main service client and resets location managers.
     *
     * @param loc Location object
     */
    void onLocationChanged(Location loc) {
        Log.d(TAG, loc.toString());
        if (!session.isStarted()) {
            LOG.debug("onLocationChanged called, but session.isStarted is false");
            stopLogging();
            return;
        }

        if (loc == null ||
                Double.isNaN(loc.getLatitude()) ||
                Double.isNaN(loc.getLongitude())) {
            return;
        }

        boolean isPassiveLocation = loc.getExtras().getBoolean(BundleConstants.PASSIVE);
        long currentTimeStamp = System.currentTimeMillis();

        LOG.debug("Has description? " + session.hasDescription() + ", Single point? " + session.isSinglePointMode() + ", Last timestamp: " + session.getLatestTimeStamp());

        // Don't log a point until the user-defined time has elapsed
        // However, if user has set an annotation, just log the point, disregard time and distance filters
        // However, if it's a passive location, disregard the time filter

        /*if (!isPassiveLocation && !session.hasDescription() && !session.isSinglePointMode() && (currentTimeStamp - session.getLatestTimeStamp()) < (preferenceHelper.getMinimumLoggingInterval() * 1000)) {
            return;
        }*/

        //Don't log a point if user has been still
        // However, if user has set an annotation, just log the point, disregard time and distance filters
        if(userHasBeenStillForTooLong()) {
            LOG.info("Received location but the user hasn't moved, ignoring");
            return;
        }

        if(!isPassiveLocation && !isFromValidListener(loc)){
            return;
        }

        //Check if a ridiculous distance has been travelled since previous point - could be a bad GPS jump
        /*if(session.getCurrentLocationInfo() != null){
            double distanceTravelled = Maths.calculateDistance(loc.getLatitude(), loc.getLongitude(), session.getCurrentLocationInfo().getLatitude(), session.getCurrentLocationInfo().getLongitude());
            long timeDifference = (int)Math.abs(loc.getTime() - session.getCurrentLocationInfo().getTime())/1000;

            if( timeDifference > 0 && (distanceTravelled/timeDifference) > 357){ //357 m/s ~=  1285 km/h
                LOG.warn(String.format("Very large jump detected - %d meters in %d sec - discarding point", (long)distanceTravelled, timeDifference));
                return;
            }
        }*/

        double speed = -1;
        double distanceTravelled = -1;
        if (session.getCurrentLocationInfo() != null) {
            distanceTravelled = Maths.calculateDistance(loc.getLatitude(), loc.getLongitude(), session.getCurrentLocationInfo().getLatitude(), session.getCurrentLocationInfo().getLongitude());
            long timeDifference = Math.abs(loc.getTime() - session.getCurrentLocationInfo().getTime()) / 1000;

            if (timeDifference != 0) {
                speed = distanceTravelled / timeDifference;
            }

            if (speed > 357) { //357 m/s ~=  1285 km/h
                LOG.warn(String.format("Very large jump detected - %d meters in %d sec - discarding point", (long) distanceTravelled, timeDifference));
                return;
            }

            if (distanceTravelled != -1 && distanceTravelled < 10) {
                if (loc.getAccuracy() >= session.getCurrentLocationInfo().getAccuracy()) {
                    return;
                }
            }

            if (timeDifference < 1) {
                return;
            }

            loc.setSpeed((float) speed);
        }

        // Don't do anything until the user-defined accuracy is reached
        // even for annotations
        if (preferenceHelper.getMinimumAccuracy() > 0) {

            if(!loc.hasAccuracy() || loc.getAccuracy() == 0){
                return;
            }

            if (preferenceHelper.getMinimumAccuracy() < Math.abs(loc.getAccuracy())) {

                if(session.getFirstRetryTimeStamp() == 0){
                    session.setFirstRetryTimeStamp(System.currentTimeMillis());
                }

                if (currentTimeStamp - session.getFirstRetryTimeStamp() <= preferenceHelper.getLoggingRetryPeriod() * 1000) {
                    LOG.warn("Only accuracy of " + String.valueOf(loc.getAccuracy()) + " m. Point discarded." + getString(R.string.inaccurate_point_discarded));
                    //return and keep trying
                    return;
                }

                if (currentTimeStamp - session.getFirstRetryTimeStamp() > preferenceHelper.getLoggingRetryPeriod() * 1000) {
                    LOG.warn("Only accuracy of " + String.valueOf(loc.getAccuracy()) + " m and timeout reached." + getString(R.string.inaccurate_point_discarded));
                    //Give up for now
                    stopManagerAndResetAlarm();

                    //reset timestamp for next time.
                    session.setFirstRetryTimeStamp(0);
                    return;
                }

                //Success, reset timestamp for next time.
                session.setFirstRetryTimeStamp(0);
            }
        }

        //Don't do anything until the user-defined distance has been traversed
        // However, if user has set an annotation, just log the point, disregard time and distance filters
        // However, if it's a passive location, ignore distance filter.
        if (!isPassiveLocation && !session.hasDescription() && !session.isSinglePointMode() && preferenceHelper.getMinimumDistanceInterval() > 0 && session.hasValidLocation()) {

            double distanceTraveled = Maths.calculateDistance(loc.getLatitude(), loc.getLongitude(),
                    session.getCurrentLatitude(), session.getCurrentLongitude());

            if (preferenceHelper.getMinimumDistanceInterval() > distanceTraveled) {
                LOG.warn(String.format(getString(R.string.not_enough_distance_traveled), String.valueOf(Math.floor(distanceTraveled))) + ", point discarded");
                stopManagerAndResetAlarm();
                return;
            }
        }


        LOG.info(SessionLogcatAppender.MARKER_LOCATION, String.valueOf(loc.getLatitude()) + "," + String.valueOf(loc.getLongitude()));
        loc = Locations.getLocationWithAdjustedAltitude(loc, preferenceHelper);
        loc = Locations.getLocationAdjustedForGPSWeekRollover(loc);
        resetCurrentFileName(false);
        session.setLatestTimeStamp(System.currentTimeMillis());
        session.setFirstRetryTimeStamp(0);
        session.setCurrentLocationInfo(loc);
        setDistanceTraveled(loc);
        showNotification();

        if(isPassiveLocation){
            LOG.debug("Logging passive location to file");
        }

        writeToFile(loc);
        resetAutoSendTimersIfNecessary();
        stopManagerAndResetAlarm();

        EventBus.getDefault().post(new ServiceEvents.LocationUpdate(loc));

        GoblobLocationManager.getInstance().saveLiveLocation(loc);

        if (session.isSinglePointMode()) {
            LOG.debug("Single point mode - stopping now");
            if (!singlePointMode){
                if (stopByStill) {
                    stopGpsManager();
                }
            } else {
                stopLogging();
            }
            session.setSinglePointMode(false);
        }
        Log.e(TAG, loc.toString());
    }

    private boolean isFromValidListener(Location loc) {

        if(!preferenceHelper.shouldLogSatelliteLocations() && !preferenceHelper.shouldLogNetworkLocations()){
            return true;
        }

        if(!preferenceHelper.shouldLogNetworkLocations()){
            return loc.getProvider().equalsIgnoreCase(LocationManager.GPS_PROVIDER);
        }

        if(!preferenceHelper.shouldLogSatelliteLocations()){
            return !loc.getProvider().equalsIgnoreCase(LocationManager.GPS_PROVIDER);
        }

        return true;
    }

    private void setDistanceTraveled(Location loc) {
        // Distance
        if (session.getPreviousLocationInfo() == null) {
            session.setPreviousLocationInfo(loc);
        }
        // Calculate this location and the previous location location and add to the current running total distance.
        // NOTE: Should be used in conjunction with 'distance required before logging' for more realistic values.
        double distance = Maths.calculateDistance(
                session.getPreviousLatitude(),
                session.getPreviousLongitude(),
                loc.getLatitude(),
                loc.getLongitude());
        session.setPreviousLocationInfo(loc);
        session.setTotalTravelled(session.getTotalTravelled() + distance);
    }

    protected void stopManagerAndResetAlarm() {
        /*if (!preferenceHelper.shouldKeepGPSOnBetweenFixes()) {
            stopGpsManager();
        }*/

        stopAbsoluteTimer();
        setAlarmForNextPoint();
    }


    private void stopAlarm() {
        Intent i = new Intent(this, GpsLoggingService.class);
        i.putExtra(IntentConstants.GET_NEXT_POINT, true);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        nextPointAlarmManager.cancel(pi);
    }

    @TargetApi(23)
    private void setAlarmForNextPoint() {
        LOG.debug("Set alarm for " + preferenceHelper.getMinimumLoggingInterval() + " seconds");

        Intent i = new Intent(this, GpsLoggingService.class);
        i.putExtra(IntentConstants.GET_NEXT_POINT, true);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        nextPointAlarmManager.cancel(pi);

        if(Systems.isDozing(this)){
            //Only invoked once per 15 minutes in doze mode
            LOG.warn("Device is dozing, using infrequent alarm");
            nextPointAlarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + preferenceHelper.getMinimumLoggingInterval() * 1000, pi);
        }
        else {
            nextPointAlarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + preferenceHelper.getMinimumLoggingInterval() * 1000, pi);
        }
    }


    /**
     * Calls file helper to write a given location to a file.
     *
     * @param loc Location object
     */
    private void writeToFile(Location loc) {
        session.setAddNewTrackSegment(false);

        try {
            LOG.debug("Calling file writers");
            FileLoggerFactory.write(getApplicationContext(), loc);

            if (session.hasDescription()) {
                LOG.info("Writing annotation: " + session.getDescription());
                FileLoggerFactory.annotate(getApplicationContext(), session.getDescription(), loc);
            }
        }
        catch(Exception e){
            LOG.error(getString(R.string.could_not_write_to_file), e);
        }

        session.clearDescription();
        EventBus.getDefault().post(new ServiceEvents.AnnotationStatus(true));
    }

    /**
     * Informs the main service client of the number of visible satellites.
     *
     * @param count Number of Satellites
     */
    void setSatelliteInfo(int count) {
        session.setVisibleSatelliteCount(count);
        EventBus.getDefault().post(new ServiceEvents.SatellitesVisible(count));
    }

    public void onNmeaSentence(long timestamp, String nmeaSentence) {

        if (preferenceHelper.shouldLogToNmea()) {
            NmeaFileLogger nmeaLogger = new NmeaFileLogger(Strings.getFormattedFileName());
            nmeaLogger.write(timestamp, nmeaSentence);
        }
    }

    /**
     * Can be used from calling classes as the go-between for methods and
     * properties.
     */
    public class GpsLoggingBinder extends Binder {
        public GpsLoggingService getService() {
            return GpsLoggingService.this;
        }
    }


    @EventBusHook
    public void onEvent(CommandEvents.RequestToggle requestToggle){
        if (session.isStarted()) {
            stopLogging();
        } else {
            startLogging(false);
        }
    }

    @EventBusHook
    public void onEvent(CommandEvents.RequestStartStop startStop){
        if(startStop.start){
            startLogging(false);
        }
        else {
            stopLogging();
        }

        EventBus.getDefault().removeStickyEvent(CommandEvents.RequestStartStop.class);
    }


    @EventBusHook
    public void onEvent(CommandEvents.CheckLocationSettings checkLocationSettings) {
        checkLocationSettings(checkLocationSettings.action);
        EventBus.getDefault().removeStickyEvent(CommandEvents.CheckLocationSettings.class);
    }

    /**
     * Check if the device's location settings are adequate for the app's needs using the
     *
     * @param action
     */
    private void checkLocationSettings(final String action) {
        Log.e(TAG, "checkLocationSettings");
        LocationRequest mLocationRequest = new LocationRequest();

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        Task<LocationSettingsResponse> task = LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());
        task.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                Activity activity = CovidApp.getInstance().getRunningActivity();

                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    // All location settings are satisfied. The client can initialize location
                    // requests here.
                    if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    }
                } catch (ApiException exception) {
                    switch (exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // LocationParameters settings are not satisfied. But could be fixed by showing the
                            // user a dialog.
                            try {
                                // Cast to a resolvable exception.
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                if (action != null && action.equalsIgnoreCase("currentLocation")) {
                                    resolvable.startResolutionForResult(activity, 2001);
                                } else if (action != null && action.equalsIgnoreCase("startAndBindService")) {
                                    resolvable.startResolutionForResult(activity, 2003);
                                } else {
                                    resolvable.startResolutionForResult(activity, 2002);
                                }
                                break;
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            } catch (ClassCastException e) {
                                // Ignore, should be an impossible error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // LocationParameters settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.

                            break;
                    }
                }
            }
        });

    }


    @EventBusHook
    public void onEvent(CommandEvents.GetStatus getStatus){
        CommandEvents.GetStatus statusEvent = EventBus.getDefault().removeStickyEvent(CommandEvents.GetStatus.class);
        if(statusEvent != null){
            notifyStatus(session.isStarted());
        }

    }

    @EventBusHook
    public void onEvent(CommandEvents.AutoSend autoSend){
        autoSendLogFile(autoSend.formattedFileName);

        EventBus.getDefault().removeStickyEvent(CommandEvents.AutoSend.class);
    }

    @EventBusHook
    public void onEvent(CommandEvents.Annotate annotate){
        final String desc = annotate.annotation;
        if (desc.length() == 0) {
            LOG.debug("Clearing annotation");
            session.clearDescription();
        } else {
            LOG.debug("Pending annotation: " + desc);
            session.setDescription(desc);
            EventBus.getDefault().post(new ServiceEvents.AnnotationStatus(false));

            if(session.isStarted()){
                startGpsManager();
            }
            else {
                logOnce();
            }
        }

        EventBus.getDefault().removeStickyEvent(CommandEvents.Annotate.class);
    }

    @EventBusHook
    public void onEvent(CommandEvents.LogOnce logOnce){
        logOnce();
    }

    @EventBusHook
    public void onEvent(ServiceEvents.ActivityRecognitionEvent activityRecognitionEvent){

        session.setLatestDetectedActivity(activityRecognitionEvent.result.getMostProbableActivity());

        /*if(!preferenceHelper.shouldNotLogIfUserIsStill()){
            session.setUserStillSinceTimeStamp(0);
            return;
        }*/

        if(activityRecognitionEvent.result.getMostProbableActivity().getType() == DetectedActivity.STILL){
            LOG.debug(activityRecognitionEvent.result.getMostProbableActivity().toString());
            if(session.getUserStillSinceTimeStamp() == 0){
                LOG.debug("Just entered still state, attempt to log");
                stopByStill = false;
                startGpsManager();
                session.setUserStillSinceTimeStamp(System.currentTimeMillis());
            } else {
                stopByStill = true;
                stopGpsManager();
            }
        }
        else {
            LOG.debug(activityRecognitionEvent.result.getMostProbableActivity().toString());
            //Reset the still-since timestamp
            session.setUserStillSinceTimeStamp(0);
            LOG.debug("Just exited still state, attempt to log");
            stopByStill = false;
            startGpsManager();
        }
    }

    @EventBusHook
    public void onEvent(ProfileEvents.SwitchToProfile switchToProfileEvent){
        try {

            boolean isCurrentProfile = preferenceHelper.getCurrentProfileName().equals(switchToProfileEvent.newProfileName);

            LOG.debug("Switching to profile: " + switchToProfileEvent.newProfileName);

            if(!isCurrentProfile){
                //Save the current settings to a file (overwrite)
                File f = new File(Files.storageFolder(GpsLoggingService.this), preferenceHelper.getCurrentProfileName()+".properties");
                preferenceHelper.savePropertiesFromPreferences(f);
            }


            //Read from a possibly existing file and load those preferences in
            File newProfile = new File(Files.storageFolder(GpsLoggingService.this), switchToProfileEvent.newProfileName+".properties");
            if(newProfile.exists()){
                preferenceHelper.setPreferenceFromPropertiesFile(newProfile);
            }

            //Switch current profile name
            preferenceHelper.setCurrentProfileName(switchToProfileEvent.newProfileName);
            LOG.info("Switched to profile: " + switchToProfileEvent.newProfileName);

        } catch (IOException e) {
            LOG.error("Could not save profile to file", e);
        }
    }

}
