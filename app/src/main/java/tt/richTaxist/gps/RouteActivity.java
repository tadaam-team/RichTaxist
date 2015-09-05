package tt.richTaxist.gps;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.SupportMapFragment;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import tt.richTaxist.DB.LocationsStorage;
import tt.richTaxist.MainActivity;
import tt.richTaxist.R;
import tt.richTaxist.Shift;
import tt.richTaxist.Storage;

public class RouteActivity extends FragmentActivity {
    static Context context;
    private static final String LOG_TAG = "Route activity";
    //private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Fragment mapFragment;
    private AsyncTask updateTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.gps_activity_route);
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        context = getApplicationContext();
        Storage.measureScreenWidth(context, (ViewGroup) findViewById(R.id.gps_activity_route));
        setUpMapIfNeeded();

        mapFragment = GPSHelper.getMapFragment();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.mapFragment, mapFragment);
        ft.commit();

        final Shift currentShift = MainActivity.currentShift;

        //add RangeSeekBar to map window
        final Calendar rangeStart = Calendar.getInstance();
        rangeStart.setTime(currentShift.beginShift);
        final Calendar rangeEnd = Calendar.getInstance();
        if (currentShift.isClosed()) rangeEnd.setTime(currentShift.endShift);

        ((MapFragment) mapFragment).showPath(LocationsStorage.getLocationsByShift(currentShift));

        final RangeSeekBar<Long> seekBar = new RangeSeekBar<>(rangeStart.getTimeInMillis(), rangeEnd.getTimeInMillis(), RouteActivity.this);
        seekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener<Long>() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Long minValue, Long maxValue) {
                // handle changed range values
                //TODO добавить окошки для введенных дат
                if (updateTask!=null) updateTask.cancel(true);
                rangeStart.setTimeInMillis(minValue);
                rangeEnd.setTimeInMillis(maxValue);
                String msg1 = String.format("%02d.%02d.%02d %02d:%02d", rangeStart.get(Calendar.DAY_OF_MONTH), rangeStart.get(Calendar.MONTH) + 1, rangeStart.get(Calendar.YEAR) % 100,
                        rangeStart.get(Calendar.HOUR_OF_DAY), rangeStart.get(Calendar.MINUTE));
                String msg2 = String.format("%02d.%02d.%02d %02d:%02d", rangeEnd.get(Calendar.DAY_OF_MONTH), rangeEnd.get(Calendar.MONTH) + 1, rangeEnd.get(Calendar.YEAR) % 100,
                        rangeEnd.get(Calendar.HOUR_OF_DAY), rangeEnd.get(Calendar.MINUTE));
                Toast.makeText(RouteActivity.this, "MIN = " + msg1 + ",\nMAX = " + msg2, Toast.LENGTH_LONG).show();
                ((MapFragment) mapFragment).showPath(LocationsStorage.getLocationsByPeriod(rangeStart.getTime(), rangeEnd.getTime()));
            }
        });

        // add RangeSeekBar to pre-defined layout
        ViewGroup layout = (ViewGroup) findViewById(R.id.gps_activity_route);
        layout.addView(seekBar);

        if (!currentShift.isClosed()){

            updateTask = new AsyncTask() {
                @Override
                protected Object doInBackground(Object[] params) {
                    while (true) {
                        try {
                            if (isCancelled()) break;
                            TimeUnit.SECONDS.sleep(4);
                            publishProgress();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            break;
                        }
                    }
                    return null;
                }

                @Override
                protected void onProgressUpdate(Object[] values) {
                    //super.onProgressUpdate(values);
                    Log.d(LOG_TAG,"Updating map");
                    try {
                        seekBar.setNormalizedMaxValue(Calendar.getInstance().getTimeInMillis());
                        ((MapFragment) mapFragment).showPath(LocationsStorage.getLocationsByPeriod(rangeStart.getTime(), Calendar.getInstance().getTime()));
                    } catch (Exception e) {
                        e.printStackTrace();
                        updateTask.cancel(true);
                    }
                }
            };
            updateTask.execute();
         }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (updateTask!=null) updateTask.cancel(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {link mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        /*if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }*/
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        //mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }
}
