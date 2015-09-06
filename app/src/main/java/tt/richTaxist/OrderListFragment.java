package tt.richTaxist;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

//import tt.richTaxist.dummy.DummyContent;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnOrderListFragmentInteractionListener}
 * interface.
 */
public class OrderListFragment extends Fragment implements AbsListView.OnItemClickListener {
    private OnOrderListFragmentInteractionListener mListener;
    private AbsListView mListView;
    private ArrayAdapter mAdapter;

    public OrderListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new OrderAdapter(getActivity());
        MainActivity.mAdapter = mAdapter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_list, container, false);

        if (Storage.showListHint) {
            Toast listHint = Toast.makeText(getActivity(), R.string.listHint, Toast.LENGTH_SHORT);
            listHint.setGravity(Gravity.TOP, 0, 0);
            listHint.show();
        }
        MainActivity.sortOrdersStorage();

        mListView = (AbsListView) view.findViewById(android.R.id.list);
        // Set the adapter
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        final SwipeDetector swipeDetector = new SwipeDetector();
        mListView.setOnTouchListener(swipeDetector);
//        mListView.setOnItemClickListener(this);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Order selectedOrder = (Order) parent.getItemAtPosition(position);

                if (swipeDetector.swipeDetected()) {
                    SwipeDetector.Action action = swipeDetector.getAction();

                    switch (action) {
                        case LR://удалить запись, которую смахнули вправо
                            Toast.makeText(getActivity(), "заказ удален", Toast.LENGTH_SHORT).show();
                            MainActivity.ordersStorage.remove(selectedOrder);
                            mAdapter.notifyDataSetChanged();
                            break;

                        case RL://вывести в поля для редактирования и удалить из списка запись, которую смахнули влево
                            Toast.makeText(getActivity(), "выбран заказ для редактирования", Toast.LENGTH_SHORT).show();
                            MainActivity.refreshWidgets(selectedOrder);
                            MainActivity.ordersStorage.remove(selectedOrder);
                            mAdapter.notifyDataSetChanged();
//                            finish();
                            break;
                        //исправленная запись вернется в список по нажатию "ДОБАВИТЬ ЗАКАЗ"

                        default:
                            Toast.makeText(getActivity(), "ошибка обработки жеста", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    //клик по записи выводит ее тост
                    Toast.makeText(getActivity(), selectedOrder.toString(), Toast.LENGTH_LONG).show();
                }
            }
        });
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnOrderListFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnOrderListInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
//            mListener.onOrderListFragmentInteraction(DummyContent.ITEMS.get(position).id);
        }
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */

    public interface OnOrderListFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onOrderListFragmentInteraction(String id);
    }

    class OrderAdapter extends ArrayAdapter<Order> {
        private final Context context;

        public OrderAdapter(Context context) {
            super(context, android.R.layout.simple_list_item_1, MainActivity.ordersStorage);
            this.context = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //TODO: вызов этого метода здесь не корректен, т.к. мы назначаем размер списку каждый раз, когда добавляем в него очередную строку
            //однако я пока не вижу, как получить ссылку на нужный ViewGroup вне метода getView
            Storage.measureScreenWidth(context, parent);
            Order order = getItem(position);

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.list_entry, parent, false);
            }

            //установим, какие данные из Order отобразятся в полях списка
            TextView textViewMain = (TextView) convertView.findViewById(R.id.entryTextViewMain);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(order.arrivalDateTime);
            textViewMain.setText(String.format("подача = %02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)));

            TextView textViewAdditional = (TextView) convertView.findViewById(R.id.entryTextViewAdditional);
            textViewAdditional.setText(String.format("цена = %d", order.price));

            //назначим картинку обрабатываемой строке списка
            ImageView imageView = (ImageView) convertView.findViewById(R.id.entryIcon);
            if (TypeOfPayment.CASH.equals(order.typeOfPayment))  imageView.setImageResource(R.drawable.ic_cash);
            if (TypeOfPayment.CARD.equals(order.typeOfPayment))  imageView.setImageResource(R.drawable.ic_card);
            if (TypeOfPayment.TIP.equals(order.typeOfPayment))   imageView.setImageResource(R.drawable.ic_tip);

            return convertView;
        }
    }
}
