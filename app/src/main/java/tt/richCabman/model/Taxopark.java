package tt.richCabman.model;

import android.database.Cursor;
import android.provider.BaseColumns;
import static tt.richCabman.database.tables.TaxoparksTable.*;
/**
 * Created by Tau on 23.09.2015.
 */
public class Taxopark {
    public long taxoparkID;
    public String taxoparkName;
    public boolean isDefault;
    public int defaultBilling;

    public Taxopark(String taxoparkName, boolean isDefault, int defaultBilling) {
        taxoparkID = -1;//необходимо для использования автоинкремента id новой записи в sql
        this.taxoparkName   = taxoparkName;
        this.isDefault      = isDefault;
        this.defaultBilling = defaultBilling;
    }

    public Taxopark(Cursor cursor) {
        taxoparkID     = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
        taxoparkName   = cursor.getString(cursor.getColumnIndex(TAXOPARK_NAME));
        isDefault      = cursor.getInt(cursor.getColumnIndex(IS_DEFAULT)) == 1;
        defaultBilling = cursor.getInt(cursor.getColumnIndex(DEFAULT_BILLING));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Taxopark that = (Taxopark) o;
        return taxoparkID == that.taxoparkID;
    }

    @Override
    public int hashCode() {
        return (int) (31 * taxoparkID);
    }

    @Override
    public String toString() {
        return taxoparkName;
//        return String.format("ID: %d, имя: %s, основной?: %s, способ расчетов: %d", taxoparkID, taxoparkName, isDefault,  defaultBilling);
    }
}
