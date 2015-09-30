package tt.richTaxist;

import android.support.v4.app.ListFragment;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Calendar;
import tt.richTaxist.DB.OrdersStorage;
import tt.richTaxist.DB.ShiftsStorage;
import android.widget.LinearLayout.LayoutParams;

public class ShiftsListFragment extends ListFragment {
    private static final String LOG_TAG = "ShiftsListFragment";
    private AppCompatActivity mActivity;
    public static ArrayAdapter shiftAdapter;
    private SwipeDetector swipeDetector;
    private Spinner spnMonth, spnTaxopark;

    public ShiftsListFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (AppCompatActivity) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        shiftAdapter = new ShiftAdapter(mActivity);
        FirstScreenActivity.shiftAdapterMA = shiftAdapter;
        setListAdapter(shiftAdapter);
        setRetainInstance(true);
        //onDestroy() will not be called (but onDetach() still will be, because the fragment is being detached from its current activity).
        //onCreate(Bundle) will not be called since the fragment is not being re-created.
        //onAttach(Activity) and onActivityCreated(Bundle) will still be called.
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_shifts_list, container, false);
        LayoutParams layoutParams = new LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f);
        rootView.setLayoutParams(layoutParams);
        MainActivity.sortShiftsStorage();

        spnMonth    = (Spinner) rootView.findViewById(R.id.spnMonth);
        spnTaxopark = (Spinner) rootView.findViewById(R.id.spnTaxopark);

        ArrayAdapter<?> spnMonthAdapter = ArrayAdapter.createFromResource(mActivity, R.array.months,  android.R.layout.simple_spinner_item);
        spnMonthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnMonth.setAdapter(spnMonthAdapter);
        Storage.setPositionOfSpinner(2, spnMonthAdapter, spnMonth);

        ArrayAdapter spnTaxoparkAdapter = new ArrayAdapter<>(mActivity, android.R.layout.simple_spinner_item, MainActivity.taxoparks);
        spnTaxopark.setAdapter(spnTaxoparkAdapter);
        Storage.setPositionOfSpinner(1, spnTaxoparkAdapter, spnTaxopark);

        ListView mListView = (ListView) rootView.findViewById(android.R.id.list);
        swipeDetector = new SwipeDetector();
        mListView.setOnTouchListener(swipeDetector);

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Storage.saveSpinner(2, spnMonth);
        Storage.saveSpinner(0, spnTaxopark);
    }

    @Override
    public void onListItemClick(ListView parent, View v, int position, long id) {
        Shift selectedShift = (Shift) parent.getItemAtPosition(position);
        if (swipeDetector.swipeDetected()) {
            SwipeDetector.Action action = swipeDetector.getAction();

            switch (action) {
                case LEFT_TO_RIGHT://удалить смену, которую смахнули вправо
                    if (!selectedShift.hasOrders()) deleteShift(selectedShift);
                    else openShiftDeleteDialog(selectedShift);
                    break;

                case RIGHT_TO_LEFT://вывести в поля для редактирования смену, которую смахнули влево
                    MainActivity.currentShift = selectedShift;
                    MainActivity.ordersStorage.fillOrdersByShift(MainActivity.currentShift.shiftID);
                    Intent intent = new Intent(mActivity, ShiftTotalsActivity.class);
                    intent.putExtra("author", "FirstScreenActivity");
                    startActivity(intent);
                    Toast.makeText(mActivity, "выбрана смена для редактирования", Toast.LENGTH_SHORT).show();
                    //закрывать стартовый экран можно только после выбора смены, т.к. пользователь может захотеть вернуться в стартовый экран
                    mActivity.finish();
                    break;

                default:
                    Toast.makeText(mActivity, "ошибка обработки жеста", Toast.LENGTH_SHORT).show();
            }
        } else {
            //клик по записи выводит ее тост
            Toast.makeText(mActivity, selectedShift.toString(), Toast.LENGTH_LONG).show();
        }
    }

    private void openShiftDeleteDialog(final Shift shift) {
        AlertDialog.Builder quitDialog = new AlertDialog.Builder(mActivity);
        quitDialog.setTitle("Эта смена не пустая, удалить?");
        quitDialog.setPositiveButton("Да", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteShift(shift);
            }
        });
        quitDialog.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        quitDialog.show();
    }

    private void deleteShift(Shift shift){
        Toast.makeText(mActivity, "смена удалена", Toast.LENGTH_SHORT).show();
        ShiftsStorage.remove(shift);
        OrdersStorage.deleteOrdersByShift(shift);
        MainActivity.shiftsStorage.remove(shift);
        shiftAdapter.notifyDataSetChanged();
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
            textViewMain.setText(String.format("смена %02d.%02d", calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1));

            TextView textViewAdditional = (TextView) convertView.findViewById(R.id.entryTextViewAdditional);
            textViewAdditional.setText(String.format("зп офиц.: %d, зп с чаем: %d", shift.salaryOfficial, shift.salaryPlusBonus));

            //назначим картинку каждой строке списка
            ImageView imageView = (ImageView) convertView.findViewById(R.id.entryIcon);
            if (shift.isClosed()) imageView.setImageResource(R.drawable.ic_lock_closed_black);
            else                  imageView.setImageResource(R.drawable.ic_lock_open_black);

            return convertView;
        }
    }
}
