package tt.richTaxist.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import java.util.ArrayList;
import java.util.Calendar;
import tt.richTaxist.Bricks.CustomSpinner;
import tt.richTaxist.Bricks.CustomSpinner.TypeOfSpinner;
import tt.richTaxist.Bricks.DateTimeRangeFrag;
import tt.richTaxist.Bricks.SingleChoiceListDF;
import tt.richTaxist.Constants;
import tt.richTaxist.DB.DataSource;
import tt.richTaxist.R;
import tt.richTaxist.Adapters.RecyclerViewShiftAdapter;
import tt.richTaxist.Util;
import tt.richTaxist.Units.Shift;
/**
 * Created by TAU on 18.04.2016.
 */
public class ShiftsListFragment extends Fragment implements
        DateTimeRangeFrag.DateTimeRangeFragInterface,
        SingleChoiceListDF.SingleChoiceListDFInterface {
    public static final String TAG = "ShiftsListFragment";
    private RecyclerViewShiftAdapter rvAdapter;
    private CustomSpinner spnTaxopark;
    private DateTimeRangeFrag dateTimeRangeFrag;
    private boolean isListSingleVisible;
    private DataSource dataSource;
    private ShiftsListFragmentInterface listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            //проверим, реализован ли нужный интерфейс родительским фрагментом или активностью
            listener = (ShiftsListFragmentInterface) getParentFragment();
            if (listener == null) {
                listener = (ShiftsListFragmentInterface) context;
            }
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement ShiftsListFragmentInterface");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_shifts_list, container, false);
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        spnTaxopark = (CustomSpinner) rootView.findViewById(R.id.spnTaxopark);
        dataSource = new DataSource(getContext());
        createDateTimeRangeFrag();

        rvAdapter = new RecyclerViewShiftAdapter(dataSource.getShiftsSource().getAllShifts(Util.youngIsOnTop));
        recyclerView.setAdapter(rvAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        rvAdapter.setListener(new RecyclerViewShiftAdapter.Listener() {
            @Override
            public void onClick(Object selectedObject) {
                Shift selectedShift = (Shift) selectedObject;
                if (!selectedShift.isClosed()) {
                    listener.editShift(selectedShift);
                } else {
                    showShiftIsClosedDialog(selectedShift);
                }
            }

            @Override
            public void onClickMore(Object selectedObject, int positionInRVList) {
                Shift selectedShift = (Shift) selectedObject;
                SingleChoiceListDF dialog = new SingleChoiceListDF();
                Bundle args = new Bundle();
                args.putLong(Constants.OBJECT_ID_EXTRA, selectedShift.shiftID);
                args.putInt(Constants.POSITION_EXTRA, positionInRVList);
                dialog.setArguments(args);
                dialog.show(getChildFragmentManager(), "SingleChoiceListDF");
            }
        });
        return rootView;
    }

    private void showShiftIsClosedDialog(final Shift selectedShift) {
        android.app.AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(getActivity());
        dialog.setTitle(getString(R.string.shiftIsClosed));
        dialog.setMessage(getString(R.string.shiftIsClosedMsg));
        dialog.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedShift.openShift(dataSource);
                listener.editShift(selectedShift);
            }
        });
        dialog.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
               /*NOP*/
            }
        });
        dialog.show();
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
                    shiftsList = dataSource.getShiftsSource().getShiftsInRangeByTaxopark(dateTimeRangeFrag.getRangeStart(), dateTimeRangeFrag.getRangeEnd(),
                            Util.youngIsOnTop, spnTaxopark.taxoparkID);
                } else {
                    shiftsList = dataSource.getShiftsSource().getShiftsByTaxopark(Util.youngIsOnTop, spnTaxopark.taxoparkID);
                }
                rvAdapter.setShifts(shiftsList);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {/*NOP*/}
        });
    }

    public void removeShiftFromList(Shift shift, int positionInRVList){
        if (rvAdapter != null) {
            rvAdapter.removeShiftFromList(shift, positionInRVList);
        }
    }

    @Override
    public void processListItem(long selectedShiftID, int selectedActionID, int positionInRVList) {
        Shift selectedShift = dataSource.getShiftsSource().getShiftByID(selectedShiftID);

        switch (selectedActionID){
            case 0://править
                if (!selectedShift.isClosed()) {
                    listener.editShift(selectedShift);
                } else {
                    showShiftIsClosedDialog(selectedShift);
                }
                break;

            case 1://показать подробности
                AlertDialog.Builder quitDialog = new AlertDialog.Builder(getContext());
                quitDialog.setMessage(selectedShift.getDescription(getContext()));
                //пользователь может нажать на OK или просто в любое место вне окна диалога. оно закроется
                quitDialog.setCancelable(true);
                quitDialog.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { /*NOP*/ }
                });
                quitDialog.show();
                break;

            case 2://удалить
                listener.requestDeleteShift(selectedShift, positionInRVList);
                break;
        }
    }

    @Override
    public void calculate(Calendar rangeStart, Calendar rangeEnd) {
        //been executed each time upon input finish of date/time start/end
        rvAdapter.setShifts(dataSource.getShiftsSource().getShiftsInRangeByTaxopark(rangeStart, rangeEnd,
                Util.youngIsOnTop, spnTaxopark.taxoparkID));
    }

    public interface ShiftsListFragmentInterface{
        void requestDeleteShift(Shift selectedShift, int positionInRVList);
        void editShift(Shift selectedShift);
    }
}
