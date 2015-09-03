package tt.richTaxist.DB.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        super.onCreate(sqLiteDatabase);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        super.onUpgrade(sqLiteDatabase, i, i1);
    }

    public boolean commit(Shift shift){
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

        long result = db.update(TABLE_NAME, cv, SHIFT_ID + " = ?", new String[]{String.valueOf(shift.shiftID)});
        db.close();
        return result != -1;
    }

    public int remove(Shift shift){
        SQLiteDatabase db = getWritableDatabase();
        int result = db.delete(TABLE_NAME, SHIFT_ID + " = ?", new String[]{String.valueOf(shift.shiftID)});
        db.close();
        return result;
    }

    public int remove(List<Shift> shifts){
        SQLiteDatabase db = getWritableDatabase();
        int result = 0;
        for (int i = 0; i < shifts.size(); i++) {
            Shift shift = shifts.get(i);
            result += db.delete(TABLE_NAME, SHIFT_ID + " = ?", new String[]{String.valueOf(shift.shiftID)});
        }
        db.close();
        return result;
    }

    public ArrayList<Shift> getShifts(Date fromDate, Date toDate, boolean youngIsOnTop) {
        ArrayList<Shift> shiftsStorage = new ArrayList<>();
        String sortMethod = "ASC";
        if (youngIsOnTop) sortMethod = "DESC";
        // Select All Query
        //TODO: String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE "
//                + BEGIN_SHIFT + "BETWEEN '" + dateFormat.format(fromDate) + "' and '" + dateFormat.format(toDate)
//                + "' ORDER BY " + BEGIN_SHIFT + " " + sortMethod;
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE "
                + BEGIN_SHIFT + ">='" + dateFormat.format(fromDate) + "' AND "
                + BEGIN_SHIFT + "<='" + dateFormat.format(toDate)
                + "' ORDER BY " + BEGIN_SHIFT + " " + sortMethod;
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do shiftsStorage.add(loadShiftFromCursor(cursor));
            while (cursor.moveToNext());
        }
        //TODO: добавить cursor.close для всех случаев также для Orders и Locations
        return shiftsStorage;
    }

    public ArrayList<Shift> getShiftsForList() {
        ArrayList<Shift> shiftsStorage = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NAME;
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do shiftsStorage.add(loadShiftFromCursor(cursor));
            while (cursor.moveToNext());
        }
        return shiftsStorage;
    }

    public Shift getLastShift() {
        Shift shift = null;
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " ORDER BY _id DESC LIMIT 1";
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do shift = loadShiftFromCursor(cursor);
            while (cursor.moveToNext());
        }
        return shift;
    }

    public Shift getShiftByID(int shiftID) {
        Shift shift = null;
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE " + SHIFT_ID + "='" + String.valueOf(shiftID) + "'";
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        //TODO: в курсоре единственная запись и луп излишен
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do shift = loadShiftFromCursor(cursor);
            while (cursor.moveToNext());
        }
        return shift;
    }

    public boolean isLastShiftClosed(){
        Shift shift = getLastShift();
        if (shift == null) return true;
        return shift.endShift != null;
    }

    private Shift loadShiftFromCursor(Cursor cursor){
        Date beginShift = null;
        Date endShift   = null;
        int shiftID     = 0;
        try {
            shiftID    = cursor.getInt(1);
            beginShift = dateFormat.parse(cursor.getString(2));
            if (cursor.getString(3) != null) endShift = dateFormat.parse(cursor.getString(3));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Shift shift                 = new Shift(shiftID);
        shift.beginShift            = beginShift;
        shift.endShift              = endShift;

        //TODO: shift.revenueOfficial = cursor.getInt(cursor.getColumnIndex(REVENUE_OFFICIAL));
        shift.revenueOfficial       = cursor.getInt(4);
        shift.revenueCash           = cursor.getInt(5);
        shift.revenueCard           = cursor.getInt(6);
        shift.petrol                = cursor.getInt(7);
        shift.petrolFilledByHands   = cursor.getInt(8) != 0;
        shift.toTheCashier          = cursor.getInt(9);
        shift.salaryOfficial        = cursor.getInt(10);
        shift.revenueBonus          = cursor.getInt(11);
        shift.salaryPlusBonus       = cursor.getInt(12);
        shift.workHoursSpent        = cursor.getInt(13);
        shift.salaryPerHour         = cursor.getInt(14);
        shift.distance              = cursor.getInt(15);
        shift.travelTime            = cursor.getInt(16);

        return shift;
    }
}
