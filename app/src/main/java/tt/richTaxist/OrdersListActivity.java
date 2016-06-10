package tt.richTaxist;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import tt.richTaxist.DB.DataSource;
import tt.richTaxist.Fragments.OrdersListFragment;
import tt.richTaxist.Units.Order;
import tt.richTaxist.Units.Shift;

public class OrdersListActivity extends AppCompatActivity implements
        OrdersListFragment.OrdersListInterface {
    private Shift currentShift;
    private DataSource dataSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getResources().getBoolean(R.bool.screenWiderThan450)) {
            //здесь достаточно места чтобы фрагмент листа заказов показать вместе с подробностями
            //значит эту активность можно закрыть
            finish();
            return;
        }
        dataSource = new DataSource(getApplicationContext());

        if (savedInstanceState == null) {
            //при первом создании активности прочитаем интент и найдем смену в БД
            long shiftID = getIntent().getLongExtra(Constants.SHIFT_ID_EXTRA, -1);
            if (shiftID != -1){
                currentShift = dataSource.getShiftsSource().getShiftByID(shiftID);
            }
            OrdersListFragment ordersListFragment = new OrdersListFragment();
            //если у активности только один внутренний фрагмент, файл разметки не нужен
            getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content, ordersListFragment, OrdersListFragment.TAG).commit();
        }
    }

    //обработчик выбора заказа из листа
    @Override
    public void returnToOrderFragment(Order selectedOrder) {
        Intent response = new Intent();
        response.putExtra(Order.ORDER_KEY, selectedOrder);
        setResult(RESULT_OK, response);//RESULT_CANCELED если закрываем аппаратным возвратом
        finish();
    }

    @Override
    public long getCurrentShiftId() {
        if (currentShift != null) {
            return currentShift.shiftID;
        } else {
            return -1;
        }
    }
}
