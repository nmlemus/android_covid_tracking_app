package com.goblob.covid.ui.map;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.appolica.interactiveinfowindow.InfoWindow;
import com.appolica.interactiveinfowindow.InfoWindowManager;
import com.appolica.interactiveinfowindow.fragment.MapInfoWindowFragment;
import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.goblob.covid.R;
import com.goblob.covid.data.dao.GetCallback;
import com.goblob.covid.data.dao.model.Bluetooth;
import com.goblob.covid.data.dao.model.CorregimientosPA;
import com.goblob.covid.data.dao.model.DailyReportAll;
import com.goblob.covid.events.LocationProvidersChanged;
import com.goblob.covid.geolocation.CommandEvents;
import com.goblob.covid.geolocation.EventBusHook;
import com.goblob.covid.geolocation.GoblobLocationManager;
import com.goblob.covid.geolocation.IntentConstants;
import com.goblob.covid.geolocation.ServiceEvents;
import com.goblob.covid.geolocation.GpsLoggingService;
import com.goblob.covid.notification.NotificationManager;
import com.goblob.covid.ui.Main2Activity;
import com.goblob.covid.ui.adapters.CustomIconGridViewAdapter;
import com.goblob.covid.ui.adapters.ListResultsRecyclerViewAdapter;
import com.goblob.covid.ui.cluster.BluetoothClusterItem;
import com.goblob.covid.ui.cluster.BluetoothClusterRenderer;
import com.goblob.covid.ui.cluster.CaseClusterItem;
import com.goblob.covid.ui.cluster.CaseClusterRenderer;
import com.goblob.covid.ui.cluster.CovidClusterItem;
import com.goblob.covid.ui.infowindow.FormFragmentProfileInfoContents;
import com.goblob.covid.ui.listener.ClickListener;
import com.goblob.covid.ui.listener.RecyclerTouchListener;
import com.goblob.covid.utils.DbHelper;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;

import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_NORMAL;
import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_SATELLITE;

public class MapFragment extends Fragment implements OnMapReadyCallback, ClusterManager.OnClusterClickListener<ClusterItem>, ClusterManager.OnClusterInfoWindowClickListener<ClusterItem>, ClusterManager.OnClusterItemClickListener<ClusterItem>, ClusterManager.OnClusterItemInfoWindowClickListener<ClusterItem>, View.OnClickListener, InfoWindowManager.WindowShowListener {
    private final String TAG = MapFragment.class.getSimpleName();
    private MapViewModel mapViewModel;
    private GoogleMap googleMap;
    private FloatingSearchView mSearchView;
    private View root;
    private View search_icons_layout;

    String[] gridViewString = {
            "Vista Global",
            "Encuentros",
            "Alertas",
            "Graficos",
            "R.O.S.A"
            //"Dating",
            //"Weather",
            //"New Search"
    };

    int[] gridViewImageId = {
            R.drawable.ic_action_globe,
            R.drawable.icons8_track_order_96,
            R.drawable.ic_warning_black_24dp,
            R.drawable.ic_pie_chart_white_24dp,
            R.drawable.ic_phone_forwarded_red_24dp
            // R.drawable.icons8_dating_48,
            // R.drawable.icons8_partly_cloudy_rain_48,
            // R.drawable.icons8_plus_48
    };
    private ClusterManager<ClusterItem> mClusterManager;
    private Cluster<ClusterItem> currentCluster;
    private boolean centerLocation;
    private FloatingActionButton currentLocation;
    private float cameraZoom = 14;
    private boolean centerLocation1;
    private FloatingActionButton zoomGlobal;
    private FloatingActionButton zoomIn;
    private FloatingActionButton zoomOut;
    private FloatingActionButton btnSatellite;
    private Runnable postDelayed;
    private InfoWindowManager infoWindowManager;
    private InfoWindow infoWindow;
    private BottomSheetBehavior<View> sheetBehavior, symptomSheetBehavior;
    private RecyclerView listResult;
    private ListResultsRecyclerViewAdapter adapter;

    private Button stateFine, stateBad;
    private LatLng lastLocation;
    private boolean waitForCurrentLocation;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        mapViewModel = new ViewModelProvider(this).get(MapViewModel.class);

        root = inflater.inflate(R.layout.fragment_map, container, false);

        sheetBehavior = BottomSheetBehavior.from(root.findViewById(R.id.bottom_sheet));
        symptomSheetBehavior = BottomSheetBehavior.from(root.findViewById(R.id.bottom_sheet_symptom));

        stateFine = root.findViewById(R.id.button_fine);
        stateFine.setOnClickListener(this);
        stateBad = root.findViewById(R.id.button_bad);
        stateBad.setOnClickListener(this);


        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                getActivity().finish();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);

        /**
         * bottom sheet state change listener
         * we are changing button text when sheet changed state
         * */
        sheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED: {
                        //btnBottomSheet.setText("Close Sheet");
                    }
                    break;
                    case BottomSheetBehavior.STATE_COLLAPSED: {
                        //btnBottomSheet.setText("Expand Sheet");
                    }
                    break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        sheetBehavior.setHideable(false);
        sheetBehavior.setDraggable(false);
        sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        configureBottomSheetSymptom();

        mSearchView = root.findViewById(R.id.floating_search_view);

        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();

        DrawerLayout drawer = getActivity().findViewById(R.id.drawer_layout);

        mSearchView.attachNavigationDrawerToMenuButton(drawer);

        mSearchView.setNotificationCount(5);

        mSearchView.setOnNotificationClickListener(new FloatingSearchView.OnNotificationClickListener() {
            @Override
            public void onNotificationClick() {
                ((Main2Activity) getActivity()).showNotifications();
            }
        });

        mSearchView.setOnMenuItemClickListener(new FloatingSearchView.OnMenuItemClickListener() {
            @Override
            public void onActionMenuItemSelected(MenuItem item) {
                if (item.getItemId() == R.id.action_settings) {

                }
            }
        });

        mSearchView.setOnSearchInputClickListener(new FloatingSearchView.OnSearchInputClickListener() {
            @Override
            public void onSearchInputClickListener() {
                showSearchButtons();
            }
        });

        mSearchView.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {
            @Override
            public void onSearchTextChanged(String oldQuery, String newQuery) {
                adapter.getFilter().filter(newQuery.trim().toLowerCase());
            }
        });

        mSearchView.setOnFocusChangeListener(new FloatingSearchView.OnFocusChangeListener() {
            @Override
            public void onFocus() {
                showSearchButtons();
            }

            @Override
            public void onFocusCleared() {
                hideSearchButtons();
            }
        });

        mSearchView.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
            @Override
            public void onSuggestionClicked(SearchSuggestion searchSuggestion) {

            }

            @Override
            public void onSearchAction(String newQuery) {
                adapter.getFilter().filter(newQuery.trim().toLowerCase());
            }
        });

        MapInfoWindowFragment mapFragment = (MapInfoWindowFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        infoWindowManager = mapFragment.infoWindowManager();

        infoWindowManager.setWindowShowListener(this);

        configureSearchGrid();

        registerEventBus();

        countNewNotifications();

        configureFabButtons();

        configureCountryList();

        return root;
    }

    private void configureBottomSheetSymptom() {
        /**
         * bottom sheet state change listener
         * we are changing button text when sheet changed state
         * */
        symptomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED: {
                        //btnBottomSheet.setText("Close Sheet");
                    }
                    break;
                    case BottomSheetBehavior.STATE_COLLAPSED: {
                        //btnBottomSheet.setText("Expand Sheet");
                    }
                    break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        symptomSheetBehavior.setHideable(true);
        symptomSheetBehavior.setDraggable(false);
        symptomSheetBehavior.setPeekHeight(0);
        if (!GoblobLocationManager.getInstance().isHowAreYou()){
            symptomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
        GpsLoggingService.startService(getContext());
    }

    @EventBusHook
    public void onEvent(CommandEvents.HowAreYou howAreYou){
        if (!howAreYou.good) {
            NavHostFragment.findNavController(this).navigate(R.id.nav_symptom);
        }
        if (howAreYou.b) {
            symptomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        } else {
            symptomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    }

    private void configureCountryList() {
        listResult = root.findViewById(R.id.resultlist);

        listResult.setHasFixedSize(true);

        listResult.setItemAnimator(new DefaultItemAnimator());

        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);

        listResult.addItemDecoration(itemDecoration);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());

        layoutManager.setOrientation(RecyclerView.VERTICAL);

        layoutManager.scrollToPosition(0);

        listResult.setLayoutManager(layoutManager);

        listResult.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (getActivity().getCurrentFocus() != null) {
                    InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                }
            }
        });

        listResult.addOnItemTouchListener(new RecyclerTouchListener(getContext(), listResult, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                mSearchView.clearFocus();
                search_icons_layout.setVisibility(View.GONE);

                List values = ((ListResultsRecyclerViewAdapter) listResult.getAdapter()).getContactListFiltered();
                if (position != -1 && position < values.size()) {
                    if (values.get(position) instanceof DailyReportAll) {
                        DailyReportAll report = (DailyReportAll) values.get(position);
                        LatLng latLng = new LatLng(report.getLocation().getLatitude(), report.getLocation().getLongitude());
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 5));
                        showInfoWindow(report.getCaseClusterItem());
                    } else {
                        CorregimientosPA report = (CorregimientosPA) values.get(position);
                        LatLng latLng = new LatLng(report.getLocation().getLatitude(), report.getLocation().getLongitude());
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                        showInfoWindow(report.getCaseClusterItem());
                    }
                }
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
    }

    private void configureFabButtons() {
        zoomGlobal = root.findViewById(R.id.zoomglobal);
        zoomGlobal.setOnClickListener(this);
        zoomGlobal.hide();

        zoomIn = root.findViewById(R.id.zoomin);
        zoomIn.hide();
        zoomIn.setOnClickListener(this);

        zoomOut = root.findViewById(R.id.zoomout);
        zoomOut.hide();
        zoomOut.setOnClickListener(this);

        btnSatellite = root.findViewById(R.id.btnSatellite);
        btnSatellite.hide();
        btnSatellite.setOnClickListener(this);

        currentLocation = root.findViewById(R.id.currentLocation);
        currentLocation.setOnClickListener(this);
        currentLocation.hide();

        locationMode();
    }

    private void showFabButtons() {
        zoomGlobal.hide();
        zoomIn.show();
        zoomOut.show();
        btnSatellite.show();
        currentLocation.show();

        if (postDelayed != null) {
            zoomGlobal.removeCallbacks(postDelayed);
        }

        postDelayed = new Runnable() {
            @Override
            public void run() {
                zoomGlobal.hide();
                zoomIn.hide();
                zoomOut.hide();
                btnSatellite.hide();
                currentLocation.hide();
                postDelayed = null;
            }
        };

        zoomGlobal.postDelayed(postDelayed, 7000);
    }

    private void locationMode() {
        int mode = Settings.Secure.getInt(getActivity().getContentResolver(), Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF);

        if (mode == Settings.Secure.LOCATION_MODE_OFF) {
            currentLocation.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_gps_off_black_24dp));
        } else {
            currentLocation.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_gps_lime));

            if (waitForCurrentLocation) {
                waitForCurrentLocation = false;
                if (GoblobLocationManager.getInstance().checkLocationActive("currentLocation")) {
                    Intent serviceIntent = new Intent(getContext(), GpsLoggingService.class);
                    serviceIntent.putExtra(IntentConstants.LOG_ONCE, true);
                    // Start the service in case it isn't already running
                    getActivity().startService(serviceIntent);
                }
            }
        }
    }

    @EventBusHook
    public void onEventMainThread(LocationProvidersChanged locationProvidersChanged) {
        locationMode();
    }

    private void hideSearchButtons() {
        search_icons_layout.setVisibility(View.GONE);
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
    }

    private void showSearchButtons() {
        mSearchView.hideProgress();
        search_icons_layout.setVisibility(View.VISIBLE);
    }

    private void search(GoblobSearchType searchType) {
        search_icons_layout.setVisibility(View.GONE);
        mSearchView.clearSearchFocus();

        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);

        VisibleRegion vr = googleMap.getProjection().getVisibleRegion();

        Location center = new Location("center");
        center.setLatitude(googleMap.getCameraPosition().target.latitude);
        center.setLongitude(googleMap.getCameraPosition().target.longitude);

        Location farVisiblePoint = new Location("farPoint");
        farVisiblePoint.setLatitude(vr.farLeft.latitude);
        farVisiblePoint.setLongitude(vr.farLeft.longitude);

        float radius = center.distanceTo(farVisiblePoint);

        radius = radius < 50000 ? radius : 50000;

        if (searchType == GoblobSearchType.Notifications) {
            // NavHostFragment.findNavController(this).navigate(R.id.nav_notification);
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(2.0f));
            showCases("global");
            showFabButtons();
        } else if (searchType == GoblobSearchType.Traces) {
            showTraces();
        } else if (searchType == GoblobSearchType.Alert) {
            NavHostFragment.findNavController(this).navigate(R.id.nav_notification);
            /*Map param = new HashMap();
            ParseCloud.callFunctionInBackground("test", param, new FunctionCallback<String>() {
                @Override
                public void done(String result, ParseException e) {
                    Log.e(TAG, ""+result);
                }
            });*/
        } else if (searchType == GoblobSearchType.Emergency) {
            String contact = "+507 6931-3823"; // use country code with your phone number
            String url = "https://api.whatsapp.com/send?phone=" + contact;
            try {
                PackageManager pm = getActivity().getBaseContext().getPackageManager();
                pm.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES);
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            } catch (PackageManager.NameNotFoundException e) {
                Toast.makeText(getActivity(), "Whatsapp app not installed in your phone", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        } else if (searchType == GoblobSearchType.Charts) {
            NavHostFragment.findNavController(this).navigate(R.id.nav_chart);
        } else {
            clearClusters();
        }
    }

    private void clearClusters() {
        for (Iterator<ClusterItem> iterator = mClusterManager.getAlgorithm().getItems().iterator(); iterator.hasNext(); ) {
            ClusterItem profileClusterItem = iterator.next();
            ((CovidClusterItem)profileClusterItem).removePolyline();
            mClusterManager.getAlgorithm().removeItem(profileClusterItem);
        }

        mClusterManager.cluster();
    }

    private void configureSearchGrid() {
        search_icons_layout = root.findViewById(R.id.search_icons_layout);

        final GridView searchGridView = (GridView) root.findViewById(R.id.searchGridView);

        searchGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (getActivity().getCurrentFocus() != null) {
                    InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });

        final CustomIconGridViewAdapter adapterViewAndroid = new CustomIconGridViewAdapter(getContext(), gridViewString, gridViewImageId);
        searchGridView.setNumColumns(5);
        searchGridView.setAdapter(adapterViewAndroid);
        searchGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
                search(GoblobSearchType.values()[i]);
            }
        });
    }


    private void unregisterEventBus() {
        try {
            EventBus.getDefault().unregister(this);
        } catch (Throwable t) {
            //this may crash if registration did not go through. just be safe
        }
    }

    @Override
    public void onDestroyView() {
        unregisterEventBus();
        googleMap.clear();
        super.onDestroyView();
    }

    /**
     * EventBus Zone. Here we put all the eventBus methods.
     */
    private void registerEventBus() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap map) {
        this.googleMap = map;

        moveGoogleMapCompass();

        this.googleMap.getUiSettings().setMyLocationButtonEnabled(false);

        this.googleMap.getUiSettings().setMapToolbarEnabled(false);

        this.googleMap.getUiSettings().setCompassEnabled(true);

        this.googleMap.setBuildingsEnabled(false);

        this.googleMap.setPadding(0, 200, 0, 150);

        centerLocation1 = DbHelper.get().getValue("cameraZoom") == null;

        mClusterManager = new ClusterManager<ClusterItem>(getContext(), this.googleMap);
        mClusterManager.setOnClusterClickListener(this);
        mClusterManager.setOnClusterInfoWindowClickListener(this);
        mClusterManager.setOnClusterItemClickListener(this);
        mClusterManager.setOnClusterItemInfoWindowClickListener(this);
        mClusterManager.setAnimation(true);

        infoWindowManager.onMapReady(map);

        infoWindowManager.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                showFabButtons();
            }
        });

        infoWindowManager.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                mClusterManager.onCameraIdle();

                CameraPosition cameraPosition = googleMap.getCameraPosition();

                cameraZoom = cameraPosition.zoom;

                Log.d("Zoom de la camara: ", String.valueOf(cameraZoom));
                DbHelper.get().saveValue("cameraZoom", Float.toString(cameraPosition.zoom));
                DbHelper.get().saveValue("cameraLatitude", Double.toString(cameraPosition.target.latitude));
                DbHelper.get().saveValue("cameraLongitude", Double.toString(cameraPosition.target.longitude));

                if (cameraPosition.zoom >= 20 && currentCluster != null && currentCluster.getSize() > 1) {
                    recalculateClusterItemsPosition(currentCluster);
                    currentCluster = null;
                }
            }
        });

        infoWindowManager.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int reason) {
                if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                    centerLocation = false;
                } else if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_API_ANIMATION) {
                    //Toast.makeText(this, "The user tapped something on the map.", Toast.LENGTH_SHORT).show();
                } else if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_DEVELOPER_ANIMATION) {
                    //Toast.makeText(this, "The app moved the camera.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        googleMap.setOnMarkerClickListener(mClusterManager);

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        }

        if (!centerLocation && !centerLocation1) {
            restoreMapTypeAndCameraPosition();
        } else if (centerLocation1){
            if(ParseUser.getCurrentUser().has("countrycode")) {
                mapViewModel.getDailyReport(ParseUser.getCurrentUser().getString("countrycode")).observe(this, new Observer<DailyReportAll>() {
                    @Override
                    public void onChanged(DailyReportAll dailyReportAll) {
                        if (dailyReportAll != null) {
                            centerLocation1 = false;
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(dailyReportAll.getLocation().getLatitude(), dailyReportAll.getLocation().getLongitude()), 6));
                        } else if (lastLocation != null) {
                            centerLocation1 = false;
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastLocation, 16));
                        }
                    }
                });
            } else if(lastLocation != null) {
                centerLocation1 = false;
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastLocation, 16));
            }
        }

        showCases("start");
    }

    private void moveGoogleMapCompass() {
        try {
            View mMapView = root.findViewById(R.id.map);

            final ViewGroup
                    parent = (ViewGroup) mMapView.findViewWithTag("GoogleMapMyLocationButton").getParent();
            parent.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        Resources r = getResources();
                        //convert our dp margin into pixels
                        int marginPixels = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, r.getDisplayMetrics());
                        // Get the map compass view
                        View mapCompass = parent.getChildAt(4);

                        // create layoutParams, giving it our wanted width and height(important, by default the width is "match parent")
                        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(mapCompass.getHeight(), mapCompass.getHeight());
                        // position on top right
                        rlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
                        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                        rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
                        //give compass margin
                        rlp.setMargins(marginPixels, 50, marginPixels, marginPixels);

                        mapCompass.setLayoutParams(rlp);

                        View mapRoute = parent.getChildAt(3);

                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mapRoute.getLayoutParams();

                        // Align it to - parent top|left
                        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
                        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);

                        params.setMargins(marginPixels, 0, 0, 0);

                        mapRoute.setLayoutParams(params);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void recalculateClusterItemsPosition(Cluster<ClusterItem> cluster) {
        try {
            int count = 0;
            float a = (float) 360.0 / cluster.getItems().size();
            LatLngBounds.Builder builder = LatLngBounds.builder();

            for (ClusterItem person : cluster.getItems()) {
                ((CovidClusterItem)person).setNewPosition(new LatLng(((CovidClusterItem)person).getCurrentLocation().latitude - 0.0003 * Math.cos(
                        (a * count) / 180 * Math.PI), ((CovidClusterItem)person).getCurrentLocation().longitude - 0.0003 * Math.sin(
                        (a * count) / 180 * Math.PI)));

                count++;

                builder.include(person.getPosition());

                if (person instanceof BluetoothClusterItem) {
                    animateMarkerTo((BluetoothClusterItem)person);
                } else {
                    animateMarkerTo((CaseClusterItem)person);
                }
            }

            // Get the LatLngBounds
            final LatLngBounds bounds = builder.build();

            // Animate camera to the bounds
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 80));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void animateMarkerTo(CaseClusterItem marker) {
        if (mClusterManager != null && mClusterManager.getAlgorithm() != null) {
            CaseClusterItem person = new CaseClusterItem(marker.getCurrentLocation(), marker.getTitle(), marker.getSnippet(), marker.getCasesConfirmed(), marker.getCasesRecovered(), marker.getCasesDeaths(), marker.getCreatedAt(), marker.getCountryCode(), googleMap);

            person.setNewPosition(marker.getNewPosition());

            marker.removePolyline();

            mClusterManager.getAlgorithm().removeItem(marker);

            mClusterManager.getAlgorithm().addItem(person);

            mClusterManager.cluster();
        }
    }


    private void animateMarkerTo(BluetoothClusterItem marker) {
        if (mClusterManager != null && mClusterManager.getAlgorithm() != null) {
            BluetoothClusterItem person = new BluetoothClusterItem(marker.getCurrentLocation(), marker.getCrosses(), marker.getMacAddress(), googleMap);

            person.setNewPosition(marker.getNewPosition());

            marker.removePolyline();

            mClusterManager.getAlgorithm().removeItem(marker);

            mClusterManager.getAlgorithm().addItem(person);

            mClusterManager.cluster();
        }
    }


    private void restoreMapTypeAndCameraPosition() {
        String cameraZoom = DbHelper.get().getValue("cameraZoom");
        String cameraLatitude = DbHelper.get().getValue("cameraLatitude");
        String cameraLongitude = DbHelper.get().getValue("cameraLongitude");

        if (cameraZoom != null) {
            this.cameraZoom = Float.parseFloat(cameraZoom);

            if (cameraLatitude != null && cameraLongitude != null) {
                LatLng latLng2 = new LatLng(Double.parseDouble(cameraLatitude), Double.parseDouble(cameraLongitude));
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng2, Float.parseFloat(cameraZoom));
                googleMap.moveCamera(cameraUpdate);
            }
        }

        String mapType = DbHelper.get().getValue("mapType");

        if (mapType != null) {
            googleMap.setMapType(Integer.parseInt(mapType));
            btnSatellite.setImageResource(googleMap.getMapType() == MAP_TYPE_SATELLITE ? R.drawable.ic_satellite_off
                    : R.drawable.ic_satellite_on);
        }
    }

    private void showCases(String searchOption) {
        ParseUser user = ParseUser.getCurrentUser();
        if (user.getString("country").equalsIgnoreCase("Panama") && searchOption.equalsIgnoreCase("start")) {
            showCasesPA();
        } else {
            showCasesAll();
        }
    }

    private void showCasesAll() {
        mSearchView.showProgress();

        if (infoWindow != null) {
            infoWindowManager.hide(infoWindow, false);
        }

        clearClusters();

        mClusterManager.setRenderer(new CaseClusterRenderer(this, this.googleMap, mClusterManager));

        mapViewModel.getDailyReportAll().observe(getViewLifecycleOwner(), new Observer<List<DailyReportAll>>() {
            @Override
            public void onChanged(List<DailyReportAll> objects) {
                List<WeightedLatLng> list = new ArrayList<WeightedLatLng>();

                adapter = new ListResultsRecyclerViewAdapter(getContext(), objects);
                listResult.setAdapter(adapter);

                for (DailyReportAll object : objects) {
                    ParseGeoPoint point = object.getLocation();

                    int confirmed = object.getConfirmed();
                    int recovered = object.getRecovered();
                    int death = object.getDeaths();

                    if (object.has("population") && object.get("population") != null && object.getInt("population") > 0) {
                        double confirmed2 = Double.valueOf(confirmed) / 100000;
                        WeightedLatLng weightedLatLng = new WeightedLatLng(new LatLng(point.getLatitude(), point.getLongitude()), confirmed2);
                        list.add(weightedLatLng);
                    }


                    String title = object.getString("country");

                    /*if (object.has("country")) {
                        title = object.getString("country");
                    }*/

                    if (object.has("province") && object.getString("province") != null && !object.getString("province").equalsIgnoreCase("")) {
                        title += "/" + object.getString("province");
                    }
                    if (object.has("county") && object.getString("county") != null && !object.getString("county").equalsIgnoreCase("")) {
                        title += "/" + object.getString("county");
                    }

                    CaseClusterItem caseClusterItem = new CaseClusterItem(new LatLng(point.getLatitude(), point.getLongitude()), title, Integer.toString(confirmed), confirmed, recovered, death, object.getCreatedAt(), object.getCountryCode(), googleMap);
                    object.setCaseClusterItem(caseClusterItem);
                    mClusterManager.addItem(caseClusterItem);
                }
                // Create a heat map tile provider, passing it the latlngs of the police stations.
                /*HeatmapTileProvider mProvider = new HeatmapTileProvider.Builder()
                        .weightedData(list)
                        .build();
                // Add a tile overlay to the map, using the heat map tile provider.
                TileOverlay mOverlay = googleMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
                // mOverlay.notify();*/

                mClusterManager.cluster();
                mSearchView.hideProgress();
            }
        });
    }

    private void showCasesPA() {
        mSearchView.showProgress();

        LatLng latLng2 = new LatLng(8.9936, -79.51973);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng2, 8);
        googleMap.moveCamera(cameraUpdate);

        clearClusters();

        mClusterManager.setRenderer(new CaseClusterRenderer(this, this.googleMap, mClusterManager));

        mapViewModel.getDailyReportPA().observe(getViewLifecycleOwner(), new Observer<List<CorregimientosPA>>() {
            @Override
            public void onChanged(List<CorregimientosPA> objects) {
                List<WeightedLatLng> list = new ArrayList<WeightedLatLng>();

                adapter = new ListResultsRecyclerViewAdapter(getContext(), objects);
                listResult.setAdapter(adapter);

                for (CorregimientosPA object : objects) {
                    int recovered = 0;
                    int death = 0;
                    ParseGeoPoint point = (ParseGeoPoint) object.get("location");
                    int confirmed = (Integer) object.get("confirmed");
                    /*if (object.has("recovered") && object.get("recovered") != null) {
                        recovered = (Integer) object.get("recovered");
                    }
                    if (object.has("deaths") && object.get("deaths") != null) {
                        death = (Integer) object.get("deaths");
                    }

                    if (object.has("population") && object.get("population") != null && object.getInt("population") > 0) {
                        double confirmed2 = Double.valueOf(confirmed) / 100000;
                        WeightedLatLng weightedLatLng = new WeightedLatLng(new LatLng(point.getLatitude(), point.getLongitude()), confirmed2);
                        list.add(weightedLatLng);
                    }*/

                    WeightedLatLng weightedLatLng = new WeightedLatLng(new LatLng(point.getLatitude(), point.getLongitude()), confirmed);
                    list.add(weightedLatLng);

                    String title = object.getString("name");

                    /*if (object.has("country")) {
                        title = object.getString("country");
                    }*/

                    if (object.has("province") && object.getString("province") != null && !object.getString("province").equalsIgnoreCase("")) {
                        title += "/" + object.getString("province");
                    }
                    if (object.has("county") && object.getString("county") != null && !object.getString("county").equalsIgnoreCase("")) {
                        title += "/" + object.getString("county");
                    }

                    CaseClusterItem caseClusterItem = new CaseClusterItem(new LatLng(point.getLatitude(), point.getLongitude()), title, Integer.toString(confirmed), confirmed, recovered, death, object.getCreatedAt(), "pa", googleMap);
                    object.setCaseClusterItem(caseClusterItem);
                    mClusterManager.addItem(caseClusterItem);
                }
                // Create a heat map tile provider, passing it the latlngs of the police stations.
                HeatmapTileProvider mProvider = new HeatmapTileProvider.Builder()
                        .weightedData(list)
                        .build();
                // Add a tile overlay to the map, using the heat map tile provider.
                TileOverlay mOverlay = googleMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
                // mOverlay.notify();

                mClusterManager.cluster();
                mSearchView.hideProgress();
            }
        });
    }

    private void showGeofences() {
        // mSearchView.showProgress();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Geofence");
        query.fromPin("geofence");
        query.fromLocalDatastore();
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (objects != null && objects.size() > 0) {
                    for (ParseObject object : objects) {
                        ParseGeoPoint point = (ParseGeoPoint) object.get("location");
                        Circle circle = googleMap.addCircle(new CircleOptions()
                                .center(new LatLng(point.getLatitude(), point.getLongitude()))
                                .radius(150)
                                .strokeColor(Color.RED)
                                .fillColor(Color.BLUE));
                        if (object.get("type").toString().equalsIgnoreCase("Home")) {
                            googleMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(point.getLatitude(), point.getLongitude()))
                                    .title("Home")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons8_home_address_48))
                                    .snippet("#QuedateEnCasa"));
                        } else {
                            googleMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(point.getLatitude(), point.getLongitude()))
                                    .title("Work")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons8_hammer_48))
                                    .snippet("#Cuidate, #MantenLaDistancia"));
                        }
                    }
                }
            }
        });
    }

    private void showTraces() {
        mSearchView.showProgress();

        if (infoWindow != null) {
            infoWindowManager.hide(infoWindow, false);
        }

        clearClusters();

        mClusterManager.setRenderer(new BluetoothClusterRenderer(this, this.googleMap, mClusterManager));

        final Calendar newCalendar = Calendar.getInstance();
        DatePickerDialog StartTime = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);

                mapViewModel.getTrace(ParseUser.getCurrentUser(), newDate.getTime()).observe(getViewLifecycleOwner(), new Observer<List<Bluetooth>>() {
                    @Override
                    public void onChanged(List<Bluetooth> objects) {
                        List<Bluetooth> list = new ArrayList<Bluetooth>();

                        for (Bluetooth object : objects) {
                            ParseGeoPoint point = object.getLocation();
                            if (point != null) {
                                boolean e = false;
                                for (Bluetooth b : list) {
                                    if (b.getMacAddress().toLowerCase().equalsIgnoreCase(object.getMacAddress().toLowerCase())) {
                                        e = true;
                                        b.addLocation(object.getLocation());
                                        break;
                                    }
                                }
                                if (!e) {
                                    object.addLocation(object.getLocation());
                                    list.add(object);
                                }
                            }
                        }

                        LatLngBounds.Builder builder = LatLngBounds.builder();

                        for (Bluetooth object : list) {
                            for (ParseGeoPoint point : object.getLocations()) {
                                /*googleMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(point.getLatitude(), point.getLongitude()))
                                        .title((String) object.get("createdAt"))
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons8_walking_48))
                                        /*.snippet((String) object.get("provider"))*///);

                                LatLng latLng = new LatLng(point.getLatitude(), point.getLongitude());
                                builder.include(latLng);
                                BluetoothClusterItem bluetoothClusterItem = new BluetoothClusterItem(latLng, object.getCrosses(), object.getMacAddress(), googleMap);
                                object.setBluetoothClusterItem(bluetoothClusterItem);
                                mClusterManager.addItem(bluetoothClusterItem);
                            }
                        }
                        if (list.size() > 0) {
                            LatLngBounds bounds = builder.build();
                            // Animate camera to the bounds
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 80));
                        }
                        mClusterManager.cluster();
                        mSearchView.hideProgress();
                    }
                });
            }
        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
        StartTime.show();
    }


    @EventBusHook
    public void onEventMainThread(ServiceEvents.LocationUpdate locationUpdate) {
        if (locationUpdate == null || locationUpdate.location == null) {
            return;
        }

        lastLocation = new LatLng(locationUpdate.location.getLatitude(), locationUpdate.location.getLongitude());

        if (centerLocation1) {
            if (googleMap != null) {
                googleMap.setMyLocationEnabled(true);
            }

            centerLocation1 = false;
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastLocation, 16));
        }

        if (centerLocation) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastLocation, cameraZoom));
        }
    }

    private void countNewNotifications() {
        NotificationManager.get().getUnreadNotifications(new GetCallback<Integer>() {
            @Override
            public void done(final Integer count, Exception e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSearchView.setNotificationCount(count);
                    }
                });
            }
        });
    }

    void showHeatmap() {
        final List<WeightedLatLng> list = new ArrayList<WeightedLatLng>();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Covid");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                for (ParseObject object : objects) {
                    ParseGeoPoint point = (ParseGeoPoint) object.get("location");
                    Double d = Double.valueOf((Integer) object.get("confirmed"));
                    WeightedLatLng weightedLatLng = new WeightedLatLng(new LatLng(point.getLatitude(), point.getLongitude()), d);
                    list.add(weightedLatLng);
                }
                // Create a heat map tile provider, passing it the latlngs of the police stations.
                HeatmapTileProvider mProvider = new HeatmapTileProvider.Builder()
                        .weightedData(list)
                        .build();
                // Add a tile overlay to the map, using the heat map tile provider.
                TileOverlay mOverlay = googleMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
                // mOverlay.notify();
            }
        });
    }

    @Override
    public boolean onClusterClick(Cluster<ClusterItem> cluster) {
        this.currentCluster = cluster;
        // Create the builder to collect all essential cluster items for the bounds.
        LatLngBounds.Builder builder = LatLngBounds.builder();
        for (ClusterItem item : cluster.getItems()) {
            builder.include(item.getPosition());
        }
        // Get the LatLngBounds
        final LatLngBounds bounds = builder.build();

        // Animate camera to the bounds
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 80));

        return true;
    }

    @Override
    public void onClusterInfoWindowClick(Cluster<ClusterItem> cluster) {

    }

    @Override
    public boolean onClusterItemClick(ClusterItem item) {
        if(item instanceof CaseClusterItem) {
            showInfoWindow((CaseClusterItem) item);
        }
        return false;
    }

    public void showInfoWindow(CaseClusterItem caseClusterItem) {
        if (infoWindow != null) {
            infoWindowManager.hide(infoWindow, false);
        }

        final int offsetX = 5;
        final int offsetY = 80;

        final InfoWindow.MarkerSpecification markerSpec =
                new InfoWindow.MarkerSpecification(offsetX, offsetY);
        markerSpec.setCenterByX(true);

        caseClusterItem.setInfoWindowShow(true);

        if (caseClusterItem.getMarker() != null) {
            FormFragmentProfileInfoContents formFragmentProfileInfoContents = new FormFragmentProfileInfoContents();
            formFragmentProfileInfoContents.setCaseClusterItem(caseClusterItem);
            formFragmentProfileInfoContents.setLifecycleOwner(this);
            formFragmentProfileInfoContents.setMarker(caseClusterItem.getMarker());
            infoWindow = new InfoWindow(caseClusterItem.getMarker(), markerSpec, formFragmentProfileInfoContents);
            infoWindowManager.toggle(infoWindow, false);
        }
    }

    @Override
    public void onClusterItemInfoWindowClick(ClusterItem item) {

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.currentLocation) {
            showFabButtons();
            if (cameraZoom < 16) {
                cameraZoom = 16;
                currentLocation();
            } else if (cameraZoom >= 16 && cameraZoom < 18) {
                cameraZoom = 18;
                currentLocation();
            } else if (cameraZoom >= 18) {
                cameraZoom = 16;
                currentLocation();
            }
        } else if (id == R.id.zoomglobal) {
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(2.0f));
            showCases("global");
            showFabButtons();
        } else if (id == R.id.zoomin) {
            showFabButtons();
            googleMap.animateCamera(CameraUpdateFactory.zoomOut());
        } else if (id == R.id.zoomout) {
            showFabButtons();
            googleMap.animateCamera(CameraUpdateFactory.zoomIn());
        } else if (id == R.id.btnSatellite) {
            showFabButtons();
            changeMapType();
        } else if (id == R.id.button_bad) {
            Intent serviceIntent = new Intent(getContext(), GpsLoggingService.class);
            serviceIntent.putExtra(IntentConstants.GOOD, false);
            getActivity().startService(serviceIntent);

            NavHostFragment.findNavController(this).navigate(R.id.nav_symptom);
        } else if (id == R.id.button_fine) {
            Intent serviceIntent = new Intent(getContext(), GpsLoggingService.class);
            serviceIntent.putExtra(IntentConstants.GOOD, true);
            getActivity().startService(serviceIntent);
        }
    }

    private void changeMapType() {
        googleMap.setMapType(googleMap.getMapType() == MAP_TYPE_SATELLITE ? MAP_TYPE_NORMAL : MAP_TYPE_SATELLITE);
        btnSatellite.setImageResource(googleMap.getMapType() == MAP_TYPE_SATELLITE ? R.drawable.ic_satellite_off
                : R.drawable.ic_satellite_on);
        DbHelper.get().saveValue("mapType", Integer.toString(googleMap.getMapType()));
    }

    private void currentLocation() {
        waitForCurrentLocation = true;
        centerLocation = true;
        if (GoblobLocationManager.getInstance().getCurrentLocationInfo() != null) {
            onEventMainThread(new ServiceEvents.LocationUpdate(GoblobLocationManager.getInstance().getCurrentLocationInfo()));
        }
        if (GoblobLocationManager.getInstance().checkLocationActive("currentLocation")) {
            Intent serviceIntent = new Intent(getContext(), GpsLoggingService.class);
            serviceIntent.putExtra(IntentConstants.LOG_ONCE, true);
            getActivity().startService(serviceIntent);
        }
    }

    @Override
    public void onWindowShowStarted(@NonNull InfoWindow infoWindow) {

    }

    @Override
    public void onWindowShown(@NonNull InfoWindow infoWindow) {

    }

    @Override
    public void onWindowHideStarted(@NonNull InfoWindow infoWindow) {

    }

    @Override
    public void onWindowHidden(@NonNull InfoWindow infoWindow) {
        if (infoWindow.getWindowFragment() instanceof FormFragmentProfileInfoContents) {
            ((FormFragmentProfileInfoContents) infoWindow.getWindowFragment()).getCaseClusterItem().setInfoWindowShow(false);
        }
    }
}
