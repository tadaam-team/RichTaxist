package tt.richCabman.activities;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import tt.richCabman.util.Constants;
import tt.richCabman.R;

public class MapMyLocationChangeActivity extends FragmentActivity {
    //Called when the activity is first created.
    SupportMapFragment mapFragment;
    GoogleMap map;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.appName);

        setContentView(R.layout.gps_google_loc);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        map = mapFragment.getMap();
        if (map == null) {
            finish();
            return;
        }
        init();
    }

    private void init() {
        map.setMyLocationEnabled(true);
        map.getUiSettings().setAllGesturesEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);

        map.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                Log.d(Constants.LOG_TAG, "onMapLoaded");
                Location location = map.getMyLocation();

                if (location != null) {
                    LatLng myLocation = new LatLng(location.getLatitude(),
                            location.getLongitude());
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation,
                            14));
                }
             }
        });

    }

    /*
    @Override
    public void onMyLocationChange(MyLocationItem myLocationItem) {
        double speed = myLocationItem.getSpeed();
        Log.d(Constants.LOG_TAG, "speed: "+String.valueOf(speed));
        if (speed==0.0) mMapController.setZoomCurrent(13); else mMapController.setZoomCurrent(13-((float) speed)/4);

    }
    */
}