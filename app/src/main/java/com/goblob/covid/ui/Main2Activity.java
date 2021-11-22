package com.goblob.covid.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Toast;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.goblob.covid.R;
import com.goblob.covid.geolocation.CommandEvents;
import com.goblob.covid.geolocation.EventBusHook;
import com.goblob.covid.geolocation.GoblobLocationManager;
import com.goblob.covid.geolocation.GpsLoggingService;
import com.goblob.covid.geolocation.IntentConstants;
import com.goblob.covid.geolocation.SessionLogcatAppender;
import com.goblob.covid.utils.GoblobLogsManager;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.greenrobot.event.EventBus;

public class Main2Activity extends AppCompatActivity {
    private static final String TAG = "Main2Activity";
    private static final int LOCATION_PICKER = 125;

    private static final String[] REQUIRED_PERMISSIONS =
            new String[] {
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
    private GoblobLocationManager goblobLocationManager = GoblobLocationManager.getInstance();
    private static final Logger LOG = LoggerFactory.getLogger(Main2Activity.class);

    private static final int REQUEST_CODE_REQUIRED_PERMISSIONS = 1;

    private AppBarConfiguration mAppBarConfiguration;
    private NavController navController;
    private FloatingActionButton fab;
    private MenuItem item;

    public Activity getInstance() {
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Log.d("Destionation: ", mAppBarConfiguration.getTopLevelDestinations().toString());
                String contact = "+507 6931-3823"; // use country code with your phone number
                String url = "https://api.whatsapp.com/send?phone=" + contact;
                try {
                    PackageManager pm = getBaseContext().getPackageManager();
                    pm.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES);
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                } catch (PackageManager.NameNotFoundException e) {
                    Toast.makeText(Main2Activity.this, "Whatsapp app not installed in your phone", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        final NavigationView navigationView = findViewById(R.id.nav_view);

        item = navigationView.getMenu().findItem(R.id.start_stop);

        if(GoblobLocationManager.getInstance().isStarted()) {
            item.setTitle(R.string.stop_ride);
            item.setIcon(R.drawable.ic_gps_off_black_24dp);
        } else {
            item.setTitle(R.string.start_ride);
            item.setIcon(R.drawable.ic_gps_lime);
        }

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_map, R.id.nav_gallery, R.id.nav_notification, R.id.nav_profile,
                R.id.nav_create_notification, R.id.nav_symptom, R.id.nav_chart)
                .setDrawerLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        //NavigationUI.setupWithNavController(navigationView, navController);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                boolean handled = NavigationUI.onNavDestinationSelected(item, navController);

                if(item.getItemId() == R.id.start_stop) {
                    // showInfoDialog("Show profile");
                    if (GoblobLocationManager.getInstance().checkLocationActive("startAndBindService")) {
                        Intent serviceIntent = new Intent(Main2Activity.this, GpsLoggingService.class);

                        if (GoblobLocationManager.getInstance().isStarted()) {
                            serviceIntent.putExtra(IntentConstants.IMMEDIATE_STOP, true);
                            item.setTitle(R.string.start_ride);
                            item.setIcon(R.drawable.ic_gps_lime);
                        } else {
                            item.setTitle(R.string.stop_ride);
                            item.setIcon(R.drawable.ic_gps_off_black_24dp);
                            serviceIntent.putExtra(IntentConstants.IMMEDIATE_START, true);
                        }
                        // Start the service in case it isn't already running
                        startService(serviceIntent);
                        // Now bind to service
                        goblobLocationManager.setBoundToService(true);
                    }
                } else if(item.getItemId() == R.id.send_logs) {
                    GoblobLogsManager.get().sendLogsByEmail(Main2Activity.this);
                } else {
                    if (handled) {
                        ViewParent parent = navigationView.getParent();
                        if (parent instanceof DrawerLayout) {
                            ((DrawerLayout) parent).closeDrawer(navigationView);
                        } else {
                            BottomSheetBehavior bottomSheetBehavior = findBottomSheetBehavior(navigationView);
                            if (bottomSheetBehavior != null) {
                                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                            }
                        }
                    }
                }
                return handled;
            }
        });
        //startAndBindService();

        Bundle extras = getIntent().getExtras();

        if (extras != null && extras.containsKey("Init") && extras.getBoolean("Init", false) == true) {
            showProfile();
        }

        if (extras != null && extras.containsKey("BAD") && extras.getBoolean("BAD", false) == true) {
            extras.remove("BAD");
            navController.navigate(R.id.nav_symptom);
        }

        registerEventBus();

        GoblobLogsManager.get().startLogsStore();
    }


    /**
     * EventBus Zone. Here we put all the eventBus methods.
     */
    private void registerEventBus() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }


    @EventBusHook
    public void onEvent(CommandEvents.RequestStartStop startStop){
        if(startStop.start) {
            item.setTitle(R.string.stop_ride);
            item.setIcon(R.drawable.ic_gps_off_black_24dp);
        } else {
            item.setTitle(R.string.start_ride);
            item.setIcon(R.drawable.ic_gps_lime);
        }
    }

    static BottomSheetBehavior findBottomSheetBehavior(@NonNull View view) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (!(params instanceof CoordinatorLayout.LayoutParams)) {
            ViewParent parent = view.getParent();
            if (parent instanceof View) {
                return findBottomSheetBehavior((View) parent);
            }
            return null;
        }
        CoordinatorLayout.Behavior behavior = ((CoordinatorLayout.LayoutParams) params)
                .getBehavior();
        if (!(behavior instanceof BottomSheetBehavior)) {
            // We hit a CoordinatorLayout, but the View doesn't have the BottomSheetBehavior
            return null;
        }
        return (BottomSheetBehavior) behavior;
    }

    /**
     * Starts the service and binds the activity to it.
     */
    private void startAndBindService() {
        if (GoblobLocationManager.getInstance().checkLocationActive("startAndBindService")) {
            GpsLoggingService.startService(this);
            goblobLocationManager.setBoundToService(true);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!hasPermissions(this, REQUIRED_PERMISSIONS)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_REQUIRED_PERMISSIONS);
            }
        }
        startAndBindService();
    }

    @Override
    protected void onDestroy() {
        stopAndUnbindServiceIfRequired();
        unregisterEventBus();
        super.onDestroy();
    }


    private void unregisterEventBus() {
        try {
            EventBus.getDefault().unregister(this);
        } catch (Throwable t) {
            //this may crash if registration did not go through. just be safe
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //startAndBindService();
    }

    /**
     * Stops the service if it isn't logging. Also unbinds.
     */
    private void stopAndUnbindServiceIfRequired() {
        if (goblobLocationManager.isBoundToService()) {
            try {
                goblobLocationManager.setBoundToService(false);
            } catch (Exception e) {
                LOG.warn(SessionLogcatAppender.MARKER_INTERNAL, "Could not unbind service", e);
            }
        }
    }

    /** Returns true if the app was granted all the permissions. Otherwise, returns false. */
    private static boolean hasPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /** Handles user acceptance (or denial) of our permission request. */
    @CallSuper
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        startAndBindService();

        GoblobLogsManager.get().startLogsStore();

        if (requestCode != REQUEST_CODE_REQUIRED_PERMISSIONS) {
            return;
        }

        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, R.string.error_missing_permissions, Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        }
        recreate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main2, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
                /*Log.e(TAG, "onDestinationChanged: "+destination.getLabel());
                if (destination.getId() == R.id.nav_gallery) {
                    fab.setVisibility(View.GONE);
                } else if (destination.getId() == R.id.nav_geofence) {
                    fab.setVisibility(View.VISIBLE);
                    fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                } else if (destination.getId() == R.id.nav_notification) {
                    fab.setVisibility(View.VISIBLE);
                } else {
                    fab.setVisibility(View.VISIBLE);
                }*/
            }
        });
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void showNotifications(){
        navController.navigate(R.id.nav_notification);
    }

    public void showProfile() { navController.navigate(R.id.nav_profile); }

    public void showSymptom() { navController.navigate(R.id.nav_symptom); }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.onNavDestinationSelected(item, navController)
                || super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 2001) {
                /*if (GoblobLocationManager.getInstance().checkLocationActive("currentLocation")) {
                    Intent serviceIntent = new Intent(this, GpsLoggingService.class);
                    serviceIntent.putExtra(IntentConstants.LOG_ONCE, true);
                    // Start the service in case it isn't already running
                    startService(serviceIntent);
                }*/
            } else if (requestCode == 2003) {
                startAndBindService();
            }
        } else {
            if (requestCode == 2003) {
                goblobLocationManager.setStarted(false);
            }
        }
    }

}
