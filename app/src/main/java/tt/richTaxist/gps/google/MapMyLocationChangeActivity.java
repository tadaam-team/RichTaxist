package tt.richTaxist.gps.google;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;

import tt.richTaxist.R;


public class MapMyLocationChangeActivity extends FragmentActivity {
    private static final String LOG_TAG = "MMLCA";
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
    }

    /*
    @Override
    public void onMyLocationChange(MyLocationItem myLocationItem) {
        double speed = myLocationItem.getSpeed();
        Log.d(LOG_TAG, "speed: "+String.valueOf(speed));
        if (speed==0.0) mMapController.setZoomCurrent(13); else mMapController.setZoomCurrent(13-((float) speed)/4);

    }
    */
}