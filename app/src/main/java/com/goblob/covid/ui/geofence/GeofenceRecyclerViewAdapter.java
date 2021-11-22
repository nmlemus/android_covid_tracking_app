package com.goblob.covid.ui.geofence;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.goblob.covid.R;
import com.goblob.covid.dagger.Injectable;
import com.goblob.covid.data.dao.factory.DAOFactory;
import com.goblob.covid.data.dao.model.Notification;
import com.goblob.covid.ui.notification.adapter.NotificationsRecyclerViewAdapter;
import com.parse.ParseObject;
import com.squareup.picasso.Picasso;

import java.util.List;

public class GeofenceRecyclerViewAdapter extends RecyclerView.Adapter<GeofenceRecyclerViewAdapter.GeofenceViewHolder> {

    // private final ClickListener clickListener;
    private List values;
    private NotificationsRecyclerViewAdapter.Listener listener;
    private DAOFactory daoFactory;

    public GeofenceRecyclerViewAdapter(List values) {
        this.values = values;
        // this.clickListener = clickListener;
        daoFactory = Injectable.get().getDaoFactory();
    }

    @NonNull
    @Override
    public GeofenceRecyclerViewAdapter.GeofenceViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.geofence_item, viewGroup, false);
        return new GeofenceRecyclerViewAdapter.GeofenceViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull GeofenceRecyclerViewAdapter.GeofenceViewHolder customViewHolder, final int position) {
        final GeofenceRecyclerViewAdapter.GeofenceViewHolder holder = (GeofenceRecyclerViewAdapter.GeofenceViewHolder) customViewHolder;

        ParseObject parseObject = null;

        /*holder.convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickListener.onClick(view, position);
            }
        });

        holder.convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                clickListener.onLongClick(view, position);
                return true;
            }
        });*/

        parseObject = (ParseObject) values.get(position);

        if (parseObject != null) {



           /* if (notification.getStatus().equals("UNREAD")) {
                holder.notificationCard.setCardBackgroundColor(holder.imageView.getContext().getResources().getColor(R.color.feed_item_border));
            } else {
                holder.notificationCard.setCardBackgroundColor(holder.imageView.getContext().getResources().getColor(R.color.white));
            }*/

            holder.title.setText((CharSequence) parseObject.get("name"));

            holder.address.setText((CharSequence) parseObject.get("address"));



        }
    }

    public void setListener(NotificationsRecyclerViewAdapter.Listener listener) {
        this.listener = listener;
    }

    public Notification getNotification(int position) {
        if (position != -1 && position < values.size()) {
            return (Notification) values.get(position);
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return values.size();
    }


    class GeofenceViewHolder extends RecyclerView.ViewHolder {
        View convertView;
        ImageView imageView;
        TextView title, address, timestamp;
        CardView notificationCard;
        View infoLayout;
        LinearLayout friendRequestLayout;
        Button buttonReject, buttonAccept;

        public GeofenceViewHolder(View convertView) {
            super(convertView);

            this.convertView = convertView;
            //setIsRecyclable(false);

            title = convertView.findViewById(R.id.title);
            address = convertView.findViewById(R.id.address);
            timestamp = convertView.findViewById(R.id.date);
            imageView = (ImageView) convertView.findViewById(R.id.image);
            // notificationCard = convertView.findViewById(R.id.notification_card);
            infoLayout = convertView.findViewById(R.id.info_layout);
        }
    }

    public interface Listener {
        void onImageClickListener(Notification hItem);

        void onInfoClickListener(Notification hItem);
    }
}