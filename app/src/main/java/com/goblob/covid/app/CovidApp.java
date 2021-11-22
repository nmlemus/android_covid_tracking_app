package com.goblob.covid.app;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.util.ArrayMap;
import android.util.Log;

import com.goblob.covid.R;
import com.goblob.covid.dagger.DaggerDataComponent;
import com.goblob.covid.dagger.DataComponent;
import com.goblob.covid.dagger.DataModule;
import com.goblob.covid.data.dao.model.Bluetooth;
import com.goblob.covid.data.dao.model.CorregimientosPA;
import com.goblob.covid.data.dao.model.DailyReportAll;
import com.goblob.covid.utils.DbHelper;
import com.goblob.covid.utils.GoblobLogsManager;
import com.instacart.library.truetime.TrueTime;
import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.io.IOException;
import java.lang.reflect.Field;

public class CovidApp extends Application {

    String androidDeviceId = null;

    private static CovidApp ourInstance;
    private DataComponent dataComponent;

    public static CovidApp getInstance() {
        return ourInstance;
    }

    public String getAndroidDeviceId() {
        return androidDeviceId;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.ourInstance = this;

        GoblobLogsManager.get().startLogsStore();

        System.setProperty("http.keepAliveDuration", String.valueOf(30 * 60 * 1000));

        DbHelper.init(this);

        // Parse configuration

        Parse.setLogLevel(Parse.LOG_LEVEL_DEBUG);

        Parse.enableLocalDatastore(this);

        ParseObject.registerSubclass(DailyReportAll.class);
        ParseObject.registerSubclass(CorregimientosPA.class);
        ParseObject.registerSubclass(Bluetooth.class);

        // Initialize the access to the Parse server.
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("bVl2QvVjwcwGL5HAHY2YR4KstlW0A6bBdZIK1DDN")
                .clientKey("PVVwWt6gqdswXtvuNsGAE7L4VnmJwp59hJU5eC9G")
                .server("https://covid19.back4app.io")
                .enableLocalDataStore()
                .build()
        );

        ParseInstallation.getCurrentInstallation().saveInBackground();

        ParsePush.subscribeInBackground("MINSA");

        androidDeviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        if (DbHelper.get().isFirstTime(androidDeviceId)) {
            // Save the installation object to Parse
            final ParseInstallation currentInstallation = ParseInstallation.getCurrentInstallation();
            final String board = Build.BOARD;
            final String bootLoader = Build.BOOTLOADER;
            final String brand = Build.BRAND;
            final String cpu = Build.CPU_ABI;
            final String device1 = Build.DEVICE;
            final String display = Build.DISPLAY;
            final String fingerprint = Build.FINGERPRINT;
            final String hardware = Build.HARDWARE;
            final String host = Build.HOST;
            final String buildId = Build.ID;
            final String manufacturer = Build.MANUFACTURER;
            final String model = Build.MODEL;
            final String product = Build.PRODUCT;
            final String serial = Build.SERIAL;
            final String tags = Build.TAGS;
            final long time = Build.TIME;
            final String type = Build.TYPE;
            final int version = Build.VERSION.SDK_INT;
            final String versionRelease = Build.VERSION.RELEASE;

            ParseQuery<ParseObject> query = ParseQuery.getQuery("Device");
            // query.whereEqualTo("installation", currentInstallation);
            query.whereEqualTo("androidDeviceId", androidDeviceId);
            query.getFirstInBackground(new GetCallback<ParseObject>() {
                public void done(ParseObject device, ParseException e) {
                    if (device != null) {
                        currentInstallation.put("device", device);
                    } else {
                        device = new ParseObject("Device");
                        device.put("board", board);
                        device.put("bootLoader", bootLoader);
                        device.put("brand", brand);
                        device.put("cpu", cpu);
                        device.put("device", device1);
                        device.put("display", display);
                        device.put("fingerprint", fingerprint);
                        device.put("hardware", hardware);
                        device.put("host", host);
                        device.put("buildId", buildId);
                        device.put("manufacturer", manufacturer);
                        device.put("model", model);
                        device.put("product", product);
                        device.put("serial", serial);
                        device.put("tags", tags);
                        device.put("time", time);
                        device.put("type", type);
                        device.put("version", version);
                        device.put("versionRelease", versionRelease);
                        device.put("androidDeviceId", androidDeviceId);

                        currentInstallation.put("device", device);
                    }
                    currentInstallation.put("GCMSenderId", "589432994637");
                    currentInstallation.saveEventually();
                }
            });
        }


        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            Log.d("Current user: ", currentUser.getUsername());
            // Login with Parse
            // currentUser.put("isOnline", true);
            // currentUser.saveEventually();
            if (currentUser.isAuthenticated()) {
                // do stuff with the user
                Log.d("isAuthenticated: ", "True");
            } else {
                // show the signup or authenticate screen
                ParseUser.logInInBackground(currentUser.getUsername(), "covid19_panama", new LogInCallback() {
                    public void done(ParseUser user, ParseException e) {
                        if (user != null) {
                            // Hooray! The user is logged in.
                            Log.d("Logeado: ", "True");
                        } else {
                            // Signup failed. Look at the Exception to see what happened.
                        }
                    }
                });
            }
        } else {
            ParseUser.logInInBackground(androidDeviceId, "covid19_panama", new LogInCallback() {
                public void done(ParseUser user, ParseException e) {
                    if (user != null) {
                        // Hooray! The user is logged in.
                        Log.d("Logeado: ", "True");
                    } else {
                        // Signup failed. Look at the Exception to see what happened.
                        createUser();
                    }
                }
            });
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerChannel();
        }

        initDataComponent();
        dataComponent.inject(this);
        initializeTrueTime();

        /*ParseGeoPoint point = new ParseGeoPoint(40.71455, -74.00714);
        ParseObject object = new ParseObject("CovidPlace");
        object.put("covidPlaceId", "2050");
        object.put("country", "US");
        object.put("country_code", "US");
        object.put("population", 310232863);
        object.put("province", "New York");
        object.put("county", "New York");
        object.put("last_updated", "2020-03-27T21:42:05.551460Z");
        object.put("location", point);

        ParseObject dailyReport = new ParseObject("DailyReport");
        dailyReport.put("confirmed", 25573);
        dailyReport.put("deaths", 366);
        dailyReport.put("recovered", 0);
        dailyReport.put("uploadDate", "2020-03-27");
        dailyReport.put("covidPlace", object);

        dailyReport.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null)
                    Log.d("Error: ", e.getMessage());
            }
        });*/



    }

    public void initializeTrueTime() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    TrueTime.build()
                            .withSharedPreferences(CovidApp.this)
                            .withConnectionTimeout(31_428)
                            .withLoggingEnabled(true).initialize();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
    }

    private void initDataComponent() {
        dataComponent = DaggerDataComponent.builder()
                .dataModule(new DataModule(this))
                .build();
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void registerChannel() {
        NotificationChannel channel = new NotificationChannel(
                "default", getString(R.string.channel_default), NotificationManager.IMPORTANCE_MIN);
        channel.setLightColor(Color.GREEN);
        channel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
    }

    public void createUser() {
        ParseUser user = new ParseUser();
        user.setUsername(androidDeviceId);
        user.setPassword("covid19_panama");
        user.setEmail(androidDeviceId + "@goblob.covid.com");

        // Other fields can be set just like any other ParseObject,
        // using the "put" method, like this: user.put("attribute", "its value");
        // If this field does not exists, it will be automatically created

        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    // Hooray! Let them use the app now.
                } else {
                    // Sign up didn't succeed. Look at the ParseException
                    // to figure out what went wrong
                }
            }
        });
    }

    public Activity getRunningActivity() {
        try {
            Class activityThreadClass = Class.forName("android.app.ActivityThread");
            Object activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null);
            Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
            activitiesField.setAccessible(true);
            ArrayMap activities = (ArrayMap) activitiesField.get(activityThread);
            for (Object activityRecord : activities.values()) {
                Class activityRecordClass = activityRecord.getClass();
                Field pausedField = activityRecordClass.getDeclaredField("paused");
                pausedField.setAccessible(true);
                if (!pausedField.getBoolean(activityRecord)) {
                    Field activityField = activityRecordClass.getDeclaredField("activity");
                    activityField.setAccessible(true);
                    return (Activity) activityField.get(activityRecord);
                }
            }
        } catch (Exception e) {
            return null;
        }

        return null;
    }
}