package tt.richTaxist.gps;

/**
 * Created by AlexShredder on 05.07.2015.
 */
public class Coordinates {
    private double lon, lat;

    public Coordinates(double lon, double lat) {
        this.lon = lon;
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public double getLat() {
        return lat;
    }

}
