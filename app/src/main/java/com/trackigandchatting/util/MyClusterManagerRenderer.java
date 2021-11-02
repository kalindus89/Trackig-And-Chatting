package com.trackigandchatting.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.trackigandchatting.R;
import com.trackigandchatting.models.ClusterMaker;

public class MyClusterManagerRenderer extends DefaultClusterRenderer<ClusterMaker> {

    private final IconGenerator iconGenerator;
    private final ImageView imageView;
    private final int makerWidth;
    private final int makerHeight;

    public MyClusterManagerRenderer(Context context, GoogleMap map, ClusterManager<ClusterMaker> clusterManager,
                                    IconGenerator iconGenerator, ImageView imageView, int makerWidth, int makerHeight) {
        super(context, map, clusterManager);
        this.iconGenerator = iconGenerator;
        this.imageView = imageView;
        this.makerWidth = makerWidth;
        this.makerHeight = makerHeight;

        iconGenerator= new IconGenerator(context.getApplicationContext());
        imageView= new ImageView(context.getApplicationContext());
        makerWidth=(int) context.getResources().getDimension(R.dimen.custom_marker_image);
        makerHeight=(int) context.getResources().getDimension(R.dimen.custom_marker_image);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(makerWidth,makerHeight));
        int padding=(int) context.getResources().getDimension(R.dimen.custom_marker_padding);
        imageView.setPadding(padding,padding,padding,padding);
        iconGenerator.setContentView(imageView);
    }

    @Override
    protected void onBeforeClusterItemRendered(@NonNull ClusterMaker item, @NonNull MarkerOptions markerOptions) {

        imageView.setImageResource(item.getIconPicture());
        Bitmap icon= iconGenerator.makeIcon();
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(item.getTitle());
    }

    @Override
    protected boolean shouldRenderAsCluster(@NonNull Cluster<ClusterMaker> cluster) {
        return  false;
    }
}
