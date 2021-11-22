package com.goblob.covid.ui.cluster;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.goblob.covid.R;
import com.goblob.covid.ui.map.MapFragment;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

import net.grobas.view.PolygonImageView;

public class CaseClusterRenderer extends DefaultClusterRenderer<ClusterItem> {
    private MapFragment mapFragment;
    private TextView casesT;
    private PolygonImageView mClusterImageView;
    private IconGenerator mClusterIconGenerator;

    public CaseClusterRenderer(MapFragment mapFragment, GoogleMap map, ClusterManager<ClusterItem> clusterManager) {
        super(mapFragment.getContext(), map, clusterManager);
        this.mapFragment = mapFragment;
        mClusterIconGenerator = new IconGenerator(mapFragment.getContext());

        View multiProfile = LayoutInflater.from(mapFragment.getContext()).inflate(R.layout.multi_profile, null);
        mClusterIconGenerator.setContentView(multiProfile);

        mClusterImageView = multiProfile.findViewById(R.id.image);

        casesT = multiProfile.findViewById(R.id.cases);
    }

    @Override
    protected void onBeforeClusterRendered(Cluster<ClusterItem> cluster, MarkerOptions options) {
        super.onBeforeClusterRendered(cluster, options);

        for (ClusterItem profileClusterItem : cluster.getItems()) {
            ((CovidClusterItem)profileClusterItem).removePolyline();
        }

        mClusterImageView.setVertices(0);

        Object[] items = cluster.getItems().toArray();

        int cases = 0;
        for (int i = 0; i < items.length; i++) {
            cases += ((CaseClusterItem)items[i]).getCasesConfirmed();
        }

        casesT.setText(String.valueOf(cases));

        Bitmap sizeIcon = mClusterIconGenerator.makeIcon(String.valueOf(cases));
        options.icon(BitmapDescriptorFactory.fromBitmap(sizeIcon));
    }

    @Override
    protected void onBeforeClusterItemRendered(final ClusterItem caseClusterItem, MarkerOptions options) {
        super.onBeforeClusterItemRendered(caseClusterItem, options);
        mClusterImageView.setVertices(0);
        casesT.setText(String.valueOf(((CaseClusterItem)caseClusterItem).getCasesConfirmed()));
        mClusterIconGenerator.setBackground(mClusterImageView.getBackground());
        Bitmap sizeIcon = mClusterIconGenerator.makeIcon(String.valueOf(((CaseClusterItem)caseClusterItem).getCasesConfirmed()));
        options.icon(BitmapDescriptorFactory.fromBitmap(sizeIcon));
    }

    @Override
    protected void onClusterItemRendered(ClusterItem clusterItem, Marker marker) {
        super.onClusterItemRendered(clusterItem, marker);
        ((CaseClusterItem)clusterItem).setMarker(marker);
        marker.setTag(clusterItem);
        marker.setDraggable(true);
        marker.setTitle("");
        if (((CaseClusterItem)clusterItem).getInfoWindowShow()) {
            mapFragment.showInfoWindow((CaseClusterItem)clusterItem);
        }
    }

    private static Drawable getClusterIcon(Context context, String res9patchName) {
        return ContextCompat.getDrawable(context, R.drawable.app_logo);
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster cluster) {
        return cluster.getSize() > 5;
    }

}
