package tt.richTaxist.Units;

import android.database.Cursor;
import android.provider.BaseColumns;
import static tt.richTaxist.DB.Tables.BillingsTable.*;
/**
 * Created by Tau on 25.09.2015.
 */
public class Billing {
    public long billingID;
    public String billingName;
    public float commission;

    public Billing(String billingName, float commission) {
        this.billingID = -1;//необходимо для использования автоинкремента id новой записи в sql
        this.billingName = billingName;
        this.commission = commission;
    }

    public Billing(Cursor cursor) {
        billingID = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
        billingName = cursor.getString(cursor.getColumnIndex(BILLING_NAME));
        commission = cursor.getInt(cursor.getColumnIndex(COMMISSION));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Billing that = (Billing) o;
        return billingID == that.billingID;
    }

    @Override
    public int hashCode() {
        return (int) (31 * billingID);
    }

    @Override
    public String toString() {
        return billingName;
//        return String.format("ID: %d, имя: %s, комиссия: %f", billingID, billingName, commission);
    }
}
