package com.goblob.covid.ui.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.location.Address;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.goblob.covid.R;
import com.goblob.covid.data.dao.model.CorregimientosPA;
import com.goblob.covid.data.dao.model.DailyReportAll;
import com.goblob.covid.utils.TimeUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by edel on 28/09/17.
 */

public class ListResultsRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

    private static final int PROFILE = 1;
    private Context context;
    private List values;
    private List contactListFiltered;

    public ListResultsRecyclerViewAdapter(Context context, List values) {
        this.context = context;
        this.values = values;
        this.contactListFiltered = values;
    }

    public List getContactListFiltered() {
        return contactListFiltered;
    }

    public void swapData(List values) {
        this.values = values;
        this.contactListFiltered = values;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (viewType == PROFILE) {
            View v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.country_layout, viewGroup, false);
            return new ListResultsRecyclerViewAdapter.AddressViewHolder(v);
        }

        return null;
    }

    @Override
    public int getItemViewType(int position) {
        if (contactListFiltered.get(position) instanceof DailyReportAll) {
            return PROFILE;
        }
        if (contactListFiltered.get(position) instanceof CorregimientosPA) {
            return PROFILE;
        }
        return -1;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder customViewHolder, int position) {
        if (position != -1) {
            if (customViewHolder.getItemViewType() == PROFILE) {
                if(contactListFiltered.get(position) instanceof DailyReportAll) {
                    DailyReportAll dailyReport = (DailyReportAll) contactListFiltered.get(position);
                    AddressViewHolder holder = (AddressViewHolder) customViewHolder;

                    String title = dailyReport.getCountry();

                    /*if (object.has("country")) {
                        title = object.getString("country");
                    }*/

                    if (dailyReport.getProvince() != null && !dailyReport.getProvince().equalsIgnoreCase("")) {
                        title += " " + dailyReport.getProvince();
                    }

                    holder.title.setText(title);

                    holder.confirmedValue.setText(Integer.toString(dailyReport.getConfirmed()));
                    holder.recoveredValue.setText(Integer.toString(dailyReport.getRecovered()));
                    holder.deadValue.setText(Integer.toString(dailyReport.getDeaths()));

                    Resources resources = context.getResources();

                    int resourceId = resources.getIdentifier("ic_list_" + dailyReport.getCountryCode().toLowerCase(), "drawable",
                            context.getPackageName());

                    if (resourceId != 0) {
                        holder.imageView.setImageDrawable(AppCompatResources.getDrawable(context, resourceId));
                    } else {
                        holder.imageView.setImageResource(R.drawable.interrogacion);
                    }

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    sdf.setTimeZone(TimeZone.getDefault());

                    holder.reportDateValue.setText(sdf.format(TimeUtil.getLocalTime(dailyReport.getCreatedAt().getTime())));
                } else if(contactListFiltered.get(position) instanceof CorregimientosPA) {
                    CorregimientosPA dailyReport = (CorregimientosPA) contactListFiltered.get(position);
                    AddressViewHolder holder = (AddressViewHolder) customViewHolder;

                    String title = dailyReport.getName();

                    holder.title.setText(title);

                    holder.confirmedValue.setText(Integer.toString(dailyReport.getConfirmed()));
                    //holder.recoveredValue.setText(Integer.toString(dailyReport.getRecovered()));
                    //holder.deadValue.setText(Integer.toString(dailyReport.getDeaths()));

                    Resources resources = context.getResources();

                    int resourceId = resources.getIdentifier("ic_list_" + "pa", "drawable",
                            context.getPackageName());

                    if (resourceId != 0) {
                        holder.imageView.setImageDrawable(AppCompatResources.getDrawable(context, resourceId));
                    } else {
                        holder.imageView.setImageResource(R.drawable.interrogacion);
                    }

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    sdf.setTimeZone(TimeZone.getDefault());

                    holder.reportDateValue.setText(sdf.format(TimeUtil.getLocalTime(dailyReport.getCreatedAt().getTime())));
                }
            }
        }
    }

    private String distanceKm(double distance) {
        if (distance < 1000) {
            return (Math.round(distance * 100) / 100) + " m";
        }

        return (Math.round((distance / 1000) * 100) / 100) + " km";
    }


    private String getFullAddressString(Address address) {
        String fullAddress = "";
        if (address.getFeatureName() != null) {
            fullAddress += address.getFeatureName();
        }
        if (address.getSubLocality() != null && !address.getSubLocality().isEmpty()) {
            fullAddress += ", " + address.getSubLocality();
        }
        if (address.getLocality() != null && !address.getLocality().isEmpty()) {
            fullAddress += ", " + address.getLocality();
        }
        if (address.getCountryName() != null && !address.getCountryName().isEmpty()) {
            fullAddress += ", " + address.getCountryName();
        }
        Log.d("Place id: ", address.toString());
        return address.getAddressLine(0);
    }

    @Override
    public int getItemCount() {
        return contactListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    contactListFiltered = values;
                } else {
                    List filteredList = new ArrayList<>();
                    for (Object row : values) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row instanceof DailyReportAll) {
                            if (((DailyReportAll) row).getSearchName().toLowerCase().contains(charString.toLowerCase())) {
                                filteredList.add(row);
                            }
                        } else if (row instanceof CorregimientosPA) {
                            if (((CorregimientosPA) row).getSearchName().toLowerCase().contains(charString.toLowerCase())) {
                                filteredList.add(row);
                            }
                        }
                    }

                    contactListFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = contactListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                contactListFiltered = (ArrayList) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    class AddressViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView title;
        TextView reportDateValue;
        TextView confirmedValue;
        TextView recoveredValue;
        TextView deadValue;

        public AddressViewHolder(View v) {
            super(v);
            imageView = v.findViewById(R.id.item_image);
            title = v.findViewById(R.id.title);
            reportDateValue = v.findViewById(R.id.reportDateValue);
            confirmedValue = v.findViewById(R.id.confirmedValue);
            recoveredValue = v.findViewById(R.id.recoveredValue);
            deadValue = v.findViewById(R.id.deadValue);
        }
    }
}
