package tt.richTaxist.DB;

import java.util.ArrayList;
import java.util.Date;
import tt.richTaxist.Order;
import tt.richTaxist.Shift;

/**
 * Created by AlexShredder on 29.06.2015.
 */
public class OrdersStorageList extends ArrayList<Order> {
    private boolean writeToDB = true;

    public OrdersStorageList(boolean writeToDB) {
        this.writeToDB = writeToDB;
    }
    public OrdersStorageList() {    }
    public void fillTodayOrders() {
        clear(false);
        addAll(OrdersStorage.getOrders(new Date()));
    }
    public void fillOrdersByShift(Shift shift) {
        clear(false);
        addAll(OrdersStorage.getOrdersByShift(shift));
    }

    @Override
    public boolean add(Order object) {
        if (writeToDB) OrdersStorage.commit(object);
        return super.add(object);
    }

    @Override
    public void clear() {
        if (writeToDB) OrdersStorage.remove(this);
        super.clear();
    }
    public void clear(boolean writeToDB) {
        if (writeToDB) OrdersStorage.remove(this);
        super.clear();
    }

    @Override
    public boolean remove(Object object) {
        if (writeToDB) OrdersStorage.remove((Order)object);
        return super.remove(object);
    }

    public void setWriteToDB(boolean writeToDB) {
        this.writeToDB = writeToDB;
    }
}
