package tt.richTaxist.gps.yandex.mylocation;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import ru.yandex.yandexmapkit.MapController;
import ru.yandex.yandexmapkit.MapView;
import ru.yandex.yandexmapkit.overlay.location.MyLocationItem;
import ru.yandex.yandexmapkit.overlay.location.OnMyLocationListener;
import tt.richTaxist.R;

/**
 * MapLayers.java
 * This file is a part of the Yandex Map Kit.
 * Version for Android  2012 YANDEX
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://legal.yandex.ru/mapkit/
 */
public class MapMyLocationChangeActivity extends Activity implements OnMyLocationListener {
    private static final String LOG_TAG = "MMLCA";
    //Called when the activity is first created.
    MapController mMapController;
    LinearLayout mView;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.appName);

        setContentView(R.layout.gps_yandex_loc);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        final MapView mapView = (MapView) findViewById(R.id.map);
        mapView.showBuiltInScreenButtons(true);
        mapView.getMapController().setZoomCurrent(10);

        //let's show jams
        //mapView.getMapController().setJamsVisible(true);

        mMapController = mapView.getMapController();
        mMapController.showJamsButton(true);
        // add listener
        mMapController.getOverlayManager().getMyLocation().addMyLocationListener(this);

        //mView = (LinearLayout)findViewById(R.id.view);
    }
    
    @Override
    public void onMyLocationChange(MyLocationItem myLocationItem) {
        double speed = myLocationItem.getSpeed();
        Log.d(LOG_TAG, "speed: "+String.valueOf(speed));
        if (speed==0.0) mMapController.setZoomCurrent(13); else mMapController.setZoomCurrent(13-((float) speed)/4);
        //Toast.makeText(MapMyLocationChangeActivity.this,)
        //final TextView textView = new TextView(this);
        //textView.setText("Type " + myLocationItem.getType()+" GeoPoint ["+myLocationItem.getGeoPoint().getLat()+","+myLocationItem.getGeoPoint().getLon()+"]");

        /*
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                //mView.addView(textView);
            }
        });*/
    }
}