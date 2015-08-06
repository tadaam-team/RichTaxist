package tt.richTaxist.DB;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import tt.richTaxist.DB.sql.ShiftsSQLHelper;
import tt.richTaxist.MainActivity;
import tt.richTaxist.Shift;

/**
 * Created by AlexShredder on 29.06.2015.
 */
public class ShiftsStorage {

    private static ShiftsSQLHelper dbOpenHelper = new ShiftsSQLHelper(MainActivity.context);

    private ShiftsStorage() {
    }

    private static ShiftsSQLHelper getDatabase() {
        return dbOpenHelper;
    }
    public static boolean commit(Shift shift){
        return getDatabase().commit(shift);
    }
    public static int remove(Shift shift){
        return getDatabase().remove(shift);
    }
    public static int remove(List<Shift> shifts){
        return getDatabase().remove(shifts);
    }
    public static ArrayList<Shift> getShifts(Date fromDate, Date toDate, boolean youngIsOnTop) {
        return getDatabase().getShifts(fromDate, toDate, youngIsOnTop);
    }
    public static Shift getShiftByID(String shiftID){
        return getDatabase().getShiftByID(shiftID);
    }
    public static boolean isLastShiftClosed() {
        return getDatabase().isLastShiftClosed();
    }
    public static Shift getLastShift() {
        return getDatabase().getLastShift();
    }
    public static ArrayList<Shift> getShifts(Date date, boolean youngIsOnTop) {
        return getShifts(date, date, youngIsOnTop);
    }

    public static void update(Shift shift) {
        getDatabase().update(shift);
    }

    public static ArrayList<Shift> getShiftsForList() {
        return getDatabase().getShiftsForList();
    }

}
