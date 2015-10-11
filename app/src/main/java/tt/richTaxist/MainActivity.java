package tt.richTaxist;

import android.content.res.Configuration;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Date;
import tt.richTaxist.ChatClient.ChatLoginActivity;
import tt.richTaxist.DB.OrdersSQLHelper;
import tt.richTaxist.Enums.ActivityState;

/**
 * Created by Tau on 08.06.2015.
 */

public class MainActivity extends AppCompatActivity implements
        OrderFragment.OnOrderFragmentInteractionListener,
        OrdersListFragment.OnOrderListFragmentInteractionListener {
    private static final String LOG_TAG = "MainActivity";
    public static Context context;
    public static Shift currentShift;
    final static ArrayList<Shift> shiftsStorage = new ArrayList<>();
    final static ArrayList<Order> ordersStorage = new ArrayList<>();

    public static ArrayAdapter orderAdapterMA;
    public static FragmentManager fragmentManager;
    private OrderFragment fragment1;
    private OrdersListFragment fragment2;
    private ActivityState activityState;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
//        Storage.measureScreenWidth(context, (ViewGroup) findViewById(R.id.container_main));
        Storage.deviceIsInLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;

        //фрагментная логика
        fragmentManager = getSupportFragmentManager();
        if (savedInstanceState != null) {
            int activityStateID = savedInstanceState.getInt("activityState", ActivityState.LAND_2_1.id);
            activityState = ActivityState.getById(activityStateID);
            fragment1 = (OrderFragment) fragmentManager.findFragmentByTag("fragment1");
            fragment2 = (OrdersListFragment) fragmentManager.findFragmentByTag("fragment2");
        }
        else {
            fragment2 = new OrdersListFragment();
            fragment1 = new OrderFragment();
            FragmentTransaction transactionInitial = fragmentManager.beginTransaction();
            transactionInitial.add(R.id.container_main, fragment2, "fragment2");
            transactionInitial.add(R.id.container_main, fragment1, "fragment1");
            transactionInitial.commit();
        }
        activityState = Storage.manageFragments(fragmentManager, activityState, fragment1, fragment2);
    }

    public void addOrder(Order order){
        ordersStorage.add(order);
        OrdersSQLHelper.dbOpenHelper.commit(order);
        sortOrdersStorage();
        if (orderAdapterMA != null) orderAdapterMA.notifyDataSetChanged();
        currentShift.calculateShiftTotals(0);
        Toast.makeText(context, "заказ добавлен", Toast.LENGTH_SHORT).show();
    }

    public void refreshWidgets(Order order){
        if (fragment1 != null) fragment1.refreshWidgets(order);
    }

    public void removeOrder(Order order){
        ordersStorage.remove(order);
        OrdersSQLHelper.dbOpenHelper.remove(order);
    }

    public void switchToOrderFragment(){
        switch (activityState){
            case LAND_2:
                activityState = ActivityState.LAND_2_1;
                activityState = Storage.manageFragments(fragmentManager, activityState, fragment1, fragment2);
                break;
            case PORT_2:
                activityState = ActivityState.PORT_1;
                activityState = Storage.manageFragments(fragmentManager, activityState, fragment1, fragment2);
                break;
            //также возможной точкой вызова этого метода является LAND_2_1, но в этом случае никаких операций по смене фрагментов не нужно
        }
    }

    static void sortOrdersStorage(){
        for (int i = ordersStorage.size() - 1; i > 0; i--) {
            for (int j = 0; j < i; j++) {
                Date currentDate = ordersStorage.get(j).arrivalDateTime;
                Date nextDate = ordersStorage.get(j + 1).arrivalDateTime;
                //ниже проверяем НАРУШЕНИЕ порядка, а не его правильность. если проверка true, то переставляем
                //before = самый свежий должен быть наверху, after = самый старый должен быть наверху
                if (Storage.youngIsOnTop ? !currentDate.after(nextDate) : !currentDate.before(nextDate)) {
                    Order tmp = ordersStorage.get(j);
                    ordersStorage.set(j, ordersStorage.get(j + 1));
                    ordersStorage.set(j + 1, tmp);
                }
            }
        }
    }

    static void sortShiftsStorage(){
        for (int i = shiftsStorage.size() - 1; i > 0; i--) {
            for (int j = 0; j < i; j++) {
                Date currentShiftStart = shiftsStorage.get(j).beginShift;
                Date nextShiftStart = shiftsStorage.get(j + 1).beginShift;
                //ниже проверяем НАРУШЕНИЕ порядка, а не его правильность. если проверка true, то переставляем
                //before = самый свежий должен быть наверху, after = самый старый должен быть наверху
                if (Storage.youngIsOnTop ? !currentShiftStart.after(nextShiftStart) : !currentShiftStart.before(nextShiftStart)) {
                    Shift tmp = shiftsStorage.get(j);
                    shiftsStorage.set(j, shiftsStorage.get(j + 1));
                    shiftsStorage.set(j + 1, tmp);
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("activityState", activityState.id);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.action_show_orders_list:
                //Обработчик нажатия кнопки "Список заказов"
                if (ordersStorage.size() == 0)
                    Toast.makeText(context, R.string.noOrdersMSG, Toast.LENGTH_SHORT).show();
                else {
                    if (Storage.deviceIsInLandscape) activityState = ActivityState.LAND_2;
                    else activityState = ActivityState.PORT_2;
                    activityState = Storage.manageFragments(fragmentManager, activityState, fragment1, fragment2);
                }
                return true;


            case R.id.main_menu:
                startActivity(new Intent(context, FirstScreenActivity.class));
                finish();
                return true;

            case R.id.action_chat:
                startActivity(new Intent(context, ChatLoginActivity.class));
                return true;

            case R.id.action_shift_totals:
                Intent intent = new Intent(context, ShiftTotalsActivity.class);
                intent.putExtra("author", "MainActivity");
                startActivity(intent);
                finish();
                return true;

            case R.id.action_grand_totals:
                startActivity(new Intent(context, GrandTotalsActivity.class));
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
        switch (activityState){
            case LAND_2:
                activityState = ActivityState.LAND_2_1;
                activityState = Storage.manageFragments(fragmentManager, activityState, fragment1, fragment2);
                break;
            case PORT_2:
                activityState = ActivityState.PORT_1;
                activityState = Storage.manageFragments(fragmentManager, activityState, fragment1, fragment2);
                break;
            default:
                startActivity(new Intent(context, FirstScreenActivity.class));
                finish();
        }
    }
}
