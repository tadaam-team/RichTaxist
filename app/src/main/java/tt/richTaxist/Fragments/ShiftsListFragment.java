package tt.richTaxist.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;
import java.util.Calendar;
import tt.richTaxist.Bricks.CustomSpinner;
import tt.richTaxist.Bricks.CustomSpinner.TypeOfSpinner;
import tt.richTaxist.Bricks.DateTimeRangeFrag;
import tt.richTaxist.DB.OrdersSQLHelper;
import tt.richTaxist.DB.ShiftsSQLHelper;
import tt.richTaxist.MainActivity;
import tt.richTaxist.R;
import tt.richTaxist.RecyclerViewAdapter;
import tt.richTaxist.ShiftTotalsActivity;
import tt.richTaxist.Util;
import tt.richTaxist.Units.Shift;

/**
 * Created by TAU on 18.04.2016.
 */

public class ShiftsListFragment extends Fragment implements DateTimeRangeFrag.OnDateTimeRangeFragmentInteractionListener {
    public static final String FRAGMENT_TAG = "ShiftsListFragment";
    private static final String LOG_TAG = "ShiftsListFragment";
    private FragmentActivity mActivity;
    private RecyclerViewAdapter rvAdapter;
    private CustomSpinner spnTaxopark;
    private DateTimeRangeFrag dateTimeRangeFrag;
    private boolean isListSingleVisible;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mActivity = getActivity();
        View rootView = inflater.inflate(R.layout.fragment_shifts_list, container, false);
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        spnTaxopark = (CustomSpinner) rootView.findViewById(R.id.spnTaxopark);
        //TODO: get correct dataSource
//        ShiftsSQLHelper.dbOpenHelper.getShiftsInRangeByTaxopark();

        rvAdapter = new RecyclerViewAdapter(MainActivity.shiftsStorage, RecyclerViewAdapter.AdapterDataType.SHIFT);
        recyclerView.setAdapter(rvAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
//        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        recyclerView.setLayoutManager(layoutManager);
        rvAdapter.setListener(new RecyclerViewAdapter.Listener() {
            @Override
            public void onClick(Object selectedObject) {
                //TODO: rough violation of encapsulation
                MainActivity.currentShift = (Shift) selectedObject;
                MainActivity.ordersStorage.clear();
                MainActivity.ordersStorage.addAll(OrdersSQLHelper.dbOpenHelper.getOrdersByShift(MainActivity.currentShift.shiftID));
                Intent intent = new Intent(getActivity(), ShiftTotalsActivity.class);
                intent.putExtra(ShiftTotalsActivity.EXTRA_AUTHOR, "FirstScreenActivity");
//                TODO: intent.putExtra("selectedShiftId", selectedShift.shiftID);
                getActivity().startActivity(intent);//возможно достаточно startActivity(intent)

                //закрывать стартовый экран можно только после выбора смены, т.к. пользователь может захотеть вернуться в стартовый экран
                mActivity.finish();
            }

            @Override
            public void onClickDelete(Object selectedObject) {
                Shift selectedShift = (Shift) selectedObject;
                if (!selectedShift.hasOrders()) {
                    deleteShift(selectedShift);
                } else {
                    openShiftDeleteDialog(selectedShift);
                }
            }
        });

        FragmentManager fragmentManager = getChildFragmentManager();
        MainActivity.sortShiftsStorage();
        dateTimeRangeFrag = (DateTimeRangeFrag) fragmentManager.findFragmentByTag("dateTimeRangeFrag");
        if (isListSingleVisible) {
            //когда лист единственный на экране, фрагмент следует создать
            if (dateTimeRangeFrag == null) {
                dateTimeRangeFrag = new DateTimeRangeFrag();
                FragmentTransaction ft = fragmentManager.beginTransaction();
                ft.replace(R.id.date_time_input_placeholder, dateTimeRangeFrag, "dateTimeRangeFrag");
                ft.commit();
            }
        } else {
            //когда лист НЕ единственный на экране, фрагмент следует убрать
            if (dateTimeRangeFrag != null) {
                FragmentTransaction ft = fragmentManager.beginTransaction();
                ft.remove(dateTimeRangeFrag);
                ft.commit();
            }
        }
        return rootView;
    }

    public void setSoloView(boolean isListSingleVisible){
        this.isListSingleVisible = isListSingleVisible;
    }

    private void openShiftDeleteDialog(final Shift shift) {
        AlertDialog.Builder quitDialog = new AlertDialog.Builder(mActivity);
        quitDialog.setTitle(R.string.shiftNotEmptyMSG);
        quitDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteShift(shift);
            }
        });
        quitDialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {/*NOP*/}
        });
        quitDialog.show();
    }

    private void deleteShift(Shift shift){
        ShiftsSQLHelper.dbOpenHelper.remove(shift);
        MainActivity.shiftsStorage.remove(shift);
        rvAdapter.notifyDataSetChanged();
        Toast.makeText(mActivity, R.string.shiftDeletedMSG, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        createTaxoparkSpinner();
    }

    public void createTaxoparkSpinner(){
        spnTaxopark.createSpinner(TypeOfSpinner.TAXOPARK, true);
        spnTaxopark.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View itemSelected, int selectedItemPosition, long selectedId) {
                spnTaxopark.saveSpinner(TypeOfSpinner.TAXOPARK);
                if (MainActivity.currentShift != null && dateTimeRangeFrag != null) {
                    MainActivity.shiftsStorage.clear();
                    MainActivity.shiftsStorage.addAll(ShiftsSQLHelper.dbOpenHelper.getShiftsInRangeByTaxopark(
                            dateTimeRangeFrag.getRangeStart(), dateTimeRangeFrag.getRangeEnd(), Util.youngIsOnTop, CustomSpinner.taxoparkID));
                    rvAdapter.notifyDataSetChanged();
                }
            }
            public void onNothingSelected(AdapterView<?> parent) {/*NOP*/}
        });
    }

    @Override
    public void calculate(Calendar rangeStart, Calendar rangeEnd) {
        //been executed each time upon input finish of date/time start/end
        MainActivity.shiftsStorage.clear();
        MainActivity.shiftsStorage.addAll(ShiftsSQLHelper.dbOpenHelper.getShiftsInRangeByTaxopark(
                rangeStart, rangeEnd, Util.youngIsOnTop, CustomSpinner.taxoparkID));
    }
    @Override
    public void refreshControls() {
        rvAdapter.notifyDataSetChanged();
    }
}
