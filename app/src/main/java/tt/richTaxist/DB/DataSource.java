package tt.richTaxist.DB;

import android.content.Context;
import tt.richTaxist.DB.Sources.BillingsSource;
import tt.richTaxist.DB.Sources.LocationsSource;
import tt.richTaxist.DB.Sources.OrdersSource;
import tt.richTaxist.DB.Sources.ShiftsSource;
import tt.richTaxist.DB.Sources.TaxoparksSource;
/**
 * Created by TAU on 05.06.2016.
 */
public class DataSource {
    private BillingsSource billingsSource;
    private LocationsSource locationsSource;
    private OrdersSource ordersSource;
    private ShiftsSource shiftsSource;
    private TaxoparksSource taxoparksSource;

    public DataSource(Context context) {
        billingsSource = new BillingsSource(context);
        locationsSource = new LocationsSource(context);
        ordersSource = new OrdersSource(context);
        shiftsSource = new ShiftsSource(context);
        taxoparksSource = new TaxoparksSource(context);
    }

    public BillingsSource getBillingsSource() {
        return billingsSource;
    }
    public LocationsSource getLocationsSource() {
        return locationsSource;
    }
    public OrdersSource getOrdersSource() {
        return ordersSource;
    }
    public ShiftsSource getShiftsSource() {
        return shiftsSource;
    }
    public TaxoparksSource getTaxoparksSource() {
        return taxoparksSource;
    }
}
