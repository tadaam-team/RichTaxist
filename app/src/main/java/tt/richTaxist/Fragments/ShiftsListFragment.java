package tt.richTaxist.Fragments;

import android.content.res.Resources;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import java.util.GregorianCalendar;
import tt.richTaxist.Bricks.DateTimeRangeFrag;
import tt.richTaxist.DB.OrdersSQLHelper;
import tt.richTaxist.Units.Shift;
import tt.richTaxist.DB.ShiftsSQLHelper;
import tt.richTaxist.Units.Taxopark;
import tt.richTaxist.DB.TaxoparksSQLHelper;
import tt.richTaxist.Enums.ActivityState;
import tt.richTaxist.Enums.TypeOfSpinner;
import tt.richTaxist.FirstScreenActivity;
import tt.richTaxist.MainActivity;
import tt.richTaxist.R;
import tt.richTaxist.ShiftTotalsActivity;
import tt.richTaxist.Storage;
import tt.richTaxist.SwipeDetector;
import android.widget.LinearLayout.LayoutParams;

public class ShiftsListFragment extends ListFragment implements DateTimeRangeFrag.OnDateTimeRangeFragmentInteractionListener {
    private static final String LOG_TAG = "ShiftsListFragment";
    private static FragmentActivity mActivity;
    public static ArrayAdapter shiftAdapter;
    private SwipeDetector swipeDetector;
    private static Spinner spnTaxopark;
    public static ArrayAdapter spnTaxoparkAdapter;
    private static DateTimeRangeFrag dateTimeRangeFrag;

    public ShiftsListFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mActivity = getActivity();
        shiftAdapter = new ShiftAdapter(mActivity);
        FirstScreenActivity.shiftAdapterMA = shiftAdapter;
        setListAdapter(shiftAdapter);

        View rootView = inflater.inflate(R.layout.fragment_shifts_list, container, false);
        LayoutParams layoutParams = new LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f);
        rootView.setLayoutParams(layoutParams);
        MainActivity.sortShiftsStorage();

        spnTaxopark = (Spinner) rootView.findViewById(R.id.spnTaxopark);
        createTaxoparkSpinner();

        ListView mListView = (ListView) rootView.findViewById(android.R.id.list);
        swipeDetector = new SwipeDetector();
        mListView.setOnTouchListener(swipeDetector);

        FragmentManager fragmentManager = getChildFragmentManager();//getFragmentManager()????
        if (((FirstScreenActivity) mActivity).activityState == ActivityState.PORT_2 ||
                ((FirstScreenActivity) mActivity).activityState == ActivityState.LAND_2) {
            //применяется только когда лист единственный на экране
            dateTimeRangeFrag = (DateTimeRangeFrag) fragmentManager.findFragmentByTag("dateTimeRangeFrag");
            if (dateTimeRangeFrag == null) {
                dateTimeRangeFrag = new DateTimeRangeFrag();
                FragmentTransaction ft = fragmentManager.beginTransaction();
                ft.add(R.id.container_shift_list, dateTimeRangeFrag, "dateTimeRangeFrag");
                ft.commit();
            }
        }
        else {
            //когда лист НЕ единственный на экране, фрагмент следует убрать
            dateTimeRangeFrag = (DateTimeRangeFrag) fragmentManager.findFragmentByTag("dateTimeRangeFrag");
            if (dateTimeRangeFrag != null) {
                FragmentTransaction ft = fragmentManager.beginTransaction();
                ft.remove(dateTimeRangeFrag);
                ft.commit();
            }
        }
        return rootView;
    }

    public static void createTaxoparkSpinner(){
        ArrayList<Taxopark> list = new ArrayList<>();
        list.add(0, new Taxopark(0, "- - -", false, 0));
        list.addAll(TaxoparksSQLHelper.dbOpenHelper.getAllTaxoparks());
        spnTaxoparkAdapter = new ArrayAdapter<>(mActivity, R.layout.list_entry_spinner, list);
        spnTaxopark.setAdapter(spnTaxoparkAdapter);
        spnTaxopark.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View itemSelected, int selectedItemPosition, long selectedId) {
                Storage.saveSpinner(TypeOfSpinner.TAXOPARK, spnTaxopark);
                if (MainActivity.currentShift != null) {
                    MainActivity.shiftsStorage.clear();
                    Calendar rangeStart, rangeEnd;
                    if (dateTimeRangeFrag != null) {
                        rangeStart = dateTimeRangeFrag.getRangeStart();
                        rangeEnd = dateTimeRangeFrag.getRangeEnd();
                    } else {
                        rangeStart = new GregorianCalendar(2015, Calendar.JANUARY, 1);
                        rangeEnd = Calendar.getInstance();
                    }
                    MainActivity.shiftsStorage.addAll(ShiftsSQLHelper.dbOpenHelper.getShiftsInRangeByTaxopark(
                            rangeStart, rangeEnd, Storage.youngIsOnTop, Storage.taxoparkID));
                    shiftAdapter.notifyDataSetChanged();
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {/*NOP*/}
        });
        Storage.setPositionOfSpinner(TypeOfSpinner.TAXOPARK, spnTaxoparkAdapter, spnTaxopark, 0);
    }

    @Override
    public void calculate(Calendar rangeStart, Calendar rangeEnd) {
        MainActivity.shiftsStorage.clear();
        Storage.saveSpinner(TypeOfSpinner.TAXOPARK, spnTaxopark);
        MainActivity.shiftsStorage.addAll(ShiftsSQLHelper.dbOpenHelper.getShiftsInRangeByTaxopark(
                rangeStart, rangeEnd, Storage.youngIsOnTop, Storage.taxoparkID));
    }

    @Override
    public void refreshControls() {
        shiftAdapter.notifyDataSetChanged();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
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
                    Toast.makeText(mActivity, R.string.shiftSelectedMSG, Toast.LENGTH_SHORT).show();
                    //закрывать стартовый экран можно только после выбора смены, т.к. пользователь может захотеть вернуться в стартовый экран
                    mActivity.finish();
                    break;

                default:
                    Toast.makeText(mActivity, R.string.gestureErrorMSG, Toast.LENGTH_SHORT).show();
            }
        } else {
            //клик по записи выводит ее тост
            Toast.makeText(mActivity, selectedShift.toString(), Toast.LENGTH_LONG).show();
        }
    }

    private void openShiftDeleteDialog(final Shift shift) {
        AlertDialog.Builder quitDialog = new AlertDialog.Builder(mActivity);
        quitDialog.setTitle(R.string.shiftNotEmptyMSG);
        quitDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteShift(shift);
            }
        });
        quitDialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {/*NOP*/}
        });
        quitDialog.show();
    }

    private void deleteShift(Shift shift){
        ShiftsSQLHelper.dbOpenHelper.remove(shift);
        MainActivity.shiftsStorage.remove(shift);
        shiftAdapter.notifyDataSetChanged();
        Toast.makeText(mActivity, R.string.shiftDeletedMSG, Toast.LENGTH_SHORT).show();
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
            Resources res = MainActivity.context.getResources();
            TextView textViewMain = (TextView) convertView.findViewById(R.id.entryTextViewMain);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(shift.beginShift);
            textViewMain.setText(String.format(res.getString(R.string.shift)+" %02d.%02d",
                    calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1));

            TextView textViewAdditional = (TextView) convertView.findViewById(R.id.entryTextViewAdditional);
            textViewAdditional.setText(String.format(res.getString(R.string.salaryOfficialShort) + ": %d,\n" +
                    res.getString(R.string.salaryUnofficialShort) + ": %d", shift.salaryOfficial, shift.salaryUnofficial));

            //назначим картинку каждой строке списка
            ImageView imageView = (ImageView) convertView.findViewById(R.id.entryIcon);
            if (shift.isClosed()) imageView.setImageResource(R.drawable.ic_lock_closed_black);
            else                  imageView.setImageResource(R.drawable.ic_lock_open_black);

            return convertView;
        }
    }
}
