package com.goblob.covid.ui.notification;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.goblob.covid.R;
import com.goblob.covid.dagger.Injectable;
import com.goblob.covid.data.dao.GetCallback;
import com.goblob.covid.data.dao.factory.DAOFactory;
import com.goblob.covid.data.dao.model.Notification;
import com.goblob.covid.notification.NotificationManager;
import com.goblob.covid.ui.notification.adapter.NotificationsRecyclerViewAdapter;
import com.yarolegovich.mp.MaterialPreferenceCategory;

import java.util.ArrayList;
import java.util.List;

public class NotificationFragment extends Fragment {

    private NotificationViewModel notificationViewModel;
    private DAOFactory daoFactory;

    private RecyclerView recentRecyclerView, notificationsRecyclerView;
    private MaterialPreferenceCategory recentCard, notificationsCard;
    private List recentNotifications, oldNotifications;
    private NotificationsRecyclerViewAdapter recentListAdapter, notificationsListAdapter;
    private View root;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        ((AppCompatActivity)getActivity()).getSupportActionBar().show();

        notificationViewModel =
                ViewModelProviders.of(this).get(NotificationViewModel.class);
        root = inflater.inflate(R.layout.activity_notifications, container, false);
        /*final TextView textView = root.findViewById(R.id.text_slideshow);
        notificationViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });*/

        initRecentRecyclerView();
        // loadNotifications();

        daoFactory = Injectable.get().getDaoFactory();
        return root;
    }

    private void initRecentRecyclerView() {
         // recentCard = root.findViewById(R.id.recentCard);
        recentRecyclerView = (RecyclerView) root.findViewById(R.id.recent_recycler_view);
        recentNotifications = new ArrayList<>();

        // Configuration of the RecyclerView and Adapters to manage the Image Gallery
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(root.getContext());
        recentRecyclerView.setLayoutManager(mLayoutManager);
        recentRecyclerView.setItemAnimator(new DefaultItemAnimator());

        recentListAdapter = new NotificationsRecyclerViewAdapter(recentNotifications);

        recentListAdapter.setListener(new NotificationsRecyclerViewAdapter.Listener() {
            @Override
            public void onImageClickListener(Notification notification) {
                // showProfile(profileBasic);
            }

            @Override
            public void onInfoClickListener(Notification notification) {
                notification.setStatus("READ");
                NotificationManager.get().updateNotification(notification);
                recentListAdapter.notifyDataSetChanged();
            }

        });

        recentRecyclerView.setAdapter(recentListAdapter);

        /*recentNotifications.add("Noel");
        recentNotifications.add("Pepe");
        recentNotifications.add("Pepe");
        recentNotifications.add("Pepe");
        recentListAdapter.notifyDataSetChanged();*/

        loadNotifications();
    }

    protected void loadNotifications() {
        final int[] count = {0};
        try {
            NotificationManager.get().getLastNotifications(100, new GetCallback<List<Notification>>() {
                @Override
                public void done(final List<Notification> notificationList, Exception e) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (notificationList.size() > 0) {
                                // recentCard.setVisibility(View.VISIBLE);
                                for (Notification notification : notificationList) {
                                    recentNotifications.add(notification);
                                    count[0]++;
                                }
                                recentListAdapter.notifyDataSetChanged();
                            } else {
                                // recentCard.setVisibility(View.GONE);
                            }
                        }
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
