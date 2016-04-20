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
import android.widget.LinearLayout.LayoutParams;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Calendar;
import tt.richTaxist.Bricks.DateTimeRangeFrag;
import tt.richTaxist.DB.OrdersSQLHelper;
import tt.richTaxist.DB.ShiftsSQLHelper;
import tt.richTaxist.DB.TaxoparksSQLHelper;
import tt.richTaxist.Enums.ActivityState;
import tt.richTaxist.Enums.TypeOfSpinner;
import tt.richTaxist.FirstScreenActivity;
import tt.richTaxist.MainActivity;
import tt.richTaxist.R;
import tt.richTaxist.RecyclerViewAdapter;
import tt.richTaxist.ShiftTotalsActivity;
import tt.richTaxist.Storage;
import tt.richTaxist.Units.Shift;
import tt.richTaxist.Units.Taxopark;

/**
 * Created by TAU on 18.04.2016.
 */

public class ShiftsListFragment extends Fragment implements DateTimeRangeFrag.OnDateTimeRangeFragmentInteractionListener {
    private static final String LOG_TAG = "ShiftsListFragment";
    private FragmentActivity mActivity;
    private RecyclerViewAdapter adapter;
    private Spinner spnTaxopark;
    private DateTimeRangeFrag dateTimeRangeFrag;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mActivity = getActivity();
        View rootView = inflater.inflate(R.layout.fragment_shifts_list, container, false);
        //TODO: remove crutch
        LayoutParams layoutParams = new LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f);
        rootView.setLayoutParams(layoutParams);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        spnTaxopark = (Spinner) rootView.findViewById(R.id.spnTaxopark);
        //TODO: get correct dataSource
//        ShiftsSQLHelper.dbOpenHelper.getShiftsInRangeByTaxopark();

        adapter = new RecyclerViewAdapter(MainActivity.shiftsStorage, RecyclerViewAdapter.AdapterDataType.SHIFT);
        recyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
//        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        recyclerView.setLayoutManager(layoutManager);
        adapter.setListener(new RecyclerViewAdapter.Listener() {
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
        if (((FirstScreenActivity) mActivity).activityState == ActivityState.PORT_2 ||
                ((FirstScreenActivity) mActivity).activityState == ActivityState.LAND_2) {
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
        adapter.notifyDataSetChanged();
        Toast.makeText(mActivity, R.string.shiftDeletedMSG, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        createTaxoparkSpinner();
    }

    public void createTaxoparkSpinner(){
        ArrayList<Taxopark> list = new ArrayList<>();
        list.add(0, new Taxopark(0, "- - -", false, 0));
        list.addAll(TaxoparksSQLHelper.dbOpenHelper.getAllTaxoparks());
        ArrayAdapter spnTaxoparkAdapter = new ArrayAdapter<>(getActivity(), R.layout.list_entry_spinner, list);
        spnTaxopark.setAdapter(spnTaxoparkAdapter);
        spnTaxopark.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View itemSelected, int selectedItemPosition, long selectedId) {
                Storage.saveSpinner(TypeOfSpinner.TAXOPARK, spnTaxopark);
                if (MainActivity.currentShift != null && dateTimeRangeFrag != null) {
                    MainActivity.shiftsStorage.clear();
                    MainActivity.shiftsStorage.addAll(ShiftsSQLHelper.dbOpenHelper.getShiftsInRangeByTaxopark(
                            dateTimeRangeFrag.getRangeStart(), dateTimeRangeFrag.getRangeEnd(), Storage.youngIsOnTop, Storage.taxoparkID));
                    adapter.notifyDataSetChanged();
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {/*NOP*/}
        });
        Storage.setPositionOfSpinner(TypeOfSpinner.TAXOPARK, spnTaxoparkAdapter, spnTaxopark, 0);
    }

    @Override
    public void calculate(Calendar rangeStart, Calendar rangeEnd) {
        //been executed each time upon input finish of date/time start/end
        MainActivity.shiftsStorage.clear();
        MainActivity.shiftsStorage.addAll(ShiftsSQLHelper.dbOpenHelper.getShiftsInRangeByTaxopark(
                rangeStart, rangeEnd, Storage.youngIsOnTop, Storage.taxoparkID));
    }
    @Override
    public void refreshControls() {
        adapter.notifyDataSetChanged();
    }
}
