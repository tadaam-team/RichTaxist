package tt.richTaxist.Fragments;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import java.util.Calendar;
import tt.richTaxist.Bricks.DateTimeButtons;
import tt.richTaxist.DB.BillingsSQLHelper;
import tt.richTaxist.Units.Order;
import tt.richTaxist.DB.TaxoparksSQLHelper;
import tt.richTaxist.Enums.TypeOfPayment;
import tt.richTaxist.Enums.TypeOfSpinner;
import tt.richTaxist.MainActivity;
import tt.richTaxist.OrderActivity;
import tt.richTaxist.R;
import tt.richTaxist.Settings4ParksAndBillingsActivity;
import tt.richTaxist.Util;

public class OrderFragment extends Fragment implements
        DateTimeButtons.OnDateTimeButtonsFragmentInteractionListener {
    public static final String FRAGMENT_TAG = "OrderFragment";
    private static final String LOG_TAG = "OrderFragment";
    private static Context context;
    private OnOrderFragmentInteractionListener mListener;

    private Order order;
    public static Calendar arrivalDateTime;
    private RadioGroup typeOfPaymentUI;
    private EditText etPrice, etNote;
    private Spinner spnTaxopark, spnBilling;
    private ArrayAdapter spnTaxoparkAdapter, spnBillingAdapter;
    private final static int GET_DATA_FROM_ORDER_ACTIVITY = 1;

    public OrderFragment() { }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try { mListener = (OnOrderFragmentInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement " + mListener.getClass().getName());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = getContext();
        View rootView = inflater.inflate(R.layout.fragment_order, container, false);
        LayoutParams layoutParams = new LayoutParams(0, LayoutParams.MATCH_PARENT, 2.0f);
        rootView.setLayoutParams(layoutParams);

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

        rootView.findViewById(R.id.btnAddNewOrder).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Util.hideTaxometer){
                    startActivityForResult(new Intent(context, OrderActivity.class), GET_DATA_FROM_ORDER_ACTIVITY);
                } else {
                    createNewOrder(0, 0);
                }
            }
        });
        rootView.findViewById(R.id.btnClearForm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshWidgets(null);
                Util.setPositionOfSpinner(TypeOfSpinner.TAXOPARK, spnTaxoparkAdapter, spnTaxopark, -1);
                Util.setPositionOfSpinner(TypeOfSpinner.BILLING, spnBillingAdapter, spnBilling, 0);
                Toast.makeText(context, R.string.formClearedMSG, Toast.LENGTH_SHORT).show();
            }
        });

        rootView.findViewById(R.id.btnTaxopark).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, Settings4ParksAndBillingsActivity.class));
            }
        });
        rootView.findViewById(R.id.btnBilling).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, Settings4ParksAndBillingsActivity.class));
            }
        });
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        createTaxoparkSpinner();
        createBillingSpinner();
        refreshInputStyle();
        if (order != null) {
            refreshWidgets(order);
        }
    }

    public void createTaxoparkSpinner(){
        spnTaxoparkAdapter = new ArrayAdapter<>(context, R.layout.list_entry_spinner,
                TaxoparksSQLHelper.dbOpenHelper.getAllTaxoparks());
        spnTaxopark.setAdapter(spnTaxoparkAdapter);
        Util.setPositionOfSpinner(TypeOfSpinner.TAXOPARK, spnTaxoparkAdapter, spnTaxopark, -1);
    }
    public void createBillingSpinner(){
        spnBillingAdapter = new ArrayAdapter<>(context, R.layout.list_entry_spinner,
                BillingsSQLHelper.dbOpenHelper.getAllBillings());
        spnBilling.setAdapter(spnBillingAdapter);
        Util.setPositionOfSpinner(TypeOfSpinner.BILLING, spnBillingAdapter, spnBilling, Util.billingID);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("arrivalDateTime", arrivalDateTime.getTimeInMillis());
        Util.saveSpinner(TypeOfSpinner.TAXOPARK, spnTaxopark);
        Util.saveSpinner(TypeOfSpinner.BILLING, spnBilling);
    }

    private TypeOfPayment getRadioState(){
        switch (typeOfPaymentUI.getCheckedRadioButtonId()){
            case R.id.choiceCash:   return TypeOfPayment.CASH;
            case R.id.choiceCard:   return TypeOfPayment.CARD;
            case R.id.choiceBonus:  return TypeOfPayment.TIP;
            default:                throw new IllegalArgumentException("ошибка обработки типа оплаты");
        }
    }

    //этот метод получает заказ из листа, когда юзер делает свайп влево
    public void setOrder(Order order){
        this.order = order;
    }

    //этот метод обнуляет поля ввода, если переданный заказ == null
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
            Util.setPositionOfSpinner(TypeOfSpinner.TAXOPARK, spnTaxoparkAdapter, spnTaxopark, receivedOrder.taxoparkID);
            Util.setPositionOfSpinner(TypeOfSpinner.BILLING, spnBillingAdapter, spnBilling, receivedOrder.billingID);
        } else{
            arrivalDateTime = Calendar.getInstance();
            etPrice.setText("");
            typeOfPaymentUI.check(R.id.choiceCash);
            etNote.setText("");
        }

        FragmentManager fragmentManager = getChildFragmentManager();
        DateTimeButtons buttonsFragment = (DateTimeButtons) fragmentManager.findFragmentByTag("buttonsFragment");
        if (buttonsFragment != null && buttonsFragment.isAdded())
            buttonsFragment.setDateTime(arrivalDateTime);
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

        Util.saveSpinner(TypeOfSpinner.TAXOPARK, spnTaxopark);
        Util.saveSpinner(TypeOfSpinner.BILLING, spnBilling);

        Order newOrder = new Order(arrivalDateTime.getTime(), price, getRadioState(), MainActivity.currentShift, note,
                distance, travelTime, Util.taxoparkID, Util.billingID);
        mListener.addOrder(newOrder);
        refreshWidgets(null);
    }

    public void onDateOrTimeSet(Calendar cal){
        arrivalDateTime.setTimeInMillis(cal.getTimeInMillis());
    }

    public interface OnOrderFragmentInteractionListener {
        void addOrder(Order order);
    }

    private void refreshInputStyle() {
        FragmentManager fragmentManager = getChildFragmentManager();
        DateTimeButtons buttonsFragment = (DateTimeButtons) fragmentManager.findFragmentByTag("buttonsFragment");
        if (buttonsFragment == null) {
            buttonsFragment = new DateTimeButtons();
            Bundle bundle = new Bundle();
            bundle.putLong("arrivalDateTime", arrivalDateTime.getTimeInMillis());
            buttonsFragment.setArguments(bundle);
        }
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.dateTimePlaceHolder, buttonsFragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();
    }
}
