package tt.richTaxist.DB;

import java.util.Date;
import java.util.List;
import java.util.Map;

import tt.richTaxist.DB.sql.OrdersSQLHelper;
import tt.richTaxist.MainActivity;
import tt.richTaxist.Order;
import tt.richTaxist.Shift;
import tt.richTaxist.TypeOfPayment;

/**
 * Created by AlexShredder on 29.06.2015.
 */
public class OrdersStorage {

    private static OrdersSQLHelper dbOpenHelper = new OrdersSQLHelper(MainActivity.context);

    private OrdersStorage() {
    }

    private static OrdersSQLHelper getDatabase() {
        return dbOpenHelper;
    }
    public static boolean commit(Order order){
        return getDatabase().commit(order);
    }
    public static int remove(Order order){
        return getDatabase().remove(order);
    }
    public static int remove(List<Order> orders){
        return getDatabase().remove(orders);
    }
    public static OrdersStorageList getOrders(Date fromDate, Date toDate) {
        return getDatabase().getOrders(fromDate, toDate);
    }
    public static OrdersStorageList getOrdersByShift(Shift shift) {
        return getDatabase().getOrdersByShift(shift);
    }
    public static OrdersStorageList getOrders(Date date) {
        return getOrders(date, date);
    }

    public static Map<TypeOfPayment,Integer> getSumOrdersByShift(Shift shift) {
        return getDatabase().getSumOrdersByShift(shift);
    }
    public static void deleteOrdersByShift(Shift shift) {
        getDatabase().deleteOrdersByShift(shift);
    }
    public static boolean hasShiftOrders(Shift shift) {
        return getDatabase().hasShiftOrders(shift);
    }
}
