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
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;
import java.util.Calendar;
import tt.richTaxist.Bricks.CustomSpinner;
import tt.richTaxist.Bricks.CustomSpinner.TypeOfSpinner;
import tt.richTaxist.Bricks.DateTimeButtons;
import tt.richTaxist.Constants;
import tt.richTaxist.Units.Order;
import tt.richTaxist.TypeOfPayment;
import tt.richTaxist.MainActivity;
import tt.richTaxist.OrderActivity;
import tt.richTaxist.R;
import tt.richTaxist.Settings4ParksAndBillingsActivity;
import tt.richTaxist.Util;

public class OrderFragment extends Fragment implements DateTimeButtons.DateTimeButtonsInterface {
    public static final String FRAGMENT_TAG = "OrderFragment";
    private OrderFragmentInterface mListener;

    private Order order;
    private Calendar arrivalDateTime;
    private RadioGroup typeOfPaymentUI;
    private EditText etPrice, etNote;
    private CustomSpinner spnTaxopark, spnBilling;
    private final static int GET_DATA_FROM_ORDER_ACTIVITY = 1;

    public OrderFragment() { }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try { mListener = (OrderFragmentInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement " + mListener.getClass().getName());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_order, container, false);

        typeOfPaymentUI = (RadioGroup)    rootView.findViewById(R.id.payTypeRadioGroup);
        etPrice         = (EditText)      rootView.findViewById(R.id.etPrice);
        etNote          = (EditText)      rootView.findViewById(R.id.etNote);
        spnTaxopark     = (CustomSpinner) rootView.findViewById(R.id.spnTaxopark);
        spnBilling      = (CustomSpinner) rootView.findViewById(R.id.spnBilling);

        rootView.findViewById(R.id.btnAddNewOrder).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Util.showTaxometer){
                    startActivityForResult(new Intent(getContext(), OrderActivity.class), GET_DATA_FROM_ORDER_ACTIVITY);
                } else {
                    wrapDataIntoOrder(0, 0);
                }
            }
        });
        rootView.findViewById(R.id.btnClearForm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshWidgets(null);
                Toast.makeText(getContext(), R.string.formClearedMSG, Toast.LENGTH_SHORT).show();
            }
        });

        rootView.findViewById(R.id.btnTaxopark).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), Settings4ParksAndBillingsActivity.class));
            }
        });
        rootView.findViewById(R.id.btnBilling).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), Settings4ParksAndBillingsActivity.class));
            }
        });
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        createTaxoparkSpinner();
        createBillingSpinner();
        refreshWidgets(order);
    }

    public void createTaxoparkSpinner(){
        spnTaxopark.createSpinner(TypeOfSpinner.TAXOPARK, false);
        spnTaxopark.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View itemSelected, int selectedItemPosition, long selectedId) {
                spnTaxopark.saveSpinner(TypeOfSpinner.TAXOPARK);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {/*NOP*/}
        });
    }
    public void createBillingSpinner(){
        spnBilling.createSpinner(TypeOfSpinner.BILLING, false);
        spnBilling.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View itemSelected, int selectedItemPosition, long selectedId) {
                spnBilling.saveSpinner(TypeOfSpinner.BILLING);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {/*NOP*/}
        });
    }

    private TypeOfPayment getRadioState(){
        switch (typeOfPaymentUI.getCheckedRadioButtonId()){
            case R.id.choiceCash:   return TypeOfPayment.CASH;
            case R.id.choiceCard:   return TypeOfPayment.CARD;
            case R.id.choiceBonus:  return TypeOfPayment.TIP;
            default:                throw new IllegalArgumentException("ошибка обработки типа оплаты");
        }
    }

    public void setOrder(Order order){
        this.order = order;
    }

    public Order getOrder(){
        return order;
    }

    public void refreshWidgets(Order receivedOrder){
        //is used to clear widgets if receivedOrder == null
        if (receivedOrder != null) {
            order = receivedOrder;
            arrivalDateTime = Calendar.getInstance();
            arrivalDateTime.setTime(receivedOrder.arrivalDateTime);
            etPrice.setText(String.valueOf(receivedOrder.price));
            switch (receivedOrder.typeOfPayment) {
                case CASH:  typeOfPaymentUI.check(R.id.choiceCash); break;
                case CARD:  typeOfPaymentUI.check(R.id.choiceCard);  break;
                case TIP:   typeOfPaymentUI.check(R.id.choiceBonus); break;
                default:    typeOfPaymentUI.check(R.id.choiceCash);
            }
            etNote.setText(receivedOrder.note);
            spnTaxopark.setPositionOfSpinner(TypeOfSpinner.TAXOPARK, receivedOrder.taxoparkID);
            spnBilling.setPositionOfSpinner(TypeOfSpinner.BILLING, receivedOrder.billingID);
        } else{
            order = null;
            arrivalDateTime = Calendar.getInstance();
            etPrice.setText("");
            typeOfPaymentUI.check(R.id.choiceCash);
            etNote.setText("");
            spnTaxopark.setPositionOfSpinner(TypeOfSpinner.TAXOPARK, -2);
            spnBilling.setPositionOfSpinner(TypeOfSpinner.BILLING, 0);
        }

        //Nested fragments are only supported when added to a fragment dynamically.
        FragmentManager fragmentManager = getChildFragmentManager();
        DateTimeButtons buttonsFragment = (DateTimeButtons) fragmentManager.findFragmentByTag(DateTimeButtons.FRAGMENT_TAG);
        if (buttonsFragment == null) {
            buttonsFragment = new DateTimeButtons();
            Bundle args = new Bundle();
            args.putLong(DateTimeButtons.DATE_TIME_EXTRA, arrivalDateTime.getTimeInMillis());
            buttonsFragment.setArguments(args);
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.replace(R.id.dateTimePlaceHolder, buttonsFragment, DateTimeButtons.FRAGMENT_TAG);
            ft.commit();
        } else {
            buttonsFragment.setDateTime(arrivalDateTime);
        }
    }

    //выполняется после возврата из OrderActivity
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GET_DATA_FROM_ORDER_ACTIVITY && resultCode == Activity.RESULT_OK){
            //когда из таксометра мы будем получать результат, wrapDataIntoOrder() будет получать дополнительные параметры
            int distance = data.getIntExtra(Order.PARAM_DISTANCE, 0);
            long travelTime = data.getIntExtra(Order.PARAM_TRAVEL_TIME, 0);
            wrapDataIntoOrder(distance, travelTime);
        }
    }

    private void wrapDataIntoOrder(int distance, long travelTime) {
        int price;
        try {
            price = Integer.parseInt(etPrice.getText().toString());
        } catch (NumberFormatException e) {
            Log.d(Constants.LOG_TAG, getResources().getString(R.string.wrongPriceErrMsg));
            Toast.makeText(getContext(), String.valueOf(getResources().getString(R.string.wrongPriceErrMsg)), Toast.LENGTH_LONG).show();
            price = 0;
        }
        String note;
        try {
            note = etNote.getText().toString();
        } catch (Exception e) {
            Log.d(Constants.LOG_TAG, getResources().getString(R.string.wrongNoteErrMsg));
            Toast.makeText(getContext(), String.valueOf(getResources().getString(R.string.wrongNoteErrMsg)), Toast.LENGTH_LONG).show();
            note = "";
        }
        if (order == null) {
            order = new Order(arrivalDateTime.getTime(), price, getRadioState(), MainActivity.currentShift.shiftID, note,
                    distance, travelTime, spnTaxopark.taxoparkID, spnBilling.billingID);
        } else {
            order.update(arrivalDateTime.getTime(), price, getRadioState(), MainActivity.currentShift.shiftID, note,
                    distance, travelTime, spnTaxopark.taxoparkID, spnBilling.billingID);
        }
        //fragment does not encapsulate db work. it only gets data from user and wraps it in Order
        //it doesn't know what to do with Order. this is job for fragment host
        mListener.addOrder(order);
        refreshWidgets(null);
    }

    public void onDateOrTimeSet(Calendar cal){
        arrivalDateTime.setTimeInMillis(cal.getTimeInMillis());
    }

    public interface OrderFragmentInterface {
        void addOrder(Order order);
    }
}
