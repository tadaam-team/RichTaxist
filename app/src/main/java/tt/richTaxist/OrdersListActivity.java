package tt.richTaxist;

import android.app.ListActivity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;


/**
 * Created by Tau on 28.06.2015.
 */
public class OrdersListActivity extends ListActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //TODO: совместить лист заказов с MainActivity, если позволяет место
        if (Storage.showListHint) {
            Toast listHint = Toast.makeText(OrdersListActivity.this, R.string.listHint, Toast.LENGTH_SHORT);
            listHint.setGravity(Gravity.TOP, 0, 0);
            listHint.show();
        }
        //это очень странно, но сортировать список можно даже в последней строке onCreate.
        //сортировка будет правильной, хотя состояние ordersStorage опрашивается при создании ArrayAdapter adapter
        MainActivity.sortOrdersStorage();
        final ArrayAdapter adapter = new OrderAdapter(this);
        setListAdapter(adapter);

        final SwipeDetector swipeDetector = new SwipeDetector();
        ListView listView = getListView();
        listView.setOnTouchListener(swipeDetector);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Order selectedOrder = (Order) parent.getItemAtPosition(position);

                if (swipeDetector.swipeDetected()) {
                    SwipeDetector.Action action = swipeDetector.getAction();

                    switch (action) {
                        case LR://удалить запись, которую смахнули вправо
                            Toast.makeText(OrdersListActivity.this, "заказ удален", Toast.LENGTH_SHORT).show();
                            MainActivity.ordersStorage.remove(selectedOrder);
                            adapter.notifyDataSetChanged();
                            break;

                        case RL://вывести в поля для редактирования и удалить из списка запись, которую смахнули влево
                            Toast.makeText(OrdersListActivity.this, "выбран заказ для редактирования", Toast.LENGTH_SHORT).show();
                            MainActivity.refreshWidgets(selectedOrder);
                            MainActivity.ordersStorage.remove(selectedOrder);
                            finish();
                            break;
                        //исправленная запись вернется в список по нажатию "ДОБАВИТЬ ЗАКАЗ"

                        default:
                            Toast.makeText(OrdersListActivity.this, "ошибка обработки жеста", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    //клик по записи выводит ее тост
                    Toast.makeText(OrdersListActivity.this, selectedOrder.toString(), Toast.LENGTH_LONG).show();
                }
            }
        });
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
            textViewMain.setText(String.format("время подачи = %02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)));

            TextView textViewAdditional = (TextView) convertView.findViewById(R.id.entryTextViewAdditional);
            textViewAdditional.setText(String.format("цена = %d, тип оплаты = %s", order.price, order.typeOfPayment.toString()));

            //назначим картинку обрабатываемой строке списка
            ImageView imageView = (ImageView) convertView.findViewById(R.id.entryIcon);
            if (TypeOfPayment.CASH.equals(order.typeOfPayment))  imageView.setImageResource(R.drawable.ic_cash);
            if (TypeOfPayment.CARD.equals(order.typeOfPayment))  imageView.setImageResource(R.drawable.ic_card);
            if (TypeOfPayment.TIP.equals(order.typeOfPayment))   imageView.setImageResource(R.drawable.ic_tip);

            return convertView;
        }
    }
}
