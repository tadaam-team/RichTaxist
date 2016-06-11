package tt.richCabman.activities;

import android.support.v7.app.AppCompatActivity;
import tt.richCabman.fragments.MapPathFragment;
import tt.richCabman.interfaces.MapProvider;

/**
 * Created by AlexShredder on 16.09.2015.
 */
public class GoogleMaps  extends AppCompatActivity implements MapProvider {
    @Override
    public Class getPathActivityClass() {
        return MapPathFragment.class;
    }

    @Override
    public Class getLocActivityClass() {
        return MapMyLocationChangeActivity.class;
    }
}
