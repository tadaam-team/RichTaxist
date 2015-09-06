package tt.richTaxist;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TimePicker;
import android.widget.Toast;
import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import tt.richTaxist.Bricks.CustomTimePicker;
import tt.richTaxist.Bricks.F_CustomDatePicker;
import tt.richTaxist.ChatClient.ChatLoginActivity;
import tt.richTaxist.DB.OrdersStorageList;

/**
 * Created by Tau on 08.06.2015.
 */

public class MainActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener, F_CustomDatePicker.OnFragmentInteractionListener,
        OrderFragment.OnOrderFragmentInteractionListener, OrderListFragment.OnOrderListFragmentInteractionListener {
    private static final String LOG_TAG = "ChatActivity";
    public static Context context;
    public final static int GET_DATA_FROM_ORDER_ACTIVITY = 1;
    final static ArrayList<Shift> shiftsStorage = new ArrayList<>();
    public static Shift currentShift;
    final static OrdersStorageList ordersStorage = new OrdersStorageList();
    DatePickerDialog.OnDateSetListener dateSetListener;
    TimePickerDialog.OnTimeSetListener timeSetListener;

    private static Calendar arrivalDateTime;
    private static Button dateButton;
    private static Button timeButton;
//    private static CustomTimePicker timePicker;
//    private static LinearLayout timePickerPlaceHolder;
//    private static EditText timeInput;
    public static ArrayAdapter mAdapter;
    private static RadioGroup typeOfPaymentUI;
    private static EditText priceUI, noteUI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        Storage.measureScreenWidth(context, (ViewGroup) findViewById(R.id.activity_main));
        dateSetListener = MainActivity.this;
        timeSetListener = MainActivity.this;

        arrivalDateTime = Calendar.getInstance();

        dateButton = (Button) findViewById(R.id.btnDate);
        dateButton.setText(getStringDateFromCal(arrivalDateTime));
        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePD = DatePickerDialog.newInstance(dateSetListener, arrivalDateTime.get(Calendar.YEAR), arrivalDateTime.get(Calendar.MONTH), arrivalDateTime.get(Calendar.DAY_OF_MONTH), false);
                datePD.setVibrate(false);
                datePD.setYearRange(2015, 2017);
                datePD.setCloseOnSingleTapDay(true);
                datePD.show(getSupportFragmentManager(), "datepicker");
            }
        });
        if (Storage.typeOfDateInput == TypeOfInput.SPINNER) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            F_CustomDatePicker myFragment = new F_CustomDatePicker();
            fragmentTransaction.add(R.id.datePickerPlaceHolder, myFragment);
            fragmentTransaction.commit();
        }

        timeButton = (Button) findViewById(R.id.btnTime);
        timeButton.setText(getStringTimeFromCal(arrivalDateTime));
        timeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePD = TimePickerDialog.newInstance(timeSetListener, arrivalDateTime.get(Calendar.HOUR_OF_DAY), arrivalDateTime.get(Calendar.MINUTE), false, false);
                timePD.setVibrate(false);
                timePD.setCloseOnSingleTapMinute(Storage.singleTapTimePick);
                timePD.show(getSupportFragmentManager(), "timepicker");
            }
        });
//        timePickerPlaceHolder = (LinearLayout) findViewById(R.id.timePickerPlaceHolder);
//        timeInput = (EditText) findViewById(R.id.timeInput);
//        timeInput.setText(getStringTimeFromCal(arrivalDateTime));
//        View.OnKeyListener okl = new View.OnKeyListener() {
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                if (event.getAction() == KeyEvent.ACTION_DOWN && (keyCode == KeyEvent.KEYCODE_ENTER)) {
//                    // сохраняем текст, введенный до нажатия Enter в переменную
//                    parseTimeInput();
//                    return true;
//                }
//                return false;
//            }
//        };
//        timeInput.setOnKeyListener(okl);
//        refreshInputStyle();//плохо, но в процессе обновления создается timePicker при необходимости


        typeOfPaymentUI = (RadioGroup) findViewById(R.id.payTypeRadioGroup);
        priceUI = (EditText) findViewById(R.id.price);
        noteUI  = (EditText) findViewById(R.id.note);
        findViewById(R.id.buttonAddNew).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(context, OrderActivity.class), GET_DATA_FROM_ORDER_ACTIVITY);
            }
        });
        findViewById(R.id.buttonClearForm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshWidgets(null);
                Toast.makeText(context, "форма очищена", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onOrderFragmentInteraction(Uri uri){

    }

    @Override
    public void onOrderListFragmentInteraction(String id){

    }

    //вызывается после завершения ввода в диалоге даты
    //TODO подумать над переносом в сторож
    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
        arrivalDateTime.set(Calendar.YEAR, year);
        arrivalDateTime.set(Calendar.MONTH, month);
        arrivalDateTime.set(Calendar.DAY_OF_MONTH, day);
        dateButton.setText(getStringDateFromCal(arrivalDateTime));
//        dateInput.setText(getStringDateFromCal(arrivalDateTime));
    }

    //вызывается после завершения ввода в диалоге времени
    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
        arrivalDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
        arrivalDateTime.set(Calendar.MINUTE, minute);
        timeButton.setText(getStringTimeFromCal(arrivalDateTime));
//        timeInput.setText(getStringTimeFromCal(arrivalDateTime));
    }

    //TODO подумать над переносом в сторож
    private static String getStringDateFromCal(Calendar date){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date.getTimeInMillis());
        return String.format("%02d.%02d.%02d", cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR) % 100);
    }

    private static String getStringTimeFromCal(Calendar date){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date.getTimeInMillis());
        return String.format("%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
    }

//    private void parseTimeInput(){
//        String newTime = timeInput.getText().toString();
//        newTime = newTime.replace(":","");
//        newTime = newTime.replace(",","");
//        newTime = newTime.replace(".","");
//
//        String newMinutes = "";
//        String newHours = "";
//
//        switch(newTime.length()){
//            case 4:
//                newHours += newTime.charAt(0);
//                newHours += newTime.charAt(1);
//                arrivalDateTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(newHours));
//                newMinutes += newTime.charAt(2);
//                newMinutes += newTime.charAt(3);
//                arrivalDateTime.set(Calendar.MINUTE, Integer.parseInt(newMinutes));
//                break;
//            case 3:
//                newHours += newTime.charAt(0);
//                arrivalDateTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(newHours));
//                newMinutes += newTime.charAt(1);
//                newMinutes += newTime.charAt(2);
//                arrivalDateTime.set(Calendar.MINUTE, Integer.parseInt(newMinutes));
//                break;
//            case 2:
//                arrivalDateTime.set(Calendar.HOUR_OF_DAY, 0);
//                newMinutes += newTime.charAt(0);
//                newMinutes += newTime.charAt(1);
//                arrivalDateTime.set(Calendar.MINUTE, Integer.parseInt(newMinutes));
//                break;
//            case 1:
//                arrivalDateTime.set(Calendar.HOUR_OF_DAY, 0);
//                newMinutes += newTime.charAt(0);
//                arrivalDateTime.set(Calendar.MINUTE, Integer.parseInt(newMinutes));
//                break;
//            default:
//                Toast.makeText(getApplicationContext(), "только цифры, пожалуйста", Toast.LENGTH_SHORT).show();
//                break;
//        }
//
//        timeInput.setText(getStringTimeFromCal(arrivalDateTime));
//    }

    //TODO перейти от setVisibility к диспетчеру фрагментов
//    public static void refreshInputStyle(){
//        if (dateButton != null) {
//            switch (Storage.typeOfDateInput){
//                case BUTTON:
//                    Toast.makeText(context, "date widgets are not ready", Toast.LENGTH_SHORT).show();
//                    dateButton.setVisibility(View.VISIBLE);
//                    datePickerPlaceHolder.setVisibility(View.GONE);
//                    break;
//                case SPINNER:
//                    Toast.makeText(context, "date widgets are not ready", Toast.LENGTH_SHORT).show();
//                    dateButton.setVisibility(View.GONE);
//                    datePickerPlaceHolder.setVisibility(View.VISIBLE);
//                    break;
//                case TEXT_INPUT:
//                    Toast.makeText(context, "date widgets are not ready", Toast.LENGTH_SHORT).show();
//                    dateButton.setVisibility(View.GONE);
//                    datePickerPlaceHolder.setVisibility(View.GONE);
//                    break;
//            }
//        }

//        if (timeButton != null && timeInput != null && timePickerPlaceHolder != null) {
//            switch (Storage.typeOfTimeInput) {
//                case BUTTON:
//                    timeButton.setVisibility(View.VISIBLE);
//                    timePickerPlaceHolder.setVisibility(View.GONE);
//                    timeInput.setVisibility(View.GONE);
//                    break;
//                case SPINNER:
//                    timeButton.setVisibility(View.GONE);
//                    createAndInsertTimePicker();
//                    timeInput.setVisibility(View.GONE);
//                    break;
//                case TEXT_INPUT:
//                    timeButton.setVisibility(View.GONE);
//                    timePickerPlaceHolder.setVisibility(View.GONE);
//                    timeInput.setVisibility(View.VISIBLE);
//                    break;
//            }
//        }
//    }


    //приходится каждый раз создавать новый timePicker когда юзер заходит в настройки из Заказа чтобы обновились виджеты и интервалы спиннера
//    private static void createAndInsertTimePicker(){
//        timePicker = new CustomTimePicker(context);
//        timePicker.setIs24HourView(true);
//        timePicker.setCurrentHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));//timePicker: 1:00 --> 13:00
//        //вызывается после завершения ввода времени через спиннер
//        TimePicker.OnTimeChangedListener timeChangedListener = new TimePicker.OnTimeChangedListener() {
//            @Override
//            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
//                arrivalDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
//                arrivalDateTime.set(Calendar.MINUTE, minute);
//                //не нравится мне, что тут приходится присваивать значение и timeInput тоже, но пока не будет диспетчера фрагментов, работем так
//                timeButton.setText(getStringTimeFromCal(arrivalDateTime));
////                timeInput.setText(getStringTimeFromCal(arrivalDateTime));
//            }
//        };
//        timePicker.setOnTimeChangedListener(timeChangedListener);
////        int sizeDP = (int) (120 * getResources().getDisplayMetrics().density + 0.5f);
////        timePicker.setMinimumHeight(sizeDP);//не работает. сейчас стоит костыль предустановленной высоты лейаута-плейсхолдера
//        timePickerPlaceHolder.removeAllViews();
//        timePickerPlaceHolder.addView(timePicker);
//    }

    static void sortOrdersStorage(){
        Log.d(LOG_TAG, "youngIsOnTop: " + Storage.youngIsOnTop);
        for (int i = ordersStorage.size() - 1; i > 0; i--) {
            for (int j = 0; j < i; j++) {
                Date currentDate = ordersStorage.get(j).arrivalDateTime;
                Date nextDate = ordersStorage.get(j + 1).arrivalDateTime;
                //ниже проверяем НАРУШЕНИЕ порядка, а не его правильность. если проверка true, то переставляем
                //before = самый свежий должен быть наверху, after = самый старый должен быть наверху
                if (Storage.youngIsOnTop ? !currentDate.after(nextDate) : !currentDate.before(nextDate)) {
                    Order tmp = ordersStorage.get(j);
                    ordersStorage.set(j, ordersStorage.get(j + 1));
                    ordersStorage.set(j + 1, tmp);
                }
            }
        }
    }

    static void sortShiftsStorage(){
        Log.d(LOG_TAG, "youngIsOnTop: " + Storage.youngIsOnTop);
        for (int i = shiftsStorage.size() - 1; i > 0; i--) {
            for (int j = 0; j < i; j++) {
                Date currentShiftStart = shiftsStorage.get(j).beginShift;
                Date nextShiftStart = shiftsStorage.get(j + 1).beginShift;
                //ниже проверяем НАРУШЕНИЕ порядка, а не его правильность. если проверка true, то переставляем
                //before = самый свежий должен быть наверху, after = самый старый должен быть наверху
                if (Storage.youngIsOnTop ? !currentShiftStart.after(nextShiftStart) : !currentShiftStart.before(nextShiftStart)) {
                    Shift tmp = shiftsStorage.get(j);
                    shiftsStorage.set(j, shiftsStorage.get(j + 1));
                    shiftsStorage.set(j + 1, tmp);
                }
            }
        }
    }

    TypeOfPayment getRadioState(){
        switch (typeOfPaymentUI.getCheckedRadioButtonId()){
            case R.id.choiceCash:  return TypeOfPayment.CASH;
            case R.id.choiceCard:   return TypeOfPayment.CARD;
            case R.id.choiceBonus:  return TypeOfPayment.TIP;
            default:                throw new IllegalArgumentException();//"ошибка обработки типа оплаты";
        }
    }

    //этот метод получает заказ из листа, когда юзер делает свайп влево и обнуляет поля ввода, если переданный заказ == null
    public static void refreshWidgets(Order receivedOrder){
        if (receivedOrder != null) {
            arrivalDateTime.setTime(receivedOrder.arrivalDateTime);
            priceUI.setText(String.valueOf(receivedOrder.price));
            switch (receivedOrder.typeOfPayment) {
                case CASH:  typeOfPaymentUI.check(R.id.choiceCash); break;
                case CARD:  typeOfPaymentUI.check(R.id.choiceCard);  break;
                case TIP:   typeOfPaymentUI.check(R.id.choiceBonus); break;
                default:    typeOfPaymentUI.clearCheck();
            }
            noteUI.setText(receivedOrder.note);
        } else{
            arrivalDateTime = Calendar.getInstance();
            priceUI.setText("");
            typeOfPaymentUI.check(R.id.choiceCash);
            noteUI.setText("");
        }
        dateButton.setText(getStringDateFromCal(arrivalDateTime));
        timeButton.setText(getStringTimeFromCal(arrivalDateTime));

//        datePicker.init(arrivalDateTime.get(Calendar.YEAR), arrivalDateTime.get(Calendar.MONTH) + 1, arrivalDateTime.get(Calendar.DAY_OF_MONTH), null);
//        dateInput.setText(getStringDateFromCal(arrivalDateTime));
//        if (timePicker != null) {
//            timePicker.setCurrentHour(arrivalDateTime.get(Calendar.HOUR_OF_DAY));
//            timePicker.setCurrentMinute(arrivalDateTime.get(Calendar.MINUTE));
//        }
//        timeInput.setText(getStringTimeFromCal(arrivalDateTime));

    }

   //выполняется после возврата из OrderActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GET_DATA_FROM_ORDER_ACTIVITY && resultCode == RESULT_OK){
            int price;
            try { price = Integer.parseInt(priceUI.getText().toString());
            } catch (NumberFormatException e) {
                Log.d(LOG_TAG, "NumberFormatException caught while parsing price");
                price = 0;
            }
            String note;
            try { note = noteUI.getText().toString();
            } catch (Exception e) {
                Log.d(LOG_TAG, "Exception caught while parsing note");
                note = "";
            }
            Order newOrder = new Order(arrivalDateTime.getTime(), getRadioState(), price, currentShift, note);
            refreshWidgets(null);
            ordersStorage.add(newOrder);//единственная точка добавления заказа
            if (mAdapter != null) mAdapter.notifyDataSetChanged();
            Toast.makeText(context, "заказ добавлен", Toast.LENGTH_SHORT).show();
            currentShift.calculateShiftTotals(0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.action_show_orders_list:
                //TODO: клик по этой кнопке должен показывать фрагмент, а не старую активити
                startActivity(new Intent(context, OrdersListActivity.class));
                return true;


            case R.id.main_menu:
                startActivity(new Intent(context, FirstScreenActivity.class));
                finish();
                return true;

            case R.id.action_settings:
                startActivity(new Intent(context, SettingsActivity.class));
                return true;

            case R.id.action_new_shift:
                if(!currentShift.isClosed())
                    Toast.makeText(context, "смена не закрыта. закрыть ее можно в меню \"итоги смены\"", Toast.LENGTH_SHORT).show();
                else {
                    currentShift = new Shift();
                    ordersStorage.clear(false);
                    Toast.makeText(context, "открыта новая смена", Toast.LENGTH_SHORT).show();
                }
                return true;

            case R.id.action_chat:
                startActivity(new Intent(context, ChatLoginActivity.class));
                return true;

            case R.id.action_shift_totals:
                startActivity(new Intent(context, ShiftTotalsActivity.class));
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(context, FirstScreenActivity.class));
        finish();
    }
}
