package be.maxgaj.protripbook;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;


import java.util.Calendar;

// http://www.zoftino.com/android-datepicker-example
public class DatePickerFragment extends DialogFragment {

    private static final String ARG_YEAR = "year";
    private static final String ARG_MONTH = "month";
    private static final String ARG_DAY = "DAY";

    private DatePickerDialog.OnDateSetListener dateSetListener;
    private int year;
    private int month;
    private int day;

    public static DatePickerFragment newInstance(int year, int month, int day){
        Bundle args = new Bundle();
        args.putInt(ARG_YEAR, year);
        args.putInt(ARG_MONTH, month);
        args.putInt(ARG_DAY, day);
        DatePickerFragment datePickerFragment = new DatePickerFragment();
        datePickerFragment.setArguments(args);
        return datePickerFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.dateSetListener = (DatePickerDialog.OnDateSetListener) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.year = getArguments().getInt(ARG_YEAR);
        this.month = getArguments().getInt(ARG_MONTH);
        this.day = getArguments().getInt(ARG_DAY);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new DatePickerDialog(getContext(), this.dateSetListener, year, month, day);
    }

}
