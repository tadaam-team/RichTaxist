package tt.richTaxist;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import tt.richTaxist.Bricks.SingleChoiceListDF;
import tt.richTaxist.DB.Sources.BillingsSource;
import tt.richTaxist.DB.Sources.OrdersSource;
import tt.richTaxist.DB.Sources.ShiftsSource;
import tt.richTaxist.DB.Sources.TaxoparksSource;
import tt.richTaxist.Fragments.OrdersListFragment;
import tt.richTaxist.Units.Order;
import tt.richTaxist.Units.Shift;

public class OrdersListActivity extends AppCompatActivity implements
        OrdersListFragment.OrdersListInterface,
        SingleChoiceListDF.SingleChoiceListDFInterface {
    private Shift currentShift;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getResources().getBoolean(R.bool.screenWiderThan450)) {
            // If the screen is now in landscape mode, we can show the
            // dialog in-line with the list so we don't need this activity.
            finish();
            return;
        }

        if (savedInstanceState == null) {
            //при первом создании активити прочитаем интент и найдем смену в БД
            long shiftID = getIntent().getLongExtra(Constants.SHIFT_ID_EXTRA, -1);
            if (shiftID != -1){
                ShiftsSource shiftsSource = new ShiftsSource(getApplicationContext());
                currentShift = shiftsSource.getShiftByID(shiftID);
            }
            // During initial setup, plug in the details fragment.
            OrdersListFragment ordersListFragment = new OrdersListFragment();
            //activity, containing only one fragment doesn't need a layout
            getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content, ordersListFragment, OrdersListFragment.TAG).commit();
        }
    }

    //handles user tap on order in the list
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

    @Override
    public void getSelectedAction(long selectedOrderID, int selectedActionID) {
        OrdersSource ordersSource = new OrdersSource(getApplicationContext());
        Order selectedOrder = ordersSource.getOrderByID(selectedOrderID);
        OrdersListFragment ordersListFragment = (OrdersListFragment) getSupportFragmentManager()
                .findFragmentByTag(OrdersListFragment.TAG);

        switch (selectedActionID){
            case 0://править
                if (selectedOrder != null) {
                    returnToOrderFragment(selectedOrder);
                }
                break;

            case 1://показать подробности
                TaxoparksSource taxoparksSource = new TaxoparksSource(getApplicationContext());
                BillingsSource billingsSource = new BillingsSource(getApplicationContext());
                Toast.makeText(this, selectedOrder.getDescription(this, taxoparksSource, billingsSource), Toast.LENGTH_LONG).show();
                break;

            case 2://удалить
                ordersSource.remove(selectedOrder);
                ordersListFragment.rvAdapter.removeObject(selectedOrder);
                Toast.makeText(this, R.string.orderDeletedMSG, Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
