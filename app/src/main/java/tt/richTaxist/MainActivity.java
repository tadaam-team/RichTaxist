package tt.richTaxist;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import tt.richTaxist.DB.Sources.BillingsSource;
import tt.richTaxist.DB.Sources.OrdersSource;
import tt.richTaxist.DB.Sources.ShiftsSource;
import tt.richTaxist.Fragments.OrdersListFragment;
import tt.richTaxist.Units.Order;
import tt.richTaxist.Units.Shift;
import tt.richTaxist.Fragments.OrderFragment;
/**
 * Created by Tau on 08.06.2015.
 */
public class MainActivity extends AppCompatActivity implements
        OrderFragment.OrderFragmentInterface,
        OrdersListFragment.OrdersListInterface {
    private static final String LOG_TAG = FirstScreenActivity.LOG_TAG;
    public static Shift currentShift;
    private boolean deviceIsInLandscape;
    private ShiftsSource shiftsSource;
    private OrdersSource ordersSource;
    private BillingsSource billingsSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Util.measureScreenWidth(getApplicationContext(), (ViewGroup) findViewById(R.id.container_main));

        shiftsSource = new ShiftsSource(getApplicationContext());
        ordersSource = new OrdersSource(getApplicationContext());
        billingsSource = new BillingsSource(getApplicationContext());

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
        //на входе заказ с id == -1
        order.orderID = ordersSource.create(order);
        currentShift.calculateShiftTotals(0, order.taxoparkID, shiftsSource, ordersSource, billingsSource);
        if (deviceIsInLandscape) {
            addOrdersListFragment();
        }
        Toast.makeText(getApplicationContext(), "заказ добавлен", Toast.LENGTH_SHORT).show();
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
                if (ordersSource.getOrdersListCount(currentShift.shiftID) != 0){
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
                    Toast.makeText(getApplicationContext(), R.string.noOrdersMSG, Toast.LENGTH_SHORT).show();
                }
                return true;


            case R.id.main_menu:
                startActivity(new Intent(getApplicationContext(), FirstScreenActivity.class));
                finish();
                return true;

            case R.id.action_shift_totals:
                Intent intent = new Intent(getApplicationContext(), ShiftTotalsActivity.class);
                intent.putExtra("author", "MainActivity");
                startActivity(intent);
                finish();
                return true;

            case R.id.action_grand_totals:
                Intent intent2 = new Intent(getApplicationContext(), GrandTotalsActivity.class);
                intent2.putExtra(GrandTotalsActivity.AUTHOR, "MainActivity");
                startActivity(intent2);
                finish();
                return true;

            case R.id.action_settings:
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            startActivity(new Intent(getApplicationContext(), FirstScreenActivity.class));
            finish();
        } else {
            getSupportFragmentManager().popBackStack();
        }
    }
}
