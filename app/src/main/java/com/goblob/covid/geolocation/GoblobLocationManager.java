package com.goblob.covid.geolocation;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.provider.Settings;

import com.goblob.covid.app.CovidApp;
import com.goblob.covid.utils.Systems;
import com.google.android.gms.location.DetectedActivity;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class GoblobLocationManager {
    private static final String TAG = GoblobLocationManager.class.getSimpleName();
    private static GoblobLocationManager instance = null;
    private final Context context;
    private SharedPreferences prefs;
    private Location previousLocationInfo;
    private Location currentLocationInfo;

    private GoblobLocationManager() {
        context = CovidApp.getInstance().getApplicationContext();
        prefs = PreferenceManager.getDefaultSharedPreferences(CovidApp.getInstance().getApplicationContext());
    }

    public static GoblobLocationManager getInstance() {
        if (instance == null) {
            instance = new GoblobLocationManager();
        }
        return instance;
    }

    private String get(String key, String defaultValue) {
        return prefs.getString("SESSION_" + key, defaultValue);
    }

    private void set(String key, String value) {
        prefs.edit().putString("SESSION_" + key, value).apply();
    }


    public boolean isSinglePointMode() {
        return Boolean.valueOf(get("isSinglePointMode", "false"));
    }

    public void setSinglePointMode(boolean singlePointMode) {
        set("isSinglePointMode", String.valueOf(singlePointMode));
    }

    /**
     * @return whether GPS (tower) is enabled
     */
    public boolean isTowerEnabled() {
        return Boolean.valueOf(get("towerEnabled", "false"));
    }

    /**
     * @param towerEnabled set whether GPS (tower) is enabled
     */
    public void setTowerEnabled(boolean towerEnabled) {
        set("towerEnabled", String.valueOf(towerEnabled));
    }

    /**
     * @return whether GPS (satellite) is enabled
     */
    public boolean isGpsEnabled() {
        return Boolean.valueOf(get("gpsEnabled", "false"));
    }

    /**
     * @param gpsEnabled set whether GPS (satellite) is enabled
     */
    public void setGpsEnabled(boolean gpsEnabled) {
        set("gpsEnabled", String.valueOf(gpsEnabled));
    }

    /**
     * @return whether logging has started
     */
    public boolean isStarted() {
        return Boolean.valueOf(get("LOGGING_STARTED", "true"));
    }

    /**
     * @param isStarted set whether logging has started
     */
    public void setStarted(boolean isStarted) {

        set("LOGGING_STARTED", String.valueOf(isStarted));

        if (isStarted) {
            set("startTimeStamp", String.valueOf(System.currentTimeMillis()));
        }
    }

    /**
     * @return the isUsingGps
     */
    public boolean isUsingGps() {
        return Boolean.valueOf(get("isUsingGps", "false"));
    }

    /**
     * @param isUsingGps the isUsingGps to set
     */
    public void setUsingGps(boolean isUsingGps) {
        set("isUsingGps", String.valueOf(isUsingGps));
    }

    /**
     * @return the currentFileName (without extension)
     */
    public String getCurrentFileName() {
        return get("currentFileName", "");
    }


    /**
     * @param currentFileName the currentFileName to set
     */
    public void setCurrentFileName(String currentFileName) {
        set("currentFileName", currentFileName);
    }

    /**
     * @return the number of satellites visible
     */
    public int getVisibleSatelliteCount() {
        return Integer.valueOf(get("satellites", "0"));
    }

    /**
     * @param satellites sets the number of visible satellites
     */
    public void setVisibleSatelliteCount(int satellites) {
        set("satellites", String.valueOf(satellites));
    }


    /**
     * @return the currentLatitude
     */
    public double getCurrentLatitude() {
        if (getCurrentLocationInfo() != null) {
            return getCurrentLocationInfo().getLatitude();
        } else {
            return 0;
        }
    }

    public double getPreviousLatitude() {
        Location loc = getPreviousLocationInfo();
        return loc != null ? loc.getLatitude() : 0;
    }

    public double getPreviousLongitude() {
        Location loc = getPreviousLocationInfo();
        return loc != null ? loc.getLongitude() : 0;
    }

    public double getTotalTravelled() {
        return Double.valueOf(get("totalTravelled", "0"));
    }

    public int getNumLegs() {
        return Integer.valueOf(get("numLegs", "0"));
    }

    public void setNumLegs(int numLegs) {
        set("numLegs", String.valueOf(numLegs));
    }

    public void setTotalTravelled(double totalTravelled) {
        if (totalTravelled == 0) {
            setNumLegs(1);
        } else {
            setNumLegs(getNumLegs() + 1);
        }
        set("totalTravelled", String.valueOf(totalTravelled));
    }

    public Location getPreviousLocationInfo() {
        return previousLocationInfo;
    }

    public void setPreviousLocationInfo(Location previousLocationInfo) {
        this.previousLocationInfo = previousLocationInfo;
    }


    /**
     * Determines whether a valid location is available
     */
    public boolean hasValidLocation() {
        return (getCurrentLocationInfo() != null && getCurrentLatitude() != 0 && getCurrentLongitude() != 0);
    }

    /**
     * @return the currentLongitude
     */
    public double getCurrentLongitude() {
        if (getCurrentLocationInfo() != null) {
            return getCurrentLocationInfo().getLongitude();
        } else {
            return 0;
        }
    }

    /**
     * @return the latestTimeStamp (for location info)
     */
    public long getLatestTimeStamp() {
        return Long.valueOf(get("latestTimeStamp", "0"));
    }

    /**
     * @return the timestamp when measuring was started
     */
    public long getStartTimeStamp() {
        return Long.valueOf(get("startTimeStamp", String.valueOf(System.currentTimeMillis())));
    }

    /**
     * @param latestTimeStamp the latestTimeStamp (for location info) to set
     */
    public void setLatestTimeStamp(long latestTimeStamp) {
        set("latestTimeStamp", String.valueOf(latestTimeStamp));
    }

    /**
     * @return whether to create a new track segment
     */
    public boolean shouldAddNewTrackSegment() {
        return Boolean.valueOf(get("addNewTrackSegment", "false"));
    }

    /**
     * @param addNewTrackSegment set whether to create a new track segment
     */
    public void setAddNewTrackSegment(boolean addNewTrackSegment) {
        set("addNewTrackSegment", String.valueOf(addNewTrackSegment));
    }

    /**
     * @param autoSendDelay the autoSendDelay to set
     */
    public void setAutoSendDelay(float autoSendDelay) {
        set("autoSendDelay", String.valueOf(autoSendDelay));
    }

    /**
     * @return the autoSendDelay to use for the timer
     */
    public float getAutoSendDelay() {
        return Float.valueOf(get("autoSendDelay", "0"));
    }

    /**
     * @param currentLocationInfo the latest LocationParameters class
     */
    public void setCurrentLocationInfo(Location currentLocationInfo) {
        this.currentLocationInfo = currentLocationInfo;
    }

    /**
     * @return the LocationParameters class containing latest lat-long information
     */
    public Location getCurrentLocationInfo() {
        return currentLocationInfo;

    }

    /**
     * @param isBound set whether the activity is bound to the GpsLoggingService
     */
    public void setBoundToService(boolean isBound) {
        set("isBound", String.valueOf(isBound));
    }

    /**
     * @return whether the activity is bound to the GpsLoggingService
     */
    public boolean isBoundToService() {
        return Boolean.valueOf(get("isBound", "false"));
    }

    public boolean hasDescription() {
        return !(getDescription().length() == 0);
    }

    public String getDescription() {
        return get("description", "");
    }

    public void clearDescription() {
        setDescription("");
    }

    public void setDescription(String newDescription) {
        set("description", newDescription);
    }

    public void setWaitingForLocation(boolean waitingForLocation) {
        set("waitingForLocation", String.valueOf(waitingForLocation));
    }

    public boolean isWaitingForLocation() {
        return Boolean.valueOf(get("waitingForLocation", "false"));
    }

    public boolean isAnnotationMarked() {
        return Boolean.valueOf(get("annotationMarked", "false"));
    }

    public void setAnnotationMarked(boolean annotationMarked) {
        set("annotationMarked", String.valueOf(annotationMarked));
    }

    public String getCurrentFormattedFileName() {
        return get("currentFormattedFileName", "");
    }

    public void setCurrentFormattedFileName(String currentFormattedFileName) {
        set("currentFormattedFileName", currentFormattedFileName);
    }

    public long getUserStillSinceTimeStamp() {
        return Long.valueOf(get("userStillSinceTimeStamp", "0"));
    }

    public void setUserStillSinceTimeStamp(long lastUserStillTimeStamp) {
        set("userStillSinceTimeStamp", String.valueOf(lastUserStillTimeStamp));
    }

    public void setFirstRetryTimeStamp(long firstRetryTimeStamp) {
        set("firstRetryTimeStamp", String.valueOf(firstRetryTimeStamp));
    }

    public long getFirstRetryTimeStamp() {
        return Long.valueOf(get("firstRetryTimeStamp", "0"));
    }

    public void setLatestDetectedActivity(DetectedActivity latestDetectedActivity) {
        set("latestDetectedActivity", Strings.getDetectedActivityName(latestDetectedActivity));
        if (latestDetectedActivity != null) {
            set("latestActivityConfidence", Integer.toString(latestDetectedActivity.getConfidence()));
            set("latestActivityType", Integer.toString(latestDetectedActivity.getType()));
        } else {
            set("latestActivityConfidence", "-1");
            set("latestActivityType", "-1");
        }
    }

    public int getLatestActivityType() {
        return Integer.parseInt(get("latestActivityType", "-1"));
    }

    public int getLatestActivityConfidence() {
        return Integer.parseInt(get("latestActivityConfidence", "-1"));
    }

    public String getLatestDetectedActivityName() {
        return get("latestDetectedActivity", "");
    }



    /**
     * Funcion que recibe una location y pregunta por todos los profiles_to_share que esten almacenados localmente
     * Para cada profile que este compartiendo se manda a salvar la liveLocation usando una funcion en la cloud
     * que se llama saveLiveLocation
     *
     * @param loc Location que se recibe de los sensores del device
     */
    public void saveLiveLocation(Location loc) {
        /*daoFactory.getProfileBasicDAO().getLocalProfiles(new FindCallback<IProfileBasic>() {
            @Override
            public void done(List<IProfileBasic> profileBasicList, Exception e) {
                if (profileBasicList != null && profileBasicList.size() > 0) {
                    for (int i = 0; i < profileBasicList.size(); i++) {
                        IProfileBasic profileBasic = profileBasicList.get(i);
                        try {
                            if (profileBasic.isSharingWithFriends() || profileBasic.isNearMe() || isSharingLocation) {
                                daoFactory.getLocationDAO().saveLiveLocation(context, profileBasic, loc, getLatestActivityConfidence(), getLatestActivityType());
                            }
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        });*/

        final ParseUser user = ParseUser.getCurrentUser();
        ParseRelation<ParseObject> relation = user.getRelation("locations");

        ParseGeoPoint latlng = new ParseGeoPoint(loc.getLatitude(), loc.getLongitude());

        ParseObject location = new ParseObject("Location");
        location.put("deviceId", user.getUsername());
        location.put("location", latlng);
        location.put("altitude", loc.getAltitude());
        location.put("speed", loc.getSpeed());
        location.put("provider", loc.getProvider());
        location.put("accuracy", loc.getAccuracy());
        location.put("battery", Systems.getBatteryLevel(context));
        // location.put("mock", loc.getMock());
        location.put("user", user);
        location.saveInBackground();
    }

    public boolean checkLocationActive(String action) {
        ContentResolver contentResolver = context.getContentResolver();
        int mode = Settings.Secure.getInt(contentResolver, Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF);

        if (mode == Settings.Secure.LOCATION_MODE_OFF) {
            checkLocationSettings(action);
            return false;
        }
        return true;
    }

    public void checkLocationSettings(String action) {
        Intent intent = new Intent(context, GpsLoggingService.class);
        intent.putExtra(IntentConstants.CHECK_LOCATION_SETTINGS, true);
        intent.putExtra(IntentConstants.ACTION, action);
        context.startService(intent);
    }

    public void setHowAreYou(boolean b) {
        set("HowAreYou", String.valueOf(b));
        if(!b){
            Calendar newDate = Calendar.getInstance();
            SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
            set("lastHowAreYou", dateFormatter.format(newDate.getTime()));
        }
    }

    public boolean isHowAreYouToday(){
        Calendar newDate = Calendar.getInstance();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
        return get("lastHowAreYou", "").equalsIgnoreCase(dateFormatter.format(newDate.getTime()));
    }

    /**
     * @return whether logging has started
     */
    public boolean isHowAreYou() {
        return Boolean.valueOf(get("HowAreYou", "true"));
    }
}
