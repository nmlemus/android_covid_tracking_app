package com.goblob.covid.ui.notification.adapter;

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
import com.squareup.picasso.Picasso;

import java.util.List;

public class NotificationsRecyclerViewAdapter extends RecyclerView.Adapter<NotificationsRecyclerViewAdapter.NotificationsViewHolder> {

    // private final ClickListener clickListener;
    private List values;
    private Listener listener;
    private DAOFactory daoFactory;

    public NotificationsRecyclerViewAdapter(List values) {
        this.values = values;
        // this.clickListener = clickListener;
        daoFactory = Injectable.get().getDaoFactory();
    }

    @NonNull
    @Override
    public NotificationsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.feed_item, viewGroup, false);
        return new NotificationsRecyclerViewAdapter.NotificationsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationsViewHolder customViewHolder, final int position) {
        final NotificationsViewHolder holder = (NotificationsViewHolder) customViewHolder;

        Notification notification = null;

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

        notification = (Notification) values.get(position);

        if (notification != null) {


            Picasso.get()
                    .load("https://pbs.twimg.com/profile_images/1240440447090163712/Qi1qqVh2.jpg")
                    .placeholder(holder.imageView.getContext().getDrawable(R.drawable.app_logo))
                    .centerCrop()
                    .resize(50, 50)
                    .into(holder.imageView);

           /* if (notification.getStatus().equals("UNREAD")) {
                holder.notificationCard.setCardBackgroundColor(holder.imageView.getContext().getResources().getColor(R.color.feed_item_border));
            } else {
                holder.notificationCard.setCardBackgroundColor(holder.imageView.getContext().getResources().getColor(R.color.white));
            }*/

            holder.title.setText(notification.getName());

            holder.message.setText(notification.getMessageText());

            holder.timestamp.setText(notification.getSent_at());

            holder.url.setText(notification.getRemoteFile());

            if (notification.getLocalFile() != null && notification.getLocalFile().contains("http")) {
                holder.imageNews.setVisibility(View.VISIBLE);
                Picasso.get()
                        .load(notification.getLocalFile())
                        .placeholder(holder.imageView.getContext().getDrawable(R.drawable.app_logo))
                        .into(holder.imageNews);
            }

            final Notification finalNotification = notification;
            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onImageClickListener(finalNotification);
                    }
                }
            });

            holder.infoLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onInfoClickListener(finalNotification);
                    }
                }
            });


        }
    }

    public void setListener(Listener listener) {
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


    class NotificationsViewHolder extends RecyclerView.ViewHolder {
        View convertView;
        ImageView imageView, imageNews;
        TextView title, message, timestamp, url;
        CardView notificationCard;
        View infoLayout;
        LinearLayout friendRequestLayout;
        Button buttonReject, buttonAccept;

        public NotificationsViewHolder(View convertView) {
            super(convertView);

            this.convertView = convertView;
            //setIsRecyclable(false);

            title = convertView.findViewById(R.id.title);
            message = convertView.findViewById(R.id.message);
            timestamp = convertView.findViewById(R.id.timestamp);
            url = convertView.findViewById(R.id.url);
            imageView = (ImageView) convertView.findViewById(R.id.image);
            imageNews = (ImageView) convertView.findViewById(R.id.image_news);
            // notificationCard = convertView.findViewById(R.id.notification_card);
            infoLayout = convertView.findViewById(R.id.info_layout);
        }
    }

    public interface Listener {
        void onImageClickListener(Notification hItem);

        void onInfoClickListener(Notification hItem);
    }
}