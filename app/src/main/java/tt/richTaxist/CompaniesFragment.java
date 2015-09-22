package tt.richTaxist;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class CompaniesFragment extends ListFragment {
    private static final String LOG_TAG = "CompaniesFragment";
    private AppCompatActivity mActivity;
    HashMap<String, Object> company;
    private String keyCompanyName = "CompanyName", keyIsDefault = "IsDefault", keyDefaultBilling = "DefaultBilling";
    //TODO: сохранять в SQL 2 строки ниже
    private ArrayList<HashMap<String, Object>> companiesList = new ArrayList<>();//сохранять в SQL
    private ArrayAdapter companiesAdapter;
    public CompaniesFragment() { }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (AppCompatActivity) activity;

        //TODO: при открытии загружать из SQL данные по companiesList и defaultCompanyIndex
        company = new HashMap<>();
        company.put(keyCompanyName, "Таксовичков");
        company.put(keyIsDefault, true);
        company.put(keyDefaultBilling, 0);
        companiesList.add(company);

        company = new HashMap<>();
        company.put(keyCompanyName, "АС-такси");
        company.put(keyIsDefault, false);
        company.put(keyDefaultBilling, 0);
        companiesList.add(company);

        company = new HashMap<>();
        company.put(keyCompanyName, "068");
        company.put(keyIsDefault, false);
        company.put(keyDefaultBilling, 0);
        companiesList.add(company);

        companiesAdapter = new CompaniesAdapter(mActivity);
        setListAdapter(companiesAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_companies, container, false);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
        rootView.setLayoutParams(layoutParams);

        (rootView.findViewById(R.id.addCompany)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                company = new HashMap<>();
                company.put(keyCompanyName, "");
                company.put(keyIsDefault, false);
                company.put(keyDefaultBilling, 0);
                companiesList.add(company);
                companiesAdapter.notifyDataSetChanged();
            }
        });
        return rootView;
    }

    class CompaniesAdapter extends ArrayAdapter<HashMap<String, Object>> {
        private final Context context;

        public CompaniesAdapter(Context context) {
            super(context, android.R.layout.simple_list_item_1, companiesList);
            this.context = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.list_entry_companies, parent, false);
            }
            final HashMap<String, Object> company = companiesList.get(position);

            final EditText companyName = (EditText) convertView.findViewById(R.id.companyName);
            companyName.setText(company.get(keyCompanyName).toString());
            //моментами сохранения считаются либо нажатие enter либо потеря фокуса EditText-ом
            companyName.setOnKeyListener(new View.OnKeyListener() {
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                        saveCompanyName(companyName, company);
                        return true;
                    }
                    return false;
                }
            });
//            companyName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//                @Override
//                public void onFocusChange(View view, boolean hasFocus) {
//                    if (!hasFocus) saveCompanyName(companyName, company);
//                }
//            });

            RadioButton rbDefault = (RadioButton) convertView.findViewById(R.id.rbDefault);
            rbDefault.setChecked((Boolean) company.get(keyIsDefault));
            rbDefault.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    for (HashMap<String, Object> company : companiesList) {
                        company.put(keyIsDefault, false);
                    }
                    company.put(keyIsDefault, true);
                    notifyDataSetChanged();
                }
            });

            (convertView.findViewById(R.id.delCompany)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    companiesList.remove(company);
                    notifyDataSetChanged();
                }
            });
            return convertView;
        }
    }

    private void saveCompanyName(EditText companyName, HashMap<String, Object> currentCompany){
        String newName = companyName.getText().toString();
        boolean isInTheList = false;
        for (HashMap<String, Object> company : companiesList) {
            if (newName.equals(company.get(keyCompanyName)) && !currentCompany.equals(company)) isInTheList = true;
        }
        if (isInTheList) {
            Toast.makeText(mActivity, "таксопарк " + String.valueOf(newName) + " уже есть в списке", Toast.LENGTH_SHORT).show();
            companyName.setText("");
        }
        else company.put(keyCompanyName, newName);
    }
}
