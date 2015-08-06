package tt.richTaxist.gps.yandex;

import android.support.v7.app.AppCompatActivity;
import tt.richTaxist.gps.MapProvider;
import tt.richTaxist.gps.yandex.mylocation.MapMyLocationChangeActivity;
import tt.richTaxist.gps.yandex.path.MapPathActivity;

/**
 * Created by AlexShredder on 06.07.2015.
 */
public class YandexMaps  extends AppCompatActivity implements MapProvider{

    @Override
    public Class getPathActivityClass() {
        return MapPathActivity.class;
    }

    @Override
    public Class getLocActivityClass() {
        return MapMyLocationChangeActivity.class;
    }
}
