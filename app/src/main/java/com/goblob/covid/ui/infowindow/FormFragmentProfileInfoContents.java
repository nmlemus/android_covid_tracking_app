package com.goblob.covid.ui.infowindow;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;

import com.goblob.covid.R;
import com.goblob.covid.ui.cluster.CaseClusterItem;
import com.goblob.covid.utils.TimeUtil;
import com.google.android.gms.maps.model.Marker;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by nmlemus on 21/12/17.
 */

public class FormFragmentProfileInfoContents extends Fragment implements View.OnClickListener {

    private CaseClusterItem caseClusterItem;
    private LifecycleOwner lifecycleOwner;
    private Marker marker;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.windowlayout, container, false);
        return infoContents1(view);
    }

    public View infoContents1(View v) {
        ImageView imageView = v.findViewById(R.id.item_image);
        imageView.setOnClickListener(this);

        Resources resources = getContext().getResources();

        int resourceId = resources.getIdentifier("ic_list_"+caseClusterItem.getCountryCode().toLowerCase(), "drawable",
                getContext().getPackageName());

        if (resourceId != 0) {
            imageView.setImageDrawable(AppCompatResources.getDrawable(getContext(), resourceId));
        } else {
            imageView.setImageResource(R.drawable.interrogacion);
        }

        // Getting reference to the TextView to set latitude
        TextView title = v.findViewById(R.id.title);

        TextView reportDateValue = v.findViewById(R.id.reportDateValue);

        TextView confirmedValue = v.findViewById(R.id.confirmedValue);
        TextView recoveredValue = v.findViewById(R.id.recoveredValue);

        TextView deadValue = v.findViewById(R.id.deadValue);

        title.setText(caseClusterItem.getTitle());

        confirmedValue.setText(Integer.toString(caseClusterItem.getCasesConfirmed()));
        recoveredValue.setText(Integer.toString(caseClusterItem.getCasesRecovered()));
        deadValue.setText(Integer.toString(caseClusterItem.getCasesDeaths()));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        sdf.setTimeZone(TimeZone.getDefault());

        reportDateValue.setText(sdf.format(TimeUtil.getLocalTime(caseClusterItem.getCreatedAt().getTime())));

        return v;
    }

    public void setCaseClusterItem(CaseClusterItem caseClusterItem) {
        this.caseClusterItem = caseClusterItem;
    }

    public CaseClusterItem getCaseClusterItem() {
        return caseClusterItem;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.item_image){
            if (caseClusterItem != null) {

            }
        }
    }

    public void setLifecycleOwner(LifecycleOwner lifecycleOwner) {
        this.lifecycleOwner = lifecycleOwner;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }
}