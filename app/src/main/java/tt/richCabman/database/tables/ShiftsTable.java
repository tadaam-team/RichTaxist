package tt.richCabman.database.tables;

import android.content.ContentValues;
import android.provider.BaseColumns;
import tt.richCabman.database.MySQLHelper;
import tt.richCabman.model.Shift;
import tt.richCabman.util.Util;

public class ShiftsTable {
    public static final String TABLE_NAME              = "Shifts";

    public static final String BEGIN_SHIFT             = "beginShift";
    public static final String END_SHIFT               = "endShift";
    public static final String REVENUE_OFFICIAL        = "revenueOfficial";
    public static final String REVENUE_CASH            = "revenueCash";
    public static final String REVENUE_CARD            = "revenueCard";
    public static final String PETROL                  = "petrol";
    public static final String PETROL_FILLED_BY_HANDS  = "petrolFilledByHands";
    public static final String TO_THE_CASHIER          = "toTheCashier";
    public static final String SALARY_OFFICIAL         = "salaryOfficial";
    public static final String REVENUE_BONUS           = "revenueBonus";
    public static final String CAR_RENT                = "carRent";
    public static final String SALARY_UNOFFICIAL       = "salaryUnofficial";
    public static final String WORK_HOURS_SPENT        = "workHoursSpent";
    public static final String SALARY_PER_HOUR         = "salaryPerHour";
    public static final String DISTANCE                = "distance";
    public static final String TRAVEL_TIME             = "travelTime";

    public static final String FIELDS = MySQLHelper.PRIMARY_KEY
            + BEGIN_SHIFT               + " TEXT, "
            + END_SHIFT                 + " TEXT, "
            + REVENUE_OFFICIAL          + " INTEGER,"
            + REVENUE_CASH              + " INTEGER,"
            + REVENUE_CARD              + " INTEGER,"
            + PETROL                    + " INTEGER,"
            + PETROL_FILLED_BY_HANDS    + " NUMERIC,"
            + TO_THE_CASHIER            + " INTEGER,"
            + SALARY_OFFICIAL           + " INTEGER,"
            + REVENUE_BONUS             + " INTEGER,"
            + CAR_RENT                  + " INTEGER,"
            + SALARY_UNOFFICIAL         + " INTEGER,"
            + WORK_HOURS_SPENT          + " REAL,"
            + SALARY_PER_HOUR           + " INTEGER,"
            + DISTANCE                  + " INTEGER,"
            + TRAVEL_TIME               + " INTEGER";

    public ShiftsTable() { } //table cannot be instantiated

    public static ContentValues getContentValues(Shift shift) {
        ContentValues cv = new ContentValues();
        if (shift.shiftID != -1) {
            cv.put(BaseColumns._ID, shift.shiftID);
        }
        cv.put(BEGIN_SHIFT, Util.dateFormat.format(shift.beginShift));
        if (shift.endShift != null) {
            cv.put(END_SHIFT, Util.dateFormat.format(shift.endShift));
        } else {
            cv.put(END_SHIFT, "");
        }
        cv.put(REVENUE_OFFICIAL,        shift.revenueOfficial);
        cv.put(REVENUE_CASH,            shift.revenueCash);
        cv.put(REVENUE_CARD,            shift.revenueCard);
        cv.put(PETROL,                  shift.petrol);
        cv.put(PETROL_FILLED_BY_HANDS,  shift.petrolFilledByHands);
        cv.put(TO_THE_CASHIER,          shift.toTheCashier);
        cv.put(SALARY_OFFICIAL,         shift.salaryOfficial);
        cv.put(REVENUE_BONUS,           shift.revenueBonus);
        cv.put(CAR_RENT,                shift.carRent);
        cv.put(SALARY_UNOFFICIAL,       shift.salaryUnofficial);
        cv.put(WORK_HOURS_SPENT,        shift.workHoursSpent);
        cv.put(SALARY_PER_HOUR,         shift.salaryPerHour);
        return cv;
    }
}
