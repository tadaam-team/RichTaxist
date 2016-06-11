package tt.richCabman.database.tables;

import android.content.ContentValues;
import android.provider.BaseColumns;
import tt.richCabman.database.MySQLHelper;
import tt.richCabman.model.Billing;

public class BillingsTable {
    public static final String TABLE_NAME = "Billings";

    public static final String BILLING_NAME = "billingName";
    public static final String COMMISSION = "commission";

    public static final String FIELDS = MySQLHelper.PRIMARY_KEY
            + BILLING_NAME + " TEXT, "
            + COMMISSION + " REAL";

    public BillingsTable() { } //table cannot be instantiated

    public static ContentValues getContentValues(Billing billing) {
        ContentValues cv = new ContentValues();
        if (billing.billingID != -1) {
            cv.put(BaseColumns._ID, billing.billingID);
        }
        cv.put(BILLING_NAME, billing.billingName);
        cv.put(COMMISSION, billing.commission);
        return cv;
    }
}
