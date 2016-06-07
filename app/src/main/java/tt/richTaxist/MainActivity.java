package tt.richTaxist;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import java.util.Date;
import tt.richTaxist.DB.DataSource;
import tt.richTaxist.Fragments.OrdersListFragment;
import tt.richTaxist.Units.Order;
import tt.richTaxist.Units.Shift;
import tt.richTaxist.Fragments.OrderFragment;
/**
 * Created by Tau on 08.06.2015.
 */
public class MainActivity extends AppCompatActivity implements
        OrderFragment.OrderFragmentInterface,
        OrdersListFragment.OrdersListInterface{
    private Shift currentShift;
    private Order currentOrder = null;
    private DataSource dataSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dataSource = new DataSource(getApplicationContext());

        if (savedInstanceState == null) {
            //при первом создании активити прочитаем интент и найдем смену в БД или создадим новую смену
            long shiftID = getIntent().getLongExtra(Constants.SHIFT_ID_EXTRA, -1);
            if (shiftID != -1){
                currentShift = dataSource.getShiftsSource().getShiftByID(shiftID);
            } else {
                Shift shift = new Shift(new Date());
                shift.shiftID = dataSource.getShiftsSource().create(shift);
                currentShift = shift;
            }
        } else {
            //если активити пересоздается, читаем текущую смену и заказ из savedInstanceState
            long shiftID = savedInstanceState.getLong(Constants.SHIFT_ID_EXTRA, -1);
            currentShift = dataSource.getShiftsSource().getShiftByID(shiftID);
            currentOrder = savedInstanceState.getParcelable(Constants.CURRENT_ORDER_EXTRA);
        }

        //если фрагмент, использующий currentShift вшит в xml, то currentShift должен назначаться до вызова setContentView
        setContentView(R.layout.activity_main);

        showDetails(currentOrder);
    }

    @Override
    public void returnToOrderFragment(Order selectedOrder) {
        currentOrder = selectedOrder;
        showDetails(selectedOrder);
    }

    @Override
    public void addOrder(Order order){
        //if it is newly created order than order.orderID == -1
        boolean saveSuccess;
        if (order.orderID == -1) {
            order.orderID = dataSource.getOrdersSource().create(order);
            saveSuccess = order.orderID != -1;
        } else {
            saveSuccess = dataSource.getOrdersSource().update(order);
        }
        String msg = saveSuccess ? getResources().getString(R.string.orderSaved) : getResources().getString(R.string.orderNotSaved);
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
        currentShift.calculateShiftTotals(0, order.taxoparkID, dataSource);
        if (getResources().getBoolean(R.bool.screenWiderThan450)) {
            OrdersListFragment ordersListFragment = (OrdersListFragment) getSupportFragmentManager()
                    .findFragmentByTag(OrdersListFragment.TAG);
            ordersListFragment.addOrderToList(order);
        }
        currentOrder = null;
    }

    @Override
    public Order getOrder() {
        return currentOrder;
    }

    @Override
    public void resetOrder() {
        currentOrder = null;
    }

    @Override
    public void startTaximeter(){
        startActivityForResult(new Intent(this, TaximeterActivity.class), Constants.TAXIMETER_CALLBACK);
    }

    @Override
    public long getCurrentShiftId() {
        return currentShift.shiftID;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        OrderFragment orderFragment = (OrderFragment) getSupportFragmentManager().findFragmentById(R.id.orderFragment);
        if (orderFragment != null && resultCode == Activity.RESULT_OK) {
            if (requestCode == Constants.TAXIMETER_CALLBACK){
                //выполняется после возврата из OrdersListActivity
                int distance = data.getIntExtra(Order.PARAM_DISTANCE, 0);
                long travelTime = data.getIntExtra(Order.PARAM_TRAVEL_TIME, 0);
                addOrder(orderFragment.wrapDataIntoOrder(currentOrder, distance, travelTime));
                currentOrder = null;
            }
            if (requestCode == Constants.ORDERS_LIST_CALLBACK){
                //выполняется после возврата из OrdersListActivity
                Order selectedOrder = data.getParcelableExtra(Order.ORDER_KEY);
                if (selectedOrder != null) {
                    showDetails(selectedOrder);
                }
            }
        }
    }



    private void showDetails(Order order) {
        currentOrder = order;
        if (getResources().getBoolean(R.bool.screenWiderThan450)) {
            //ветка только для планшетов или телефонов в ландшафте. точнее для всего, что имеет ширину экрана 450dp+
            OrderFragment orderFragment = new OrderFragment();
            orderFragment.setOrder(currentOrder);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.container_order, orderFragment);
            ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out);
            ft.commit();
        } else {
            OrderFragment orderFragment = (OrderFragment) getSupportFragmentManager().findFragmentById(R.id.orderFragment);
            if (orderFragment != null) {
                orderFragment.setOrder(currentOrder);
            } else {
                Log.d(Constants.LOG_TAG, "MainActivity.showDetails() failed to find OrderFragment");
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
        menu.findItem(R.id.action_show_orders_list).setVisible(!getResources().getBoolean(R.bool.screenWiderThan450));
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.action_show_orders_list:
                //item is shown only in portrait
                if (dataSource.getOrdersSource().getOrdersListCount(currentShift.shiftID) != 0){
                    Intent intent = new Intent(this, OrdersListActivity.class);
                    intent.putExtra(Constants.SHIFT_ID_EXTRA, currentShift.shiftID);
                    startActivityForResult(intent, Constants.ORDERS_LIST_CALLBACK);
                } else {
                    Toast.makeText(getApplicationContext(), R.string.noOrdersMSG, Toast.LENGTH_SHORT).show();
                }
                return true;


            case R.id.main_menu:
                startActivity(new Intent(this, FirstScreenActivity.class));
                finish();
                return true;

            case R.id.action_shift_totals:
                Intent intent = new Intent(this, ShiftTotalsActivity.class);
                intent.putExtra(Constants.SHIFT_ID_EXTRA, currentShift.shiftID);
                intent.putExtra(Constants.AUTHOR_EXTRA, "MainActivity");
                startActivity(intent);
                finish();
                return true;

            case R.id.action_grand_totals:
                Intent intent2 = new Intent(this, GrandTotalsActivity.class);
                intent2.putExtra(Constants.AUTHOR_EXTRA, "MainActivity");
                startActivity(intent2);
                finish();
                return true;

            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(Constants.SHIFT_ID_EXTRA, currentShift.shiftID);
        outState.putParcelable(Constants.CURRENT_ORDER_EXTRA, currentOrder);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, FirstScreenActivity.class));
        finish();
    }
}
