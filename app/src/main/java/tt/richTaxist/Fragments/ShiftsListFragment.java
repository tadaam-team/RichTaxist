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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Calendar;
import tt.richTaxist.Bricks.CustomSpinner;
import tt.richTaxist.Bricks.CustomSpinner.TypeOfSpinner;
import tt.richTaxist.Bricks.DateTimeRangeFrag;
import tt.richTaxist.DB.Sources.OrdersSource;
import tt.richTaxist.DB.Sources.ShiftsSource;
import tt.richTaxist.FirstScreenActivity;
import tt.richTaxist.MainActivity;
import tt.richTaxist.R;
import tt.richTaxist.RecyclerViewAdapter;
import tt.richTaxist.ShiftTotalsActivity;
import tt.richTaxist.Util;
import tt.richTaxist.Units.Shift;

/**
 * Created by TAU on 18.04.2016.
 */

public class ShiftsListFragment extends Fragment implements DateTimeRangeFrag.DateTimeRangeFragInterface {
    public static final String FRAGMENT_TAG = "ShiftsListFragment";
    private static final String LOG_TAG = FirstScreenActivity.LOG_TAG;
    private FragmentActivity mActivity;
    private RecyclerViewAdapter rvAdapter;
    private CustomSpinner spnTaxopark;
    private DateTimeRangeFrag dateTimeRangeFrag;
    private boolean isListSingleVisible;
    private ShiftsSource shiftsSource;
    private OrdersSource ordersSource;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mActivity = getActivity();
        View rootView = inflater.inflate(R.layout.fragment_shifts_list, container, false);
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        spnTaxopark = (CustomSpinner) rootView.findViewById(R.id.spnTaxopark);
        shiftsSource = new ShiftsSource(getContext());
        ordersSource = new OrdersSource(getContext());
        createDateTimeRangeFrag();

        //we don't need to update shiftsList hence we don't use rvAdapter.notifyDataSetChanged()
        rvAdapter = new RecyclerViewAdapter(shiftsSource.getAllShifts(Util.youngIsOnTop), RecyclerViewAdapter.AdapterDataType.SHIFT);
        recyclerView.setAdapter(rvAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        rvAdapter.setListener(new RecyclerViewAdapter.Listener() {
            @Override
            public void onClick(Object selectedObject) {
                //TODO: rough violation of encapsulation
                MainActivity.currentShift = (Shift) selectedObject;
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
                if (ordersSource.getOrdersList(selectedShift.shiftID, 0).size() == 0) {
                    deleteShift(selectedShift);
                } else {
                    openShiftDeleteDialog(selectedShift);
                }
            }
        });
        return rootView;
    }

    private void createDateTimeRangeFrag(){
        FragmentManager fragmentManager = getChildFragmentManager();
        dateTimeRangeFrag = (DateTimeRangeFrag) fragmentManager.findFragmentByTag(DateTimeRangeFrag.FRAGMENT_TAG);
        if (isListSingleVisible) {
            //когда лист единственный на экране, фрагмент следует создать
            if (dateTimeRangeFrag == null) {
                dateTimeRangeFrag = new DateTimeRangeFrag();
                FragmentTransaction ft = fragmentManager.beginTransaction();
                ft.replace(R.id.date_time_input_placeholder, dateTimeRangeFrag, DateTimeRangeFrag.FRAGMENT_TAG);
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
        shiftsSource.remove(shift);
        rvAdapter.removeObject(shift);
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
            @Override
            public void onItemSelected(AdapterView<?> parent, View itemSelected, int selectedItemPosition, long selectedId) {
                spnTaxopark.saveSpinner(TypeOfSpinner.TAXOPARK);
                ArrayList<Shift> shiftsList;
                if (dateTimeRangeFrag != null){
                    shiftsList = shiftsSource.getShiftsInRangeByTaxopark(dateTimeRangeFrag.getRangeStart(), dateTimeRangeFrag.getRangeEnd(),
                            Util.youngIsOnTop, spnTaxopark.taxoparkID);
                } else {
                    shiftsList = shiftsSource.getShiftsByTaxopark(Util.youngIsOnTop, spnTaxopark.taxoparkID);
                }
                rvAdapter.setObjects(shiftsList);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {/*NOP*/}
        });
    }

    @Override
    public void calculate(Calendar rangeStart, Calendar rangeEnd) {
        //been executed each time upon input finish of date/time start/end
        rvAdapter.setObjects(shiftsSource.getShiftsInRangeByTaxopark(rangeStart, rangeEnd,
                Util.youngIsOnTop, spnTaxopark.taxoparkID));
    }
}
