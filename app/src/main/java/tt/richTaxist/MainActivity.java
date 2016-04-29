package tt.richTaxist;

import android.content.res.Configuration;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Date;
import tt.richTaxist.Fragments.OrdersListFragment;
import tt.richTaxist.Units.Order;
import tt.richTaxist.DB.OrdersSQLHelper;
import tt.richTaxist.Units.Shift;
import tt.richTaxist.Fragments.OrderFragment;

/**
 * Created by Tau on 08.06.2015.
 */

public class MainActivity extends AppCompatActivity implements
        OrderFragment.OrderFragmentInterface,
        OrdersListFragment.OrdersListInterface {
    private static final String LOG_TAG = "MainActivity";
    public static Context context;
    public static Shift currentShift;
    public final static ArrayList<Shift> shiftsStorage = new ArrayList<>();
    public final static ArrayList<Order> ordersStorage = new ArrayList<>();
    private boolean deviceIsInLandscape;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
//        Util.measureScreenWidth(context, (ViewGroup) findViewById(R.id.container_main));
        Util.deviceIsInLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;

        //фрагментная логика
        deviceIsInLandscape = (findViewById(R.id.container_orders_list) != null);
        if (deviceIsInLandscape){
            //if deviceIsInLandscape then OrderFragment is statically added
            //we can't statically add OrdersListFragment because OrdersListFragment.adapter.notifyDataSetChanged()
            //doesn't work properly with RecyclerView
            addOrdersListFragment();
        } else {
            addOrderFragment();
        }
    }

    private void addOrdersListFragment(){
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        OrdersListFragment fragment = new OrdersListFragment();
        ft.replace(R.id.container_orders_list, fragment);
        ft.commit();
    }

    private void addOrderFragment(){
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        OrderFragment fragment = new OrderFragment();
        ft.replace(R.id.container_main, fragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (deviceIsInLandscape) {
            addOrdersListFragment();
        }
    }

    @Override
    public void addOrder(Order order){
        ordersStorage.add(order);
        OrdersSQLHelper.dbOpenHelper.commit(order);
        sortOrdersStorage();
        currentShift.calculateShiftTotals(0, order.taxoparkID);
        if (deviceIsInLandscape) {
            addOrdersListFragment();
        }
        Toast.makeText(context, "заказ добавлен", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void removeOrder(Order order){
        ordersStorage.remove(order);
        OrdersSQLHelper.dbOpenHelper.remove(order);
    }

    @Override
    public void returnToOrderFragment(Order order) {
        OrderFragment orderFragment;
        if (deviceIsInLandscape){
            //if deviceIsInLandscape then OrderFragment is statically added
            orderFragment = (OrderFragment) getSupportFragmentManager().findFragmentByTag(OrderFragment.FRAGMENT_TAG);
            orderFragment.setOrder(order);
            orderFragment.refreshWidgets(order);
        } else {
            //next line removes record of OrdersListFragmentTransaction from backStack
            getSupportFragmentManager().popBackStackImmediate();
            orderFragment = new OrderFragment();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.container_main, orderFragment);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.commit();
            orderFragment.setOrder(order);
        }
    }

    public static void sortOrdersStorage(){
        for (int i = ordersStorage.size() - 1; i > 0; i--) {
            for (int j = 0; j < i; j++) {
                Date currentDate = ordersStorage.get(j).arrivalDateTime;
                Date nextDate = ordersStorage.get(j + 1).arrivalDateTime;
                //ниже проверяем НАРУШЕНИЕ порядка, а не его правильность. если проверка true, то переставляем
                //before = самый свежий должен быть наверху, after = самый старый должен быть наверху
                if (Util.youngIsOnTop ? !currentDate.after(nextDate) : !currentDate.before(nextDate)) {
                    Order tmp = ordersStorage.get(j);
                    ordersStorage.set(j, ordersStorage.get(j + 1));
                    ordersStorage.set(j + 1, tmp);
                }
            }
        }
    }

    public static void sortShiftsStorage(){
        for (int i = shiftsStorage.size() - 1; i > 0; i--) {
            for (int j = 0; j < i; j++) {
                Date currentShiftStart = shiftsStorage.get(j).beginShift;
                Date nextShiftStart = shiftsStorage.get(j + 1).beginShift;
                //ниже проверяем НАРУШЕНИЕ порядка, а не его правильность. если проверка true, то переставляем
                //before = самый свежий должен быть наверху, after = самый старый должен быть наверху
                if (Util.youngIsOnTop ? !currentShiftStart.after(nextShiftStart) : !currentShiftStart.before(nextShiftStart)) {
                    Shift tmp = shiftsStorage.get(j);
                    shiftsStorage.set(j, shiftsStorage.get(j + 1));
                    shiftsStorage.set(j + 1, tmp);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_show_orders_list).setVisible(!deviceIsInLandscape);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.action_show_orders_list:
                //item is shown only in portrait
                if (ordersStorage.size() != 0){
                    OrdersListFragment ordersListFragment = (OrdersListFragment)
                            getSupportFragmentManager().findFragmentByTag(OrdersListFragment.FRAGMENT_TAG);
                    if (ordersListFragment == null) {
                        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                        ordersListFragment = new OrdersListFragment();
                        ft.replace(R.id.container_main, ordersListFragment);
                        ft.addToBackStack("OrdersListFragmentTransaction");
                        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                        ft.commit();
                    }
                } else {
                    Toast.makeText(context, R.string.noOrdersMSG, Toast.LENGTH_SHORT).show();
                }
                return true;


            case R.id.main_menu:
                startActivity(new Intent(context, FirstScreenActivity.class));
                finish();
                return true;

            case R.id.action_shift_totals:
                Intent intent = new Intent(context, ShiftTotalsActivity.class);
                intent.putExtra("author", "MainActivity");
                startActivity(intent);
                finish();
                return true;

            case R.id.action_grand_totals:
                Intent intent2 = new Intent(context, GrandTotalsActivity.class);
                intent2.putExtra(GrandTotalsActivity.AUTHOR, "MainActivity");
                startActivity(intent2);
                finish();
                return true;

            case R.id.action_settings:
                startActivity(new Intent(context, SettingsActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            startActivity(new Intent(context, FirstScreenActivity.class));
            finish();
        } else {
            getSupportFragmentManager().popBackStack();
        }
    }
}
