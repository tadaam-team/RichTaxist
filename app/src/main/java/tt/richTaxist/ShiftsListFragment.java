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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Calendar;
import tt.richTaxist.DB.OrdersSQLHelper;
import tt.richTaxist.DB.ShiftsSQLHelper;
import tt.richTaxist.DB.TaxoparksSQLHelper;
import tt.richTaxist.Enums.TypeOfSpinner;
import android.widget.LinearLayout.LayoutParams;

public class ShiftsListFragment extends ListFragment {
    private static final String LOG_TAG = "ShiftsListFragment";
    private static AppCompatActivity mActivity;
    public static ArrayAdapter shiftAdapter;
    private SwipeDetector swipeDetector;
    private static Spinner spnMonth, spnTaxopark;
    public static ArrayAdapter spnTaxoparkAdapter;

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

        ArrayAdapter<?> spnMonthAdapter = ArrayAdapter.createFromResource(mActivity, R.array.months, android.R.layout.simple_spinner_item);
        spnMonthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnMonth.setAdapter(spnMonthAdapter);
        Storage.setPositionOfSpinner(TypeOfSpinner.MONTH, spnMonthAdapter, spnMonth, 0);

        createTaxoparkSpinner();

        ListView mListView = (ListView) rootView.findViewById(android.R.id.list);
        swipeDetector = new SwipeDetector();
        mListView.setOnTouchListener(swipeDetector);

        return rootView;
    }

    public static void createTaxoparkSpinner(){
        ArrayList<Taxopark> list = new ArrayList<>();
        list.add(0, new Taxopark("- - -", false, 0));
        list.addAll(TaxoparksSQLHelper.dbOpenHelper.getAllTaxoparks());
        spnTaxoparkAdapter = new ArrayAdapter<>(mActivity, android.R.layout.simple_spinner_item, list);
        spnTaxoparkAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnTaxopark.setAdapter(spnTaxoparkAdapter);
        spnTaxopark.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View itemSelected, int selectedItemPosition, long selectedId) {
                Storage.saveSpinner(TypeOfSpinner.TAXOPARK, spnTaxopark);
                if (MainActivity.currentShift != null) {
                    if (spnTaxopark.getSelectedItemId() == 0) {
                        MainActivity.ordersStorage.clear();
                        MainActivity.ordersStorage.addAll(OrdersSQLHelper.dbOpenHelper.getOrdersByShift(MainActivity.currentShift.shiftID));
                    } else {
                        MainActivity.ordersStorage.clear();
                        MainActivity.ordersStorage.addAll(OrdersSQLHelper.dbOpenHelper.getOrdersByShiftAndTaxopark
                                (MainActivity.currentShift.shiftID, Storage.taxoparkID));

                    }
                    shiftAdapter.notifyDataSetChanged();
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        Storage.setPositionOfSpinner(TypeOfSpinner.TAXOPARK, spnTaxoparkAdapter, spnTaxopark, 0);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Storage.saveSpinner(TypeOfSpinner.MONTH, spnMonth);
        Storage.saveSpinner(TypeOfSpinner.TAXOPARK, spnTaxopark);
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
                    MainActivity.ordersStorage.clear();
                    MainActivity.ordersStorage.addAll(OrdersSQLHelper.dbOpenHelper.getOrdersByShift(MainActivity.currentShift.shiftID));
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
        ShiftsSQLHelper.dbOpenHelper.remove(shift);
        OrdersSQLHelper.dbOpenHelper.deleteOrdersByShift(shift);
        MainActivity.shiftsStorage.remove(shift);
        shiftAdapter.notifyDataSetChanged();
        Toast.makeText(mActivity, "смена удалена", Toast.LENGTH_SHORT).show();
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
