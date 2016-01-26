package tt.richTaxist.gps;

import android.app.FragmentTransaction;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.google.android.gms.maps.SupportMapFragment;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import tt.richTaxist.Bricks.RangeSeekBar;
import tt.richTaxist.DB.LocationsSQLHelper;
import tt.richTaxist.MainActivity;
import tt.richTaxist.R;
import tt.richTaxist.Shift;
import tt.richTaxist.gps.google.MapPathActivity;

public class RouteActivity extends FragmentActivity {
    private static final String LOG_TAG = "Route activity";
    //private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private MapPathActivity mapFragment;
    private AsyncTask updateTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.gps_activity_route);
        setUpMapIfNeeded();

        mapFragment = new MapPathActivity();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.mapFragment, mapFragment);
        ft.commit();

        final Shift currentShift = MainActivity.currentShift;

        //add RangeSeekBar to map window
        final Calendar rangeStart = Calendar.getInstance();
        rangeStart.setTime(currentShift.beginShift);
        final Calendar rangeEnd = Calendar.getInstance();
        if (currentShift.isClosed()) rangeEnd.setTime(currentShift.endShift);

        mapFragment.showPath(LocationsSQLHelper.dbOpenHelper.getLocationsByShift(currentShift));
        final TextView tvRangeStart = (TextView) findViewById(R.id.tvRangeStart);
        final TextView tvRangeEnd   = (TextView) findViewById(R.id.tvRangeEnd);
        tvRangeStart.setText(getStringDateTimeFromCal(rangeStart));
        tvRangeEnd  .setText(getStringDateTimeFromCal(rangeEnd));

        final RangeSeekBar<Long> seekBar = new RangeSeekBar<>(rangeStart.getTimeInMillis(), rangeEnd.getTimeInMillis(), RouteActivity.this);
        seekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener<Long>() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Long minValue, Long maxValue) {
                // handle changed range values
                if (updateTask != null) updateTask.cancel(true);
                rangeStart.setTimeInMillis(minValue);
                rangeEnd.setTimeInMillis(maxValue);
                tvRangeStart.setText(getStringDateTimeFromCal(rangeStart));
                tvRangeEnd.setText(getStringDateTimeFromCal(rangeEnd));
                mapFragment.showPath(LocationsSQLHelper.dbOpenHelper.getLocationsByPeriod(rangeStart.getTime(), rangeEnd.getTime()));
            }
        });

        // add RangeSeekBar to pre-defined layout
        FrameLayout layout = (FrameLayout) findViewById(R.id.seekBarPlaceHolderInRouteActivity);
        layout.addView(seekBar);


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
                //Log.d(LOG_TAG,"Updating map");
                try {
                    seekBar.setNormalizedMaxValue(Calendar.getInstance().getTimeInMillis());
                    mapFragment.showPath(LocationsSQLHelper.dbOpenHelper.getLocationsByPeriod
                            (rangeStart.getTime(), Calendar.getInstance().getTime()));
                } catch (Exception e) {
                    e.printStackTrace();
                    updateTask.cancel(true);
                }
            }
        };
        updateTask.execute();
    }

    private String getStringDateTimeFromCal(Calendar cal){
        return String.format("%02d.%02d.%02d %02d:%02d", cal.get(Calendar.DAY_OF_MONTH),
                cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR) % 100,
                cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
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
