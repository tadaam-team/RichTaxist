package tt.richTaxist.Bricks;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import java.util.ArrayList;
import tt.richTaxist.DB.BillingsSQLHelper;
import tt.richTaxist.DB.TaxoparksSQLHelper;
import tt.richTaxist.R;
import tt.richTaxist.Units.Billing;
import tt.richTaxist.Units.Taxopark;
/**
 * Created by TAU on 27.04.2016.
 */
public class CustomSpinner extends Spinner {
    private static final String LOG_TAG = "CustomSpinner";
    public static int taxoparkID = -1;
    public static int billingID = -1;
    public static int monthID = -1;

    public CustomSpinner(Context context) {
        super(context);
    }
    //inflate a view from XML
    public CustomSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    //inflate a view from XML and apply a class-specific base style
    public CustomSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public static void saveSpinner(TypeOfSpinner typeOfSpinner, Spinner spinner){
        switch (typeOfSpinner) {
            case TAXOPARK:
                try { taxoparkID = ((Taxopark) spinner.getSelectedItem()).taxoparkID;
                } catch (NullPointerException e) {
                    Log.d(LOG_TAG, "taxopark not defined");
                    taxoparkID = -1;
                }
                break;

            case BILLING:
                try { billingID = ((Billing) spinner.getSelectedItem()).billingID;
                } catch (NullPointerException e) {
                    Log.d(LOG_TAG, "billing not defined");
                    billingID = -1;
                }
                break;

            case MONTH:
                try { monthID = (int) spinner.getSelectedItemId();
                } catch (NullPointerException e) {
                    Log.d(LOG_TAG, "month not defined");
                    monthID = -1;
                }
                break;
        }
    }

    public static void setPositionOfSpinner(TypeOfSpinner typeOfSpinner, ArrayAdapter adapter, Spinner spinner, int id){
        switch (typeOfSpinner){
            case TAXOPARK:
                //если получена команда обнулить состояние спиннера, возвращаем не просто первый по списку, а умолчание
                if (id == -1) {
                    for (Taxopark taxoparkIter : TaxoparksSQLHelper.dbOpenHelper.getAllTaxoparks()) {
                        if (taxoparkIter.isDefault) {
                            taxoparkID = id = taxoparkIter.taxoparkID;
                        }
                    }
                }

                Taxopark taxopark = TaxoparksSQLHelper.dbOpenHelper.getTaxoparkByID(id);
                int indexInSpinner = adapter.getPosition(taxopark);
                spinner.setSelection(indexInSpinner);
                break;

            case BILLING:
                Billing billing = BillingsSQLHelper.dbOpenHelper.getBillingByID(id);
                indexInSpinner = adapter.getPosition(billing);
                spinner.setSelection(indexInSpinner);
                break;

            case MONTH:
                spinner.setSelection(monthID);
                break;
        }
        adapter.notifyDataSetChanged();
    }

    public void createTaxoparkSpinner(boolean addBlankListEntry){
        ArrayList<Taxopark> list = new ArrayList<>();
        if (addBlankListEntry){
            list.add(0, new Taxopark(0, "- - -", false, 0));
        }
        list.addAll(TaxoparksSQLHelper.dbOpenHelper.getAllTaxoparks());
        ArrayAdapter spnTaxoparkAdapter = new ArrayAdapter<>(getContext(), R.layout.list_entry_spinner, list);
        setAdapter(spnTaxoparkAdapter);
        setPositionOfSpinner(TypeOfSpinner.TAXOPARK, spnTaxoparkAdapter, this, 0);
    }

    public enum TypeOfSpinner {
        TAXOPARK,
        BILLING,
        MONTH
    }
}
