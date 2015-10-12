package tt.richTaxist.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import tt.richTaxist.MainActivity;
import tt.richTaxist.Shift;

/**
 * Created by AlexShredder on 29.06.2015.
 */
public class ShiftsSQLHelper extends SQLHelper {
    static final String TABLE_NAME               = "shifts";
    static final String SHIFT_ID                 = "shiftID";
    static final String BEGIN_SHIFT              = "beginShift";
    static final String END_SHIFT                = "endShift";
    static final String REVENUE_OFFICIAL         = "revenueOfficial";
    static final String REVENUE_CASH             = "revenueCash";
    static final String REVENUE_CARD             = "revenueCard";
    static final String PETROL                   = "petrol";
    static final String PETROL_FILLED_BY_HANDS   = "petrolFilledByHands";
    static final String TO_THE_CASHIER           = "toTheCashier";
    static final String SALARY_OFFICIAL          = "salaryOfficial";
    static final String REVENUE_BONUS            = "revenueBonus";
    static final String SALARY_PLUS_BONUS        = "salaryPlusBonus";
    static final String WORK_HOURS_SPENT         = "workHoursSpent";
    static final String SALARY_PER_HOUR          = "salaryPerHour";
    static final String DISTANCE                 = "distance";
    static final String TRAVEL_TIME              = "travelTime";

    public static ShiftsSQLHelper dbOpenHelper = new ShiftsSQLHelper(MainActivity.context);

    static final String CREATE_TABLE = "create table " + TABLE_NAME + " ( _id integer primary key autoincrement, "
            + SHIFT_ID                  + " INT, "
            + BEGIN_SHIFT               + " DATETIME, "
            + END_SHIFT                 + " DATETIME, "
            + REVENUE_OFFICIAL          + " INT,"
            + REVENUE_CASH              + " INT,"
            + REVENUE_CARD              + " INT,"
            + PETROL                    + " INT,"
            + PETROL_FILLED_BY_HANDS    + " BOOLEAN,"
            + TO_THE_CASHIER            + " INT,"
            + SALARY_OFFICIAL           + " INT,"
            + REVENUE_BONUS             + " INT,"
            + SALARY_PLUS_BONUS         + " INT,"
            + WORK_HOURS_SPENT          + " REAL,"
            + SALARY_PER_HOUR           + " INT,"
            + DISTANCE                  + " INT,"
            + TRAVEL_TIME               + " LONGINT)";

    public ShiftsSQLHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public boolean create(Shift shift){
        SQLiteDatabase db = getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(SHIFT_ID, shift.shiftID);
        cv.put(BEGIN_SHIFT, dateFormat.format(shift.beginShift));
        if (shift.endShift != null) cv.put(END_SHIFT, dateFormat.format(shift.endShift));
        else cv.put(END_SHIFT, "");
        cv.put(REVENUE_OFFICIAL,        shift.revenueOfficial);
        cv.put(REVENUE_CASH,            shift.revenueCash);
        cv.put(REVENUE_CARD,            shift.revenueCard);
        cv.put(PETROL,                  shift.petrol);
        cv.put(PETROL_FILLED_BY_HANDS,  shift.petrolFilledByHands);
        cv.put(TO_THE_CASHIER,          shift.toTheCashier);
        cv.put(SALARY_OFFICIAL,         shift.salaryOfficial);
        cv.put(REVENUE_BONUS,           shift.revenueBonus);
        cv.put(SALARY_PLUS_BONUS,       shift.salaryPlusBonus);
        cv.put(WORK_HOURS_SPENT,        shift.workHoursSpent);
        cv.put(SALARY_PER_HOUR,         shift.salaryPerHour);

        long result = db.insert(ShiftsSQLHelper.TABLE_NAME, null, cv);
        db.close();
        return result != -1;
    }

    public boolean update(Shift shift){
        SQLiteDatabase db = getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(SHIFT_ID, shift.shiftID);
        cv.put(BEGIN_SHIFT, dateFormat.format(shift.beginShift));
        if (shift.endShift != null) cv.put(END_SHIFT, dateFormat.format(shift.endShift));
        else cv.put(END_SHIFT, "");
        cv.put(REVENUE_OFFICIAL,        shift.revenueOfficial);
        cv.put(REVENUE_CASH,            shift.revenueCash);
        cv.put(REVENUE_CARD,            shift.revenueCard);
        cv.put(PETROL,                  shift.petrol);
        cv.put(PETROL_FILLED_BY_HANDS,  shift.petrolFilledByHands);
        cv.put(TO_THE_CASHIER,          shift.toTheCashier);
        cv.put(SALARY_OFFICIAL,         shift.salaryOfficial);
        cv.put(REVENUE_BONUS,           shift.revenueBonus);
        cv.put(SALARY_PLUS_BONUS,       shift.salaryPlusBonus);
        cv.put(WORK_HOURS_SPENT,        shift.workHoursSpent);
        cv.put(SALARY_PER_HOUR,         shift.salaryPerHour);

        int result = db.update(TABLE_NAME, cv, SHIFT_ID + " = ?", new String[]{String.valueOf(shift.shiftID)});
        db.close();
        return result != -1;
    }

    public int remove(Shift shift){
        SQLiteDatabase db = getWritableDatabase();
        int result = db.delete(TABLE_NAME, SHIFT_ID + " = ?", new String[]{String.valueOf(shift.shiftID)});
        db.close();
        return result;
    }

    public ArrayList<Shift> getShiftsInRange(Date fromDate, Date toDate, boolean youngIsOnTop) {
        ArrayList<Shift> shiftsStorage = new ArrayList<>();
        String sortMethod = "ASC";
        if (youngIsOnTop) sortMethod = "DESC";
        SQLiteDatabase db = getWritableDatabase();
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE "
                + BEGIN_SHIFT + ">='" + dateFormat.format(fromDate) + "' AND "
                + BEGIN_SHIFT + "<='" + dateFormat.format(toDate)
                + "' ORDER BY " + BEGIN_SHIFT + " " + sortMethod;
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do shiftsStorage.add(loadShiftFromCursor(cursor));
            while (cursor.moveToNext());
        }
        return shiftsStorage;
    }

    public ArrayList<Shift> getAllShifts() {
        ArrayList<Shift> shiftsStorage = new ArrayList<>();
        // Select All Query
        SQLiteDatabase db = getWritableDatabase();
        String selectQuery = "SELECT  * FROM " + TABLE_NAME;
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do shiftsStorage.add(loadShiftFromCursor(cursor));
            while (cursor.moveToNext());
        }
        return shiftsStorage;
    }

//    public ArrayList<Shift> getShiftsByMonthAndTaxopark(int monthID, int taxoparkID) {
//        ArrayList<Shift> shiftList = new ArrayList<>();
//        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE "
//                + SHIFT_ID + "='" + String.valueOf(shiftID)  + "' AND "
//                + TAXOPARK_ID + "='" + String.valueOf(taxoparkID) + "'";
//
//        SQLiteDatabase db = getWritableDatabase();
//        Cursor cursor = db.rawQuery(selectQuery, null);
//
//        // looping through all rows and adding to list
//        if (cursor.moveToFirst()) {
//            do shiftList.add(loadShiftFromCursor(cursor));
//            while (cursor.moveToNext());
//        }
//        return shiftList;
//    }

    public Shift getLastShift() {
        Shift shift = null;
        SQLiteDatabase db = getWritableDatabase();
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " ORDER BY _id DESC LIMIT 1";
        Cursor cursor = db.rawQuery(selectQuery, null);

        // в курсоре единственная запись и луп излишен
        if (cursor.moveToFirst()) {
            do shift = loadShiftFromCursor(cursor);
            while (cursor.moveToNext());
        }
        return shift;
    }

    public Shift getShiftByID(int shiftID) {
        Shift shift = null;
        SQLiteDatabase db = getWritableDatabase();
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE " + SHIFT_ID + "='" + String.valueOf(shiftID) + "'";
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) shift = loadShiftFromCursor(cursor);
        return shift;
    }

    private Shift loadShiftFromCursor(Cursor cursor){
        Date beginShift = null;
        Date endShift   = null;
        int shiftID     = cursor.getInt(cursor.getColumnIndex(SHIFT_ID));

        try {
            beginShift = dateFormat.parse(cursor.getString(cursor.getColumnIndex(BEGIN_SHIFT)));
            if (cursor.getString(cursor.getColumnIndex(END_SHIFT)) != null)
                endShift = dateFormat.parse(cursor.getString(cursor.getColumnIndex(END_SHIFT)));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Shift shift                 = new Shift(shiftID);
        shift.beginShift            = beginShift;
        shift.endShift              = endShift;

        shift.revenueOfficial       = cursor.getInt(cursor.getColumnIndex(REVENUE_OFFICIAL));
        shift.revenueCash           = cursor.getInt(cursor.getColumnIndex(REVENUE_CASH));
        shift.revenueCard           = cursor.getInt(cursor.getColumnIndex(REVENUE_CARD));
        shift.petrol                = cursor.getInt(cursor.getColumnIndex(PETROL));
        shift.petrolFilledByHands   = cursor.getInt(cursor.getColumnIndex(PETROL_FILLED_BY_HANDS)) != 0;
        shift.toTheCashier          = cursor.getInt(cursor.getColumnIndex(TO_THE_CASHIER));
        shift.salaryOfficial        = cursor.getInt(cursor.getColumnIndex(SALARY_OFFICIAL));
        shift.revenueBonus          = cursor.getInt(cursor.getColumnIndex(REVENUE_BONUS));
        shift.salaryPlusBonus       = cursor.getInt(cursor.getColumnIndex(SALARY_PLUS_BONUS));
        shift.workHoursSpent        = cursor.getInt(cursor.getColumnIndex(WORK_HOURS_SPENT));
        shift.salaryPerHour         = cursor.getInt(cursor.getColumnIndex(SALARY_PER_HOUR));
        shift.distance              = cursor.getInt(cursor.getColumnIndex(DISTANCE));
        shift.travelTime            = cursor.getInt(cursor.getColumnIndex(TRAVEL_TIME));

        return shift;
    }
}