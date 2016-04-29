package tt.richTaxist.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;
import tt.richTaxist.Bricks.CustomSpinner;
import tt.richTaxist.Bricks.CustomSpinner.TypeOfSpinner;
import tt.richTaxist.DB.OrdersSQLHelper;
import tt.richTaxist.MainActivity;
import tt.richTaxist.R;
import tt.richTaxist.RecyclerViewAdapter;
import tt.richTaxist.Units.Order;

public class OrdersListFragment extends Fragment {
    public static final String FRAGMENT_TAG = "OrdersListFragment";
    private static final String LOG_TAG = "OrdersListFragment";
    private OrdersListInterface mListener;
    public RecyclerViewAdapter rvAdapter;
    private CustomSpinner spnTaxopark;

    public OrdersListFragment() { }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try { mListener = (OrdersListInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement " + mListener.getClass().getName());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        MainActivity.sortOrdersStorage();
        View rootView = inflater.inflate(R.layout.fragment_orders_list, container, false);
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        spnTaxopark = (CustomSpinner) rootView.findViewById(R.id.spnTaxopark);

        //TODO: get correct dataSource
//        OrdersSQLHelper.dbOpenHelper.getOrdersList();
        rvAdapter = new RecyclerViewAdapter(MainActivity.ordersStorage, RecyclerViewAdapter.AdapterDataType.ORDER);
        recyclerView.setAdapter(rvAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
//        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        recyclerView.setLayoutManager(layoutManager);
        rvAdapter.setListener(new RecyclerViewAdapter.Listener() {
            @Override
            public void onClick(Object selectedObject) {
                Order selectedOrder = (Order) selectedObject;
                mListener.removeOrder(selectedOrder);
                //исправленная запись вернется в список по нажатию "ДОБАВИТЬ ЗАКАЗ"
                rvAdapter.notifyDataSetChanged();
                Toast.makeText(getActivity(), R.string.orderSelectedMSG, Toast.LENGTH_SHORT).show();
                mListener.returnToOrderFragment(selectedOrder);
            }

            @Override
            public void onClickDelete(Object selectedObject) {
                Order selectedOrder = (Order) selectedObject;
                mListener.removeOrder(selectedOrder);
                rvAdapter.notifyDataSetChanged();
                Toast.makeText(getActivity(), R.string.orderDeletedMSG, Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        createTaxoparkSpinner();
    }

    public void createTaxoparkSpinner(){
        spnTaxopark.createSpinner(TypeOfSpinner.TAXOPARK, true);
        spnTaxopark.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View itemSelected, int selectedItemPosition, long selectedId) {
                spnTaxopark.saveSpinner(TypeOfSpinner.TAXOPARK);
                MainActivity.ordersStorage.clear();
                MainActivity.ordersStorage.addAll(OrdersSQLHelper.dbOpenHelper.getOrdersList(
                        MainActivity.currentShift.shiftID, spnTaxopark.taxoparkID));
                rvAdapter.notifyDataSetChanged();
            }
            public void onNothingSelected(AdapterView<?> parent) {/*NOP*/}
        });
    }

    public interface OrdersListInterface {
        void removeOrder(Order order);
        void returnToOrderFragment(Order order);
    }
}
