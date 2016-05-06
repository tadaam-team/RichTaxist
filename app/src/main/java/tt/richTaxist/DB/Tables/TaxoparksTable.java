package tt.richTaxist.DB.Tables;

import android.content.ContentValues;
import android.provider.BaseColumns;
import tt.richTaxist.DB.MySQLHelper;
import tt.richTaxist.Units.Taxopark;

public class TaxoparksTable {
    public static final String TABLE_NAME = "Taxoparks";

    public static final String TAXOPARK_NAME = "taxoparkName";
    public static final String IS_DEFAULT = "isDefault";
    public static final String DEFAULT_BILLING = "defaultBilling";

    public static final String FIELDS = MySQLHelper.PRIMARY_KEY
            + TAXOPARK_NAME     + " TEXT, "
            + IS_DEFAULT        + " INTEGER, "
            + DEFAULT_BILLING   + " INTEGER";

    public TaxoparksTable() { } //table cannot be instantiated

    public static ContentValues getContentValues(Taxopark taxopark) {
        ContentValues cv = new ContentValues();
        if (taxopark.taxoparkID != -1) {
            cv.put(BaseColumns._ID, taxopark.taxoparkID);
        }
        cv.put(TAXOPARK_NAME,   taxopark.taxoparkName);
        cv.put(IS_DEFAULT,      taxopark.isDefault ? 1 : 0);
        cv.put(DEFAULT_BILLING, taxopark.defaultBilling);
        return cv;
    }
}
