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
    public static boolean commit(Order order){
        return dbOpenHelper.commit(order);
    }
    public static OrdersStorageList getOrders(Date fromDate, Date toDate) {
        return dbOpenHelper.getOrders(fromDate, toDate);
    }
    public static Map<TypeOfPayment,Integer> getSumOrdersByShift(Shift shift) {
        return dbOpenHelper.getSumOrdersByShift(shift);
    }
    public static OrdersStorageList getOrdersByShift(Shift shift) {
        return dbOpenHelper.getOrdersByShift(shift);
    }
    public static boolean hasShiftOrders(Shift shift) {
        return dbOpenHelper.hasShiftOrders(shift);
    }
    public static int remove(Order order){
        return dbOpenHelper.remove(order);
    }
    public static int remove(List<Order> orders){
        return dbOpenHelper.remove(orders);
    }
    public static void deleteOrdersByShift(Shift shift) {
        dbOpenHelper.deleteOrdersByShift(shift);
    }
}
