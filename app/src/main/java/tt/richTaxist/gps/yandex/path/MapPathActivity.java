package tt.richTaxist.gps.yandex.path;


import android.app.Fragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import java.util.ArrayList;
import java.util.List;
import ru.yandex.yandexmapkit.MapController;
import ru.yandex.yandexmapkit.MapView;
import ru.yandex.yandexmapkit.overlay.Overlay;
import ru.yandex.yandexmapkit.overlay.OverlayItem;
import ru.yandex.yandexmapkit.overlay.balloon.BalloonItem;
import ru.yandex.yandexmapkit.overlay.location.MyLocationItem;
import ru.yandex.yandexmapkit.overlay.location.OnMyLocationListener;
import ru.yandex.yandexmapkit.utils.GeoPoint;
import tt.richTaxist.R;
import tt.richTaxist.gps.Coordinates;
import tt.richTaxist.gps.MapFragment;

/**
 * MapLayers.java
 * This file is a part of the Yandex Map Kit.
 * Version for Android  2012 YANDEX
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://legal.yandex.ru/mapkit/
 */

public class MapPathActivity extends Fragment implements OnMyLocationListener, MapFragment {
    private static final String LOG_TAG = "Map fragment";
    MapController mMapController;
    List<Coordinates> lastCoordsList;

    LinearLayout mView;

    public void showPath(List<Coordinates> coords){
        lastCoordsList = coords;
        if (mMapController == null) return;
        if (coords.size() == 0) return;

        double startLat = coords.get(0).getLat();
        double startLon = coords.get(0).getLon();
        double endLat = coords.get(coords.size() - 1).getLat();
        double endLon = coords.get(coords.size() - 1).getLon();;

        double maxLat = -1000;
        double minLat = 1000;
        double maxLon = -1000;
        double minLon = 1000;
        List<GeoPoint> geoPointList = new ArrayList<>();
        for (int i = 0; i < coords.size(); i++) {
            Coordinates coordinates = coords.get(i);
            double lat = coordinates.getLat(), lon = coordinates.getLon();
            if (lat == 0d || lon == 0d) continue;
            geoPointList.add(new GeoPoint(lat, lon));
            if (maxLat < lat) maxLat = lat;
            if (maxLon < lon) maxLon = lon;
            if (minLat > lat) minLat = lat;
            if (minLon > lon) minLon = lon;
        }
        Resources res = getResources();

        Log.d(LOG_TAG,"minLat " + String.valueOf(minLat));
        Log.d(LOG_TAG,"maxLat " + String.valueOf(maxLat));
        Log.d(LOG_TAG,"minLon " + String.valueOf(minLon));
        Log.d(LOG_TAG,"maxLon " + String.valueOf(maxLon));

        for (int i = 0; i < mMapController.getOverlayManager().getOverlays().size(); i++) {
            Object o =  mMapController.getOverlayManager().getOverlays().get(i);
            mMapController.getOverlayManager().removeOverlay((Overlay)o);
        }

        // Create a layer of objects for the map
        Overlay mOverlay = new Overlay(mMapController);

        OverlayItem startPoint = new OverlayItem(new GeoPoint(startLat , startLon), res.getDrawable(R.drawable.point));
        // Create the balloon model for the object
        BalloonItem balloonStart = new BalloonItem(getActivity(),startPoint.getGeoPoint());
        balloonStart.setText(getString(R.string.rangeStart));
        // Add the balloon model to the object
        startPoint.setBalloonItem(balloonStart);
        // Add the object to the layer
        mOverlay.addOverlayItem(startPoint);

        // Add the layer to the map
        mMapController.getOverlayManager().addOverlay(mOverlay);

        OverlayItem endPoint = new OverlayItem(new GeoPoint(endLat , endLon), res.getDrawable(R.drawable.point));
        // Create the balloon model for the object
        BalloonItem balloonEnd = new BalloonItem(getActivity(),startPoint.getGeoPoint());
        balloonStart.setText(getString(R.string.rangeEnd));
        // Add the balloon model to the object
        startPoint.setBalloonItem(balloonEnd);
        // Add the object to the layer
        mOverlay.addOverlayItem(endPoint);

        // Add the layer to the map
        mMapController.getOverlayManager().addOverlay(mOverlay);

        OverlayRect overlayRect = new OverlayRect(mMapController,geoPointList);

        mMapController.getOverlayManager().addOverlay(overlayRect);
        mMapController.setZoomToSpan(maxLat - minLat, maxLon - minLon);
        mMapController.setPositionAnimationTo(new GeoPoint((maxLat + minLat) / 2, (maxLon + minLon) / 2));
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.gps_yandex_path, null);

        final MapView mapView = (MapView) v.findViewById(R.id.map);
        mapView.showBuiltInScreenButtons(true);

        mMapController = mapView.getMapController();

         //add listener
        //mMapController.getOverlayManager().getMyLocation().addMyLocationListener(this);
        showPath(lastCoordsList);
        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setTitle(R.string.appName);
        //setContentView(R.layout.gps_yandex_path);
    }

    @Override
    public void onMyLocationChange(MyLocationItem myLocationItem) {
    }
}