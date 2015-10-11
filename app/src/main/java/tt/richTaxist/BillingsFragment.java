package tt.richTaxist;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import java.util.ArrayList;
import tt.richTaxist.DB.BillingsSQLHelper;

public class BillingsFragment extends ListFragment {
    private static final String LOG_TAG = "BillingsFragment";
    ArrayList<Billing> billings = new ArrayList<>();
    private AppCompatActivity mActivity;
    private ArrayAdapter billingsAdapter;
    public BillingsFragment() { }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (AppCompatActivity) activity;
        billings.addAll(BillingsSQLHelper.dbOpenHelper.getAllBillings());
        billingsAdapter = new BillingsAdapter(mActivity);
        setListAdapter(billingsAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_billings, container, false);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
        rootView.setLayoutParams(layoutParams);

        (rootView.findViewById(R.id.addBilling)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Billing billing = new Billing("", 0.0f);
                billings.add(billing);
                BillingsSQLHelper.dbOpenHelper.create(billing);
                billingsAdapter.notifyDataSetChanged();
                if (OrderFragment.spnBillingAdapter != null) OrderFragment.spnBillingAdapter.notifyDataSetChanged();
            }
        });
        return rootView;
    }

    class BillingsAdapter extends ArrayAdapter<Billing> {
        private final Context context;

        public BillingsAdapter(Context context) {
            super(context, android.R.layout.simple_list_item_1, billings);
            this.context = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.list_entry_billings_editable, parent, false);
            }
            final Billing billing = getItem(position);

            final EditText etBillingName = (EditText) convertView.findViewById(R.id.etBillingName);
            etBillingName.setText(billing.billingName);
            final EditText etCommission = (EditText) convertView.findViewById(R.id.etCommission);
            etCommission.setText(String.valueOf(billing.commission));

            //моментами сохранения считаются либо нажатие enter либо потеря фокуса EditText-ом
            etBillingName.setOnKeyListener(new View.OnKeyListener() {
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                        saveBilling(billing, etBillingName, etCommission);
                        return true;
                    }
                    return false;
                }
            });
            etCommission.setOnKeyListener(new View.OnKeyListener() {
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                        saveBilling(billing, etBillingName, etCommission);
                        return true;
                    }
                    return false;
                }
            });
//            etBillingName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//                @Override
//                public void onFocusChange(View view, boolean hasFocus) {
//                    if (!hasFocus) saveBilling(billing, etBillingName, etCommission);
//                }
//            });
//            etCommission.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//                @Override
//                public void onFocusChange(View view, boolean hasFocus) {
//                    if (!hasFocus) saveBilling(billing, etBillingName, etCommission);
//                }
//            });

            (convertView.findViewById(R.id.delBilling)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    billings.remove(billing);
                    BillingsSQLHelper.dbOpenHelper.remove(billing);
                    notifyDataSetChanged();
                    if (OrderFragment.spnBillingAdapter != null) OrderFragment.spnBillingAdapter.notifyDataSetChanged();
                }
            });
            return convertView;
        }

        private void saveBilling(Billing currentBilling, EditText billingName, EditText commission){
            String newName = billingName.getText().toString();
            boolean isInTheList = false;
            for (Billing billingIter : BillingsSQLHelper.dbOpenHelper.getAllBillings()) {
                if (newName.equals(billingIter.billingName) && currentBilling.billingID != billingIter.billingID) isInTheList = true;
            }
            if (isInTheList) {
                Toast.makeText(mActivity, getResources().getString(R.string.billing) + " " + String.valueOf(newName) + " " +
                        getResources().getString(R.string.isInTheList), Toast.LENGTH_SHORT).show();
                billingName.setText("");
                commission.setText("");
            }
            else {
                currentBilling.billingName = newName;
                currentBilling.commission = Float.valueOf(commission.getText().toString());
                BillingsSQLHelper.dbOpenHelper.update(currentBilling);
                if (OrderFragment.spnBillingAdapter != null) OrderFragment.spnBillingAdapter.notifyDataSetChanged();
            }
        }
    }
}
