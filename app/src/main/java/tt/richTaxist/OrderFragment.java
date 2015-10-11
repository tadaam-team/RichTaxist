package tt.richTaxist;

import android.support.v4.app.Fragment;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;
import java.util.Calendar;
import tt.richTaxist.Bricks.F_CustomDatePicker;
import tt.richTaxist.DB.BillingsSQLHelper;
import tt.richTaxist.DB.TaxoparksSQLHelper;
import tt.richTaxist.Enums.TypeOfPayment;
import tt.richTaxist.Enums.TypeOfSpinner;

public class OrderFragment extends Fragment implements DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener, F_CustomDatePicker.OnFragmentInteractionListener{
    private static final String LOG_TAG = "OrderFragment";
    private OnOrderFragmentInteractionListener mListener;
    View rootView;
    DatePickerDialog.OnDateSetListener dateSetListener;
    TimePickerDialog.OnTimeSetListener timeSetListener;
    public Calendar arrivalDateTime;
    private static Button dateButton, timeButton;
//    private static CustomTimePicker timePicker;
//    private static LinearLayout timePickerPlaceHolder;
    private RadioGroup typeOfPaymentUI;
    private EditText etPrice, etNote;
    private static Spinner spnTaxopark, spnBilling;
    public static ArrayAdapter spnTaxoparkAdapter, spnBillingAdapter;
    private final static int GET_DATA_FROM_ORDER_ACTIVITY = 1;
    private static AppCompatActivity mActivity;

    public OrderFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (AppCompatActivity) activity;
        dateSetListener = OrderFragment.this;
        timeSetListener = OrderFragment.this;
        try { mListener = (OnOrderFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnOrderFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        //onDestroy() will not be called (but onDetach() still will be, because the fragment is being detached from its current activity).
        //onCreate(Bundle) will not be called since the fragment is not being re-created.
        //onAttach(Activity) and onActivityCreated(Bundle) will still be called.
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_order, container, false);
        LayoutParams layoutParams = new LayoutParams(0, LayoutParams.MATCH_PARENT, 2.0f);
        rootView.setLayoutParams(layoutParams);

        dateButton      = (Button)      rootView.findViewById(R.id.btnDate);
        timeButton      = (Button)      rootView.findViewById(R.id.btnTime);
        typeOfPaymentUI = (RadioGroup)  rootView.findViewById(R.id.payTypeRadioGroup);
        etPrice         = (EditText)    rootView.findViewById(R.id.etPrice);
        etNote          = (EditText)    rootView.findViewById(R.id.etNote);
        spnTaxopark     = (Spinner)     rootView.findViewById(R.id.spnTaxopark);
        spnBilling      = (Spinner)     rootView.findViewById(R.id.spnBilling);

        arrivalDateTime = Calendar.getInstance();
        if (savedInstanceState != null) {
            long arrivalDateTimeLong = savedInstanceState.getLong("arrivalDateTime", arrivalDateTime.getTimeInMillis());
            arrivalDateTime.setTimeInMillis(arrivalDateTimeLong);
        }

        dateButton.setText(getStringDateFromCal(arrivalDateTime));
        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePD = DatePickerDialog.newInstance(dateSetListener,
                        arrivalDateTime.get(Calendar.YEAR), arrivalDateTime.get(Calendar.MONTH), arrivalDateTime.get(Calendar.DAY_OF_MONTH), false);
                datePD.setVibrate(false);
                datePD.setYearRange(2015, 2020);
                datePD.setCloseOnSingleTapDay(true);
                datePD.show(getChildFragmentManager(), "datepicker");// getFragmentManager() работает точно также
            }
        });
//        if (Storage.typeOfDateInput == TypeOfInput.SPINNER) {
//            FragmentManager fragmentManager = getFragmentManager();
//            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//            F_CustomDatePicker myFragment = new F_CustomDatePicker();
//            fragmentTransaction.add(R.id.datePickerPlaceHolder, myFragment);
//            fragmentTransaction.create();
//        }

        timeButton.setText(getStringTimeFromCal(arrivalDateTime));
        timeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePD = TimePickerDialog.newInstance(timeSetListener,
                        arrivalDateTime.get(Calendar.HOUR_OF_DAY), arrivalDateTime.get(Calendar.MINUTE), true, false);
                timePD.setVibrate(false);
                timePD.setCloseOnSingleTapMinute(Storage.twoTapTimePick);
                timePD.show(getChildFragmentManager(), "timepicker");// getFragmentManager() работает точно также
            }
        });
//        timePickerPlaceHolder = (LinearLayout) findViewById(R.id.timePickerPlaceHolder);
//        refreshInputStyle();//плохо, но в процессе обновления создается timePicker при необходимости

        rootView.findViewById(R.id.btnAddNewOrder).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Storage.hideTaxometer)
                    startActivityForResult(new Intent(mActivity, OrderActivity.class), GET_DATA_FROM_ORDER_ACTIVITY);
                else createNewOrder(0, 0);
            }
        });
        rootView.findViewById(R.id.btnClearForm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshWidgets(null);
                Storage.setPositionOfSpinner(TypeOfSpinner.TAXOPARK, spnTaxoparkAdapter, spnTaxopark, 0);
                Storage.setPositionOfSpinner(TypeOfSpinner.BILLING, spnBillingAdapter, spnBilling, 0);
                Toast.makeText(mActivity, R.string.formClearedMSG, Toast.LENGTH_SHORT).show();
            }
        });

        rootView.findViewById(R.id.btnTaxopark).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mActivity, Settings4ParksAndBillingsActivity.class));
            }
        });
        rootView.findViewById(R.id.btnBilling).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mActivity, Settings4ParksAndBillingsActivity.class));
            }
        });
        createTaxoparkSpinner();

        spnBillingAdapter = new ArrayAdapter<>(mActivity, android.R.layout.simple_spinner_item,
                BillingsSQLHelper.dbOpenHelper.getAllBillings());
        spnBillingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnBilling.setAdapter(spnBillingAdapter);
        Storage.setPositionOfSpinner(TypeOfSpinner.BILLING, spnBillingAdapter, spnBilling, Storage.billingID);

        return rootView;
    }

    public static void createTaxoparkSpinner(){
        spnTaxoparkAdapter = new ArrayAdapter<>(mActivity, android.R.layout.simple_spinner_item,
                TaxoparksSQLHelper.dbOpenHelper.getAllTaxoparks());
        spnTaxoparkAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnTaxopark.setAdapter(spnTaxoparkAdapter);
        Storage.setPositionOfSpinner(TypeOfSpinner.TAXOPARK, spnTaxoparkAdapter, spnTaxopark, 0);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("arrivalDateTime", arrivalDateTime.getTimeInMillis());
        Storage.saveSpinner(TypeOfSpinner.TAXOPARK, spnTaxopark);
        Storage.saveSpinner(TypeOfSpinner.BILLING, spnBilling);
    }

    private TypeOfPayment getRadioState(){
        switch (typeOfPaymentUI.getCheckedRadioButtonId()){
            case R.id.choiceCash:  return TypeOfPayment.CASH;
            case R.id.choiceCard:   return TypeOfPayment.CARD;
            case R.id.choiceBonus:  return TypeOfPayment.TIP;
            default:                throw new IllegalArgumentException();//"ошибка обработки типа оплаты";
        }
    }

    //этот метод получает заказ из листа, когда юзер делает свайп влево и обнуляет поля ввода, если переданный заказ == null
    public void refreshWidgets(Order receivedOrder){
        if (receivedOrder != null) {
            arrivalDateTime.setTime(receivedOrder.arrivalDateTime);
            etPrice.setText(String.valueOf(receivedOrder.price));
            switch (receivedOrder.typeOfPayment) {
                case CASH:  typeOfPaymentUI.check(R.id.choiceCash); break;
                case CARD:  typeOfPaymentUI.check(R.id.choiceCard);  break;
                case TIP:   typeOfPaymentUI.check(R.id.choiceBonus); break;
                default:    typeOfPaymentUI.clearCheck();
            }
            etNote.setText(receivedOrder.note);
            Storage.setPositionOfSpinner(TypeOfSpinner.TAXOPARK, spnTaxoparkAdapter, spnTaxopark, receivedOrder.taxoparkID);
            Storage.setPositionOfSpinner(TypeOfSpinner.BILLING, spnBillingAdapter, spnBilling, receivedOrder.billingID);
        } else{
            arrivalDateTime = Calendar.getInstance();
            etPrice.setText("");
            typeOfPaymentUI.check(R.id.choiceCash);
            etNote.setText("");
        }
        dateButton.setText(getStringDateFromCal(arrivalDateTime));
        timeButton.setText(getStringTimeFromCal(arrivalDateTime));

//        datePicker.init(arrivalDateTime.get(Calendar.YEAR), arrivalDateTime.get(Calendar.MONTH) + 1, arrivalDateTime.get(Calendar.DAY_OF_MONTH), null);
//        if (timePicker != null) {
//            timePicker.setCurrentHour(arrivalDateTime.get(Calendar.HOUR_OF_DAY));
//            timePicker.setCurrentMinute(arrivalDateTime.get(Calendar.MINUTE));
//        }
    }

    //выполняется после возврата из OrderActivity
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GET_DATA_FROM_ORDER_ACTIVITY && resultCode == Activity.RESULT_OK){
            //когда из таксометра мы будем получать результат, createNewOrder() будет получать дополнительные параметры
            int distance = data.getIntExtra(Order.PARAM_DISTANCE, 0);
            long travelTime = data.getIntExtra(Order.PARAM_TRAVEL_TIME, 0);;
            createNewOrder(distance, travelTime);
        }
    }

    private void createNewOrder(int distance, long travelTime){
        int price;
        try { price = Integer.parseInt(etPrice.getText().toString());
        } catch (NumberFormatException e) {
            Log.d(LOG_TAG, "NumberFormatException caught while parsing price");
            price = 0;
        }
        String note;
        try { note = etNote.getText().toString();
        } catch (Exception e) {
            Log.d(LOG_TAG, "Exception caught while parsing note");
            note = "";
        }

        Storage.saveSpinner(TypeOfSpinner.TAXOPARK, spnTaxopark);
        Storage.saveSpinner(TypeOfSpinner.BILLING, spnBilling);

        Order newOrder = new Order(arrivalDateTime.getTime(), price, getRadioState(), MainActivity.currentShift, note,
                distance, travelTime, Storage.taxoparkID, Storage.billingID);
        ((OnOrderFragmentInteractionListener) mActivity).addOrder(newOrder);
        refreshWidgets(null);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
    }

    //вызывается после завершения ввода в диалоге даты
    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
        arrivalDateTime.set(Calendar.YEAR, year);
        arrivalDateTime.set(Calendar.MONTH, month);
        arrivalDateTime.set(Calendar.DAY_OF_MONTH, day);
        dateButton.setText(getStringDateFromCal(arrivalDateTime));
    }

    //вызывается после завершения ввода в диалоге времени
    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
        arrivalDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
        arrivalDateTime.set(Calendar.MINUTE, minute);
        timeButton.setText(getStringTimeFromCal(arrivalDateTime));
    }

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

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnOrderFragmentInteractionListener {
        //TODO: собрать все прямые вызовы полей и методов MainActivity в общение через этот интерфейс
        public void addOrder(Order order);
    }

    //TODO: перейти от setVisibility к диспетчеру фрагментов
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
//            }
//        }

//        if (timeButton != null && timePickerPlaceHolder != null) {
//            switch (Storage.typeOfTimeInput) {
//                case BUTTON:
//                    timeButton.setVisibility(View.VISIBLE);
//                    timePickerPlaceHolder.setVisibility(View.GONE);
//                    break;
//                case SPINNER:
//                    timeButton.setVisibility(View.GONE);
//                    createAndInsertTimePicker();
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
//                timeButton.setText(getStringTimeFromCal(arrivalDateTime));
//            }
//        };
//        timePicker.setOnTimeChangedListener(timeChangedListener);
////        int sizeDP = (int) (120 * getResources().getDisplayMetrics().density + 0.5f);
////        timePicker.setMinimumHeight(sizeDP);//не работает. сейчас стоит костыль предустановленной высоты лейаута-плейсхолдера
//        timePickerPlaceHolder.removeAllViews();
//        timePickerPlaceHolder.addView(timePicker);
//    }
}
