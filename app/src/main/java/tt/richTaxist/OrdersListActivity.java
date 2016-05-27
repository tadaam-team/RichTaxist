package tt.richTaxist;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import tt.richTaxist.Fragments.OrdersListFragment;
import tt.richTaxist.Units.Order;

public class OrdersListActivity extends AppCompatActivity implements
        OrdersListFragment.OrdersListInterface {
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
            // During initial setup, plug in the details fragment.
            OrdersListFragment ordersListFragment = new OrdersListFragment();
            //activity, containing only one fragment doesn't need a layout
            getSupportFragmentManager().beginTransaction().add(android.R.id.content, ordersListFragment).commit();
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
}
