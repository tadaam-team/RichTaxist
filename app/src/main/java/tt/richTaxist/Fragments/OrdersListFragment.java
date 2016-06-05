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
import java.util.ArrayList;
import tt.richTaxist.Bricks.CustomSpinner;
import tt.richTaxist.Bricks.CustomSpinner.TypeOfSpinner;
import tt.richTaxist.Bricks.SingleChoiceListDF;
import tt.richTaxist.Constants;
import tt.richTaxist.DB.DataSource;
import tt.richTaxist.R;
import tt.richTaxist.RecyclerViewAdapter;
import tt.richTaxist.Units.Order;

public class OrdersListFragment extends Fragment {
    public static final String TAG = "OrdersListFragment";
    private OrdersListInterface mListener;
    public RecyclerViewAdapter rvAdapter;
    private CustomSpinner spnTaxopark;
    private DataSource dataSource;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        dataSource = new DataSource(context.getApplicationContext());
        try { mListener = (OrdersListInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OrdersListInterface");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_orders_list, container, false);
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        spnTaxopark = (CustomSpinner) rootView.findViewById(R.id.spnTaxopark);

        ArrayList<Order> ordersList = dataSource.getOrdersSource().getOrdersList(mListener.getCurrentShiftId(), spnTaxopark.getSelectedItemId());
        rvAdapter = new RecyclerViewAdapter(ordersList, RecyclerViewAdapter.AdapterDataType.ORDER);
        recyclerView.setAdapter(rvAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        rvAdapter.setListener(new RecyclerViewAdapter.Listener() {
            @Override
            public void onClick(Object selectedObject) {
                Order selectedOrder = (Order) selectedObject;
                Toast.makeText(getContext(), R.string.orderSelectedMSG, Toast.LENGTH_SHORT).show();
                mListener.returnToOrderFragment(selectedOrder);
            }

            @Override
            public void onClickMore(Object selectedObject, int positionInRVList) {
                Order selectedOrder = (Order) selectedObject;
                SingleChoiceListDF dialog = new SingleChoiceListDF();
                Bundle args = new Bundle();
                args.putLong(Constants.OBJECT_ID_EXTRA, selectedOrder.orderID);
                args.putInt(Constants.POSITION_EXTRA, positionInRVList);
                dialog.setArguments(args);
                dialog.show(getChildFragmentManager(), "SingleChoiceListDF");
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
            @Override
            public void onItemSelected(AdapterView<?> parent, View itemSelected, int selectedItemPosition, long selectedId) {
                spnTaxopark.saveSpinner(TypeOfSpinner.TAXOPARK);
                rvAdapter.setObjects(dataSource.getOrdersSource().getOrdersList(mListener.getCurrentShiftId(), spnTaxopark.taxoparkID));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {/*NOP*/}
        });
    }

    public interface OrdersListInterface {
        void returnToOrderFragment(Order selectedOrder);
        long getCurrentShiftId();
    }
}
