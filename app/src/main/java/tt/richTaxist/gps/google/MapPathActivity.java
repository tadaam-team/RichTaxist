package tt.richTaxist.gps.google;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
//import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
//import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import java.util.ArrayList;
import java.util.List;
import tt.richTaxist.R;
import tt.richTaxist.gps.Coordinates;
import tt.richTaxist.gps.MapFragment;

public class MapPathActivity extends Fragment implements MapFragment {
    private static final String LOG_TAG = "Google Map fragment";
    GoogleMap mMapController;
    List<Coordinates> lastCoordsList;
    LinearLayout mView;

    public void showPath(List<Coordinates> coords){
        lastCoordsList = coords;
        if (mMapController == null) return;
        if (coords.size() == 0) return;

        double startLat = coords.get(0).getLat();
        double startLon = coords.get(0).getLon();
        double endLat = coords.get(coords.size() - 1).getLat();
        double endLon = coords.get(coords.size() - 1).getLon();

        double maxLat = -1000;
        double minLat = 1000;
        double maxLon = -1000;
        double minLon = 1000;
        List<LatLng> geoPointList = new ArrayList<>();
        for (int i = 0; i < coords.size(); i++) {
            Coordinates coordinates = coords.get(i);
            double lat = coordinates.getLat(), lon = coordinates.getLon();
            if (lat == 0d || lon == 0d) continue;
            geoPointList.add(new LatLng(lat, lon));
            if (maxLat < lat) maxLat = lat;
            if (maxLon < lon) maxLon = lon;
            if (minLat > lat) minLat = lat;
            if (minLon > lon) minLon = lon;
        }
        Resources res = getResources();

        Log.d(LOG_TAG,"minLat " + String.valueOf(minLat));
        Log.d(LOG_TAG,"maxLat " + String.valueOf(maxLat));
        Log.d(LOG_TAG,"minLon " + String.valueOf(minLon));
        Log.d(LOG_TAG, "maxLon " + String.valueOf(maxLon));

        mMapController.clear();
        mMapController.setMyLocationEnabled(false);
        //mMapController.addGroundOverlay(new GroundOverlayOptions());
        mMapController.addMarker(new MarkerOptions().position(new LatLng(startLat, startLon)).title(getString(R.string.rangeStart)));
        mMapController.addMarker(new MarkerOptions().position(new LatLng(endLat, endLon)).title(getString(R.string.rangeEnd)));

        PolylineOptions polylineOptions = new PolylineOptions().addAll(geoPointList);
        mMapController.addPolyline(polylineOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(
                new LatLngBounds(new LatLng(minLat, minLon), new LatLng(maxLat, maxLon)), 100);
        mMapController.animateCamera(cameraUpdate);

        // Add the layer to the map
        /*mMapController.getOverlayManager().addOverlay(mOverlay);

        OverlayRect overlayRect = new OverlayRect(mMapController,geoPointList);
        mMapController.getOverlayManager().addOverlay(overlayRect);
        mMapController.setZoomToSpan(maxLat - minLat, maxLon - minLon);
        mMapController.setPositionAnimationTo(new GeoPoint((maxLat + minLat) / 2, (maxLon + minLon) / 2));*/
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.gps_google_path, container, false);
        //final MapView mapView = (MapView) rootView.findViewById(R.id.map);
        //mapView.showBuiltInScreenButtons(true);
        //final MapFragment mapView = (MapFragment) getFragmentManager()
        //        .findFragmentById(R.id.map);
        final com.google.android.gms.maps.MapFragment mapView = getMapFragment();
        mMapController = mapView.getMap();

        showPath(lastCoordsList);
        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private com.google.android.gms.maps.MapFragment getMapFragment() {
        FragmentManager fm = null;

        Log.d(LOG_TAG, "sdk: " + Build.VERSION.SDK_INT);
        Log.d(LOG_TAG, "release: " + Build.VERSION.RELEASE);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Log.d(LOG_TAG, "using getFragmentManager");
            fm = getFragmentManager();
        } else {
            Log.d(LOG_TAG, "using getChildFragmentManager");
            fm = getChildFragmentManager();
        }

        return (com.google.android.gms.maps.MapFragment) fm.findFragmentById(R.id.map);
    }
}