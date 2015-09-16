package tt.richTaxist.gps.google;

import android.support.v7.app.AppCompatActivity;

import tt.richTaxist.gps.MapProvider;

/**
 * Created by AlexShredder on 16.09.2015.
 */
public class GoogleMaps  extends AppCompatActivity implements MapProvider {
    @Override
    public Class getPathActivityClass() {
        return MapPathActivity.class;
    }

    @Override
    public Class getLocActivityClass() {
        return MapMyLocationChangeActivity.class;
    }
}
