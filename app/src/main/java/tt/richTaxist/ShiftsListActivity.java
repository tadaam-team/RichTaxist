package tt.richTaxist;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import tt.richTaxist.DB.OrdersStorage;
import tt.richTaxist.DB.ShiftsStorage;

/**
 * Created by Tau on 27.06.2015.
 */
public class ShiftsListActivity extends ListActivity {
    String LOG_TAG = "ShiftsListActivity";

    //TODO добавить фильтрацию списка смен например по месяцам
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if (Storage.showListHint) {
            Toast listHint = Toast.makeText(ShiftsListActivity.this, R.string.listHint, Toast.LENGTH_SHORT);
            listHint.setGravity(Gravity.TOP, 0, 0);
            listHint.show();
        }

        MainActivity.sortShiftsStorage();
        final ArrayAdapter adapter = new ShiftAdapter(this);
        setListAdapter(adapter);

        final SwipeDetector swipeDetector = new SwipeDetector();
        ListView listView = getListView();
        listView.setOnTouchListener(swipeDetector);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Shift selectedShift = (Shift) parent.getItemAtPosition(position);

                if (swipeDetector.swipeDetected()) {
                    SwipeDetector.Action action = swipeDetector.getAction();

                    switch (action) {
                        case LR://удалить смену, которую смахнули вправо
                            if (!selectedShift.hasOrders()) {
                                deleteShift(selectedShift, adapter);
                            }
                            else openShiftDeleteDialog(selectedShift,adapter);
                            break;

                        case RL://вывести в поля для редактирования смену, которую смахнули влево
                            Toast.makeText(ShiftsListActivity.this, "выбрана смена для редактирования", Toast.LENGTH_SHORT).show();
                            //закрывать стартовый экран можно только после выбора смены, т.к. пользователь может захотеть вернуться в стартовый экран
                            FirstScreenActivity.activity.finish();
                            MainActivity.currentShift = selectedShift;
                            MainActivity.ordersStorage.fillOrdersByShift(selectedShift);
                            startActivity(new Intent(ShiftsListActivity.this, ShiftTotalsActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));

                            finish();
                            break;

                        default:
                            Toast.makeText(ShiftsListActivity.this, "ошибка обработки жеста", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    //клик по записи выводит ее тост
                    Toast.makeText(ShiftsListActivity.this, selectedShift.toString(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    class ShiftAdapter extends ArrayAdapter<Shift> {
        private final Context context;

        public ShiftAdapter(Context context) {
            super(context, android.R.layout.simple_list_item_1, MainActivity.shiftsStorage);
            this.context = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Shift shift = getItem(position);

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.list_entry, parent, false);
            }

            //установим, какие данные из Shift отобразятся в полях списка
            TextView textViewMain = (TextView) convertView.findViewById(R.id.entryTextViewMain);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(shift.beginShift);
            textViewMain.setText(String.format("начало смены = %02d.%02d", calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1));

            TextView textViewAdditional = (TextView) convertView.findViewById(R.id.entryTextViewAdditional);
            textViewAdditional.setText(String.format("зп офиц. = %d, зп с чаем = %d", shift.salaryOfficial, shift.salaryPlusBonus));

            //назначим картинку каждой строке списка
            ImageView imageView = (ImageView) convertView.findViewById(R.id.entryIcon);
            if (shift.isClosed()) imageView.setImageResource(R.drawable.ic_lock_closed_black);
            else                  imageView.setImageResource(R.drawable.ic_lock_open_black);

            return convertView;
        }
    }

    private void openShiftDeleteDialog(final Shift shift, final ArrayAdapter adapter) {
        AlertDialog.Builder quitDialog = new AlertDialog.Builder(this);
        quitDialog.setTitle("Эта смена не пустая, удалить?");
        quitDialog.setPositiveButton("Да", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteShift(shift,adapter);
            }
        });
        quitDialog.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        quitDialog.show();
    }

    private void deleteShift(Shift shift, ArrayAdapter adapter){
        Toast.makeText(ShiftsListActivity.this, "смена удалена", Toast.LENGTH_SHORT).show();
        ShiftsStorage.remove(shift);
        OrdersStorage.deleteOrdersByShift(shift);
        MainActivity.shiftsStorage.remove(shift);
        adapter.notifyDataSetChanged();
    }
}
