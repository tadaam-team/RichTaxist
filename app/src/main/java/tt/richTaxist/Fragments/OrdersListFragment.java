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
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import java.util.ArrayList;
import tt.richTaxist.Bricks.CustomSpinner;
import tt.richTaxist.Bricks.CustomSpinner.TypeOfSpinner;
import tt.richTaxist.DB.OrdersSQLHelper;
import tt.richTaxist.DB.TaxoparksSQLHelper;
import tt.richTaxist.MainActivity;
import tt.richTaxist.R;
import tt.richTaxist.RecyclerViewAdapter;
import tt.richTaxist.Util;
import tt.richTaxist.Units.Order;
import tt.richTaxist.Units.Taxopark;

public class OrdersListFragment extends Fragment {
    public static final String FRAGMENT_TAG = "OrdersListFragment";
    private static final String LOG_TAG = "OrdersListFragment";
    private OnOrderListFragmentInteractionListener mListener;
    public RecyclerViewAdapter adapter;
    private Spinner spnTaxopark;

    public OrdersListFragment() { }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try { mListener = (OnOrderListFragmentInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement " + mListener.getClass().getName());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        MainActivity.sortOrdersStorage();
        View rootView = inflater.inflate(R.layout.fragment_orders_list, container, false);
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        spnTaxopark = (Spinner) rootView.findViewById(R.id.spnTaxopark);

        //TODO: get correct dataSource
//        OrdersSQLHelper.dbOpenHelper.getOrdersByShiftAndTaxopark();
        adapter = new RecyclerViewAdapter(MainActivity.ordersStorage, RecyclerViewAdapter.AdapterDataType.ORDER);
        recyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
//        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        recyclerView.setLayoutManager(layoutManager);
        adapter.setListener(new RecyclerViewAdapter.Listener() {
            @Override
            public void onClick(Object selectedObject) {
                Order selectedOrder = (Order) selectedObject;
                mListener.removeOrder(selectedOrder);
                //исправленная запись вернется в список по нажатию "ДОБАВИТЬ ЗАКАЗ"
                adapter.notifyDataSetChanged();
                Toast.makeText(getActivity(), R.string.orderSelectedMSG, Toast.LENGTH_SHORT).show();
                mListener.returnToOrderFragment(selectedOrder);
            }

            @Override
            public void onClickDelete(Object selectedObject) {
                Order selectedOrder = (Order) selectedObject;
                mListener.removeOrder(selectedOrder);
                adapter.notifyDataSetChanged();
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
        ArrayList<Taxopark> list = new ArrayList<>();
        list.add(0, new Taxopark(0, "- - -", false, 0));
        list.addAll(TaxoparksSQLHelper.dbOpenHelper.getAllTaxoparks());
        ArrayAdapter spnTaxoparkAdapter = new ArrayAdapter<>(getActivity(), R.layout.list_entry_spinner, list);
        spnTaxopark.setAdapter(spnTaxoparkAdapter);
        spnTaxopark.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View itemSelected, int selectedItemPosition, long selectedId) {
                CustomSpinner.saveSpinner(TypeOfSpinner.TAXOPARK, spnTaxopark);
                MainActivity.ordersStorage.clear();
                if (CustomSpinner.taxoparkID == 0) {
                    MainActivity.ordersStorage.addAll(OrdersSQLHelper.dbOpenHelper.getOrdersByShift(MainActivity.currentShift.shiftID));
                } else {
                    MainActivity.ordersStorage.addAll(OrdersSQLHelper.dbOpenHelper.getOrdersByShiftAndTaxopark(
                            MainActivity.currentShift.shiftID, CustomSpinner.taxoparkID));
                }
                adapter.notifyDataSetChanged();
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        CustomSpinner.setPositionOfSpinner(TypeOfSpinner.TAXOPARK, spnTaxoparkAdapter, spnTaxopark, 0);
    }

    public interface OnOrderListFragmentInteractionListener {
        void removeOrder(Order order);
        void returnToOrderFragment(Order order);
    }
}
