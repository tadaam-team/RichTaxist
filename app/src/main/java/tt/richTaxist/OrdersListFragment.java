package tt.richTaxist;

import android.support.v4.app.ListFragment;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
import java.util.Calendar;
import tt.richTaxist.Enums.TypeOfPayment;

public class OrdersListFragment extends ListFragment {
    private static final String LOG_TAG = "OrdersListFragment";
    private OnOrderListFragmentInteractionListener mListener;
    private AppCompatActivity mActivity;
    public static ArrayAdapter orderAdapter;
    private SwipeDetector swipeDetector;
    private Spinner spnTaxopark;
    public static ArrayAdapter spnTaxoparkAdapter;

    public OrdersListFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (AppCompatActivity) activity;
        try { mListener = (OnOrderListFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnOrderListInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        orderAdapter = new OrderAdapter(mActivity);
        MainActivity.orderAdapterMA = orderAdapter;
        setListAdapter(orderAdapter);
        setRetainInstance(true);
        //onDestroy() will not be called (but onDetach() still will be, because the fragment is being detached from its current activity).
        //onCreate(Bundle) will not be called since the fragment is not being re-created.
        //onAttach(Activity) and onActivityCreated(Bundle) will still be called.
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_orders_list, container, false);
        LayoutParams layoutParams = new LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f);
        rootView.setLayoutParams(layoutParams);
        MainActivity.sortOrdersStorage();

        spnTaxopark = (Spinner) rootView.findViewById(R.id.spnTaxopark);
        spnTaxoparkAdapter = new ArrayAdapter<>(mActivity, android.R.layout.simple_spinner_item, MainActivity.taxoparks);
        spnTaxopark.setAdapter(spnTaxoparkAdapter);
        spnTaxopark.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View itemSelected, int selectedItemPosition, long selectedId) {
                Storage.saveSpinner(0, spnTaxopark);
                if (spnTaxopark.getSelectedItemId() == 0)
                    MainActivity.ordersStorage.fillOrdersByShift(MainActivity.currentShift.shiftID);
                else
                    MainActivity.ordersStorage.fillOrdersByShiftAndTaxopark(MainActivity.currentShift.shiftID, Storage.taxoparkID);
                orderAdapter.notifyDataSetChanged();
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        Storage.setPositionOfSpinner(0, spnTaxoparkAdapter, spnTaxopark);

        ListView mListView = (ListView) rootView.findViewById(android.R.id.list);
        swipeDetector = new SwipeDetector();
        mListView.setOnTouchListener(swipeDetector);

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Storage.saveSpinner(0, spnTaxopark);
    }

    @Override
    public void onListItemClick(ListView parent, View v, int position, long id) {
        Order selectedOrder = (Order) parent.getItemAtPosition(position);
        if (swipeDetector.swipeDetected()) {
            SwipeDetector.Action action = swipeDetector.getAction();

            switch (action) {
                case LEFT_TO_RIGHT://удалить запись, которую смахнули вправо
                    ((OnOrderListFragmentInteractionListener) mActivity).removeOrder(selectedOrder);
                    orderAdapter.notifyDataSetChanged();
                    Toast.makeText(mActivity, "заказ удален", Toast.LENGTH_SHORT).show();
                    break;

                case RIGHT_TO_LEFT://вывести в поля для редактирования и удалить из списка запись, которую смахнули влево
                    ((OnOrderListFragmentInteractionListener) mActivity).refreshWidgets(selectedOrder);
                    ((OnOrderListFragmentInteractionListener) mActivity).removeOrder(selectedOrder);
                    //исправленная запись вернется в список по нажатию "ДОБАВИТЬ ЗАКАЗ"
                    orderAdapter.notifyDataSetChanged();
                    Toast.makeText(mActivity, "выбран заказ для редактирования", Toast.LENGTH_SHORT).show();
                    ((OnOrderListFragmentInteractionListener) mActivity).switchToOrderFragment();
                    break;

                default:
                    Toast.makeText(mActivity, "ошибка обработки жеста", Toast.LENGTH_SHORT).show();
            }
        } else {
            //клик по записи выводит ее тост
            Toast.makeText(getActivity(), selectedOrder.toString(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnOrderListFragmentInteractionListener {
        //TODO: собрать все прямые вызовы полей и методов MainActivity в общение через этот интерфейс
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
            Log.d("-----", "OrderConvertView: " + String.valueOf(convertView));
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
