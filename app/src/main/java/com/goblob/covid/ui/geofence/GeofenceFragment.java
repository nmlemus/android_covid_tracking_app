package com.goblob.covid.ui.geofence;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.goblob.covid.R;
import com.goblob.covid.dagger.Injectable;
import com.goblob.covid.data.dao.factory.DAOFactory;
import com.goblob.covid.data.dao.model.Notification;
import com.goblob.covid.notification.NotificationManager;
import com.goblob.covid.ui.notification.NotificationViewModel;
import com.goblob.covid.ui.notification.adapter.NotificationsRecyclerViewAdapter;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.yarolegovich.mp.MaterialPreferenceCategory;

import java.util.ArrayList;
import java.util.List;

public class GeofenceFragment extends Fragment implements View.OnClickListener {

    private static final int LOCATION_PICKER = 125;

    private NotificationViewModel notificationViewModel;
    private DAOFactory daoFactory;

    private RecyclerView recentRecyclerView, notificationsRecyclerView;
    private MaterialPreferenceCategory recentCard, notificationsCard;
    private List geoFences, oldNotifications;
    private GeofenceRecyclerViewAdapter recentListAdapter, notificationsListAdapter;
    private View root;
    private Button addHome, addWork;
    private LinearLayout homeLayout, workLayout;
    View convertView;
    ImageView imageView;
    TextView homeTitle, homeAddress, homeTimestamp, workTitle, workAddress, workTimestamp;
    CardView notificationCard;
    View infoLayout;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        ((AppCompatActivity)getActivity()).getSupportActionBar().show();

        notificationViewModel =
                ViewModelProviders.of(this).get(NotificationViewModel.class);
        root = inflater.inflate(R.layout.activity_geofence, container, false);

        homeLayout = root.findViewById(R.id.home_layout);
        homeLayout.setOnClickListener(this);

        workLayout = root.findViewById(R.id.work_layout);
        workLayout.setOnClickListener(this);

        homeTitle = root.findViewById(R.id.home_title);
        homeAddress = root.findViewById(R.id.home_address);
        homeTimestamp = root.findViewById(R.id.home_date);
        imageView = (ImageView) root.findViewById(R.id.image);
        // notificationCard = convertView.findViewById(R.id.notification_card);
        infoLayout = root.findViewById(R.id.info_layout);

        workTitle = root.findViewById(R.id.work_title);
        workAddress = root.findViewById(R.id.work_address);
        workTimestamp = root.findViewById(R.id.work_date);
        /*final TextView textView = root.findViewById(R.id.text_slideshow);
        notificationViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });*/

        GeofenceDialogFragment geofenceDialogFragment = GeofenceDialogFragment.newInstance();

        loadGeofences();
        // loadNotifications();

        daoFactory = Injectable.get().getDaoFactory();
        return root;
    }


    protected void loadGeofences() {
        final int[] count = {0};
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Geofence");
        query.fromPin("geofence");
        query.fromLocalDatastore();
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                for (ParseObject object : objects) {
                    if (object.get("type").toString().equalsIgnoreCase("Home")) {
                        homeAddress.setText((CharSequence) (object.get("name").toString() + " " + object.get("address")));
                    } else if (object.get("type").toString().equalsIgnoreCase("Work")) {
                        workAddress.setText((object.get("name").toString() + " " + object.get("address")));
                    }
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.home_layout) {
            createGeofence("Home");
        } else if (v.getId() == R.id.work_layout) {
            createGeofence("Work");
        }
    }

    private void createGeofence(String placeType) {
        Intent intent = new Intent();
        intent.setClassName(com.goblob.covid.BuildConfig.APPLICATION_ID, "com.goblob.covid.geo.LocationPicker");
        intent.setPackage(getActivity().getBaseContext().getPackageName());
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        /*try {
            if (currentProfile != null && currentProfile.getCurrentLatitude() != -1) {
                intent.putExtra("LATITUDE", currentProfile.getCurrentLatitude());
                intent.putExtra("LONGITUDE", currentProfile.getCurrentLongitude());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        intent.putExtra("placeType", placeType);
        startActivityForResult(intent, LOCATION_PICKER);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == LOCATION_PICKER) {
                final ParseObject geofence = new ParseObject("Geofence");
                ParseGeoPoint geoPoint = new ParseGeoPoint(data.getDoubleExtra("latitude", 0), data.getDoubleExtra("longitude", 0));
                geofence.put("location", geoPoint);
                geofence.put("name", data.getStringExtra("name"));
                geofence.put("address", data.getStringExtra("address"));
                geofence.put("placeId", data.getStringExtra("placeId"));
                geofence.put("type", data.getStringExtra("placeType"));

                if (data.getStringExtra("placeType").equalsIgnoreCase("Home")) {
                    homeAddress.setText(data.getStringExtra("address"));
                } else if (data.getStringExtra("placeType").equalsIgnoreCase("Work")) {
                    workAddress.setText(data.getStringExtra("address"));
                }

                geofence.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            geofence.pinInBackground("geofence");
                        } else {
                            Log.d("Error: ", e.getMessage());
                        }
                    }
                });
            }
        }
    }
}