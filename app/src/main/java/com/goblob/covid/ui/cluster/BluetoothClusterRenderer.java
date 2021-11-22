package com.goblob.covid.ui.cluster;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import com.goblob.covid.R;
import com.goblob.covid.ui.map.MapFragment;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

public class BluetoothClusterRenderer extends DefaultClusterRenderer<ClusterItem> {

    public BluetoothClusterRenderer(MapFragment mapFragment, GoogleMap map, ClusterManager<ClusterItem> clusterManager) {
        super(mapFragment.getContext(), map, clusterManager);
    }

    @Override
    protected void onBeforeClusterRendered(Cluster<ClusterItem> cluster, MarkerOptions options) {
        super.onBeforeClusterRendered(cluster, options);
        for (ClusterItem profileClusterItem : cluster.getItems()) {
            ((CovidClusterItem)profileClusterItem).removePolyline();
        }
    }

    @Override
    protected void onBeforeClusterItemRendered(final ClusterItem caseClusterItem, MarkerOptions options) {
        super.onBeforeClusterItemRendered(caseClusterItem, options);
    }

    @Override
    protected void onClusterItemRendered(ClusterItem clusterItem, Marker marker) {
        super.onClusterItemRendered(clusterItem, marker);
    }

    private static Drawable getClusterIcon(Context context, String res9patchName) {
        return ContextCompat.getDrawable(context, R.drawable.app_logo);
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster cluster) {
        return cluster.getSize() > 1;
    }

}
