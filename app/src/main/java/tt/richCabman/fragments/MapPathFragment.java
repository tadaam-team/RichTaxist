package tt.richCabman.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import java.util.ArrayList;
import java.util.List;
import tt.richCabman.R;
import tt.richCabman.model.Coordinates;
import tt.richCabman.util.Logger;

import com.google.android.gms.maps.MapFragment;

public class MapPathFragment extends Fragment {
    GoogleMap mMapController;
    List<Coordinates> lastCoordsList;

    public void showPath(List<Coordinates> coords){
        lastCoordsList = coords;
        if (mMapController == null) return;
        if (coords == null || coords.size() == 0) return;

        double startLat = coords.get(0).getLat();
        double startLon = coords.get(0).getLon();
        double endLat   = coords.get(coords.size() - 1).getLat();
        double endLon   = coords.get(coords.size() - 1).getLon();

        double maxLat = 90;//north pole
        double minLat = -90;//south pole
        double maxLon = 180;//eastern hemisphere
        double minLon = -180;//western hemisphere
        List<LatLng> geoPointList = new ArrayList<>();
        for (int i = 0; i < coords.size(); i++) {
            Coordinates coordinates = coords.get(i);
            double lat = coordinates.getLat(), lon = coordinates.getLon();
            if (lat == 0d || lon == 0d) continue;
            geoPointList.add(new LatLng(lat, lon));
            if (maxLat >= lat) maxLat = lat;
            if (minLat <= lat) minLat = lat;
            if (maxLon >= lon) maxLon = lon;
            if (minLon <= lon) minLon = lon;
        }

        Logger.d("maxLat " + String.valueOf(maxLat));
        Logger.d("minLat " + String.valueOf(minLat));
        Logger.d("maxLon " + String.valueOf(maxLon));
        Logger.d("minLon " + String.valueOf(minLon));

        mMapController.clear();
        mMapController.setMyLocationEnabled(true);
        mMapController.getUiSettings().setAllGesturesEnabled(true);
        mMapController.getUiSettings().setZoomControlsEnabled(true);
        mMapController.getUiSettings().setMyLocationButtonEnabled(true);

        mMapController.addMarker(new MarkerOptions().position(new LatLng(startLat, startLon)).title(getString(R.string.rangeStart)));
        mMapController.addMarker(new MarkerOptions().position(new LatLng(endLat, endLon)).title(getString(R.string.rangeEnd)));

        PolylineOptions polylineOptions = new PolylineOptions().addAll(geoPointList);
        mMapController.addPolyline(polylineOptions);

        final LatLngBounds bounds = new LatLngBounds(new LatLng(minLat, minLon), new LatLng(maxLat, maxLon));

        mMapController.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                Logger.d("onMapLoaded");
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 100);
                mMapController.animateCamera(cameraUpdate);
            }
        });
     }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.gps_google_path, container, false);
        final MapFragment mapView = getMapFragment();
        mMapController = mapView.getMap();
        showPath(lastCoordsList);
        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private MapFragment getMapFragment() {
        Logger.d("sdk: " + Build.VERSION.SDK_INT);
        Logger.d("release: " + Build.VERSION.RELEASE);

        FragmentManager fm;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            fm = getFragmentManager();
            Logger.d("using FragmentManager");
        } else {
            fm = getChildFragmentManager();
            Logger.d("using ChildFragmentManager");
        }
        return (MapFragment) fm.findFragmentById(R.id.map);
    }
}