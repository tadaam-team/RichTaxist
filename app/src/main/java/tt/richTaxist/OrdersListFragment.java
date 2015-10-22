package tt.richTaxist;

import android.support.v4.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Calendar;
import tt.richTaxist.DB.OrdersSQLHelper;
import tt.richTaxist.DB.TaxoparksSQLHelper;
import tt.richTaxist.Enums.TypeOfPayment;
import tt.richTaxist.Enums.TypeOfSpinner;

public class OrdersListFragment extends ListFragment {
    private static final String LOG_TAG = "OrdersListFragment";
    private OnOrderListFragmentInteractionListener mListener;
    private static Context context;
    public static ArrayAdapter orderAdapter;
    private SwipeDetector swipeDetector;
    private static Spinner spnTaxopark;
    public static ArrayAdapter spnTaxoparkAdapter;

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
        context = getActivity();
        MainActivity.sortOrdersStorage();
        orderAdapter = new OrderAdapter(context);
        MainActivity.orderAdapterMA = orderAdapter;
        setListAdapter(orderAdapter);

        View rootView = inflater.inflate(R.layout.fragment_orders_list, container, false);
        LayoutParams layoutParams = new LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f);
        rootView.setLayoutParams(layoutParams);

        spnTaxopark = (Spinner) rootView.findViewById(R.id.spnTaxopark);
        createTaxoparkSpinner();

        ListView mListView = (ListView) rootView.findViewById(android.R.id.list);
        swipeDetector = new SwipeDetector();
        mListView.setOnTouchListener(swipeDetector);

        return rootView;
    }

    public static void createTaxoparkSpinner(){
        ArrayList<Taxopark> list = new ArrayList<>();
        list.add(0, new Taxopark(0, "- - -", false, 0));
        list.addAll(TaxoparksSQLHelper.dbOpenHelper.getAllTaxoparks());
        spnTaxoparkAdapter = new ArrayAdapter<>(context, R.layout.list_entry_spinner, list);
        spnTaxopark.setAdapter(spnTaxoparkAdapter);
        spnTaxopark.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View itemSelected, int selectedItemPosition, long selectedId) {
                Storage.saveSpinner(TypeOfSpinner.TAXOPARK, spnTaxopark);
                MainActivity.ordersStorage.clear();
                if (Storage.taxoparkID == 0)
                    MainActivity.ordersStorage.addAll(OrdersSQLHelper.dbOpenHelper.getOrdersByShift(MainActivity.currentShift.shiftID));
                else
                    MainActivity.ordersStorage.addAll(OrdersSQLHelper.dbOpenHelper.getOrdersByShiftAndTaxopark
                            (MainActivity.currentShift.shiftID, Storage.taxoparkID));
                orderAdapter.notifyDataSetChanged();
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        Storage.setPositionOfSpinner(TypeOfSpinner.TAXOPARK, spnTaxoparkAdapter, spnTaxopark, 0);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Storage.saveSpinner(TypeOfSpinner.TAXOPARK, spnTaxopark);
    }

    @Override
    public void onListItemClick(ListView parent, View v, int position, long id) {
        Order selectedOrder = (Order) parent.getItemAtPosition(position);
        if (swipeDetector.swipeDetected()) {
            SwipeDetector.Action action = swipeDetector.getAction();

            switch (action) {
                case LEFT_TO_RIGHT://удалить запись, которую смахнули вправо
                    mListener.removeOrder(selectedOrder);
                    orderAdapter.notifyDataSetChanged();
                    Toast.makeText(context, R.string.orderDeletedMSG, Toast.LENGTH_SHORT).show();
                    break;

                case RIGHT_TO_LEFT://вывести в поля для редактирования и удалить из списка запись, которую смахнули влево
                    mListener.refreshWidgets(selectedOrder);
                    mListener.removeOrder(selectedOrder);
                    //исправленная запись вернется в список по нажатию "ДОБАВИТЬ ЗАКАЗ"
                    orderAdapter.notifyDataSetChanged();
                    Toast.makeText(context, R.string.orderSelectedMSG, Toast.LENGTH_SHORT).show();
                    mListener.switchToOrderFragment();
                    break;

                default:
                    Toast.makeText(context, R.string.orderListSelectErrMSG, Toast.LENGTH_SHORT).show();
            }
        } else {
            //клик по записи выводит ее тост
            Toast.makeText(getActivity(), selectedOrder.toString(), Toast.LENGTH_LONG).show();
        }
    }

    public interface OnOrderListFragmentInteractionListener {
        public void refreshWidgets(Order order);
        public void removeOrder(Order order);
        public void switchToOrderFragment();
    }



    class OrderAdapter extends ArrayAdapter<Order> {
        private final Context context;

        public OrderAdapter(Context context) {
            super(context, android.R.layout.simple_list_item_1, MainActivity.ordersStorage);
            this.context = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Order order = getItem(position);

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.list_entry, parent, false);
            }

            //установим, какие данные из Order отобразятся в полях списка
            TextView textViewMain = (TextView) convertView.findViewById(R.id.entryTextViewMain);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(order.arrivalDateTime);
            textViewMain.setText(String.format("подача: %02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)));

            TextView textViewAdditional = (TextView) convertView.findViewById(R.id.entryTextViewAdditional);
            textViewAdditional.setText(String.format("цена: %d", order.price));

            //назначим картинку обрабатываемой строке списка
            ImageView imageView = (ImageView) convertView.findViewById(R.id.entryIcon);
            if (TypeOfPayment.CASH.equals(order.typeOfPayment))  imageView.setImageResource(R.drawable.ic_cash);
            if (TypeOfPayment.CARD.equals(order.typeOfPayment))  imageView.setImageResource(R.drawable.ic_card);
            if (TypeOfPayment.TIP.equals(order.typeOfPayment))   imageView.setImageResource(R.drawable.ic_tip);

            return convertView;
        }
    }
}
