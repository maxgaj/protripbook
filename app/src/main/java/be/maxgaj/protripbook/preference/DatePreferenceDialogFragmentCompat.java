package be.maxgaj.protripbook.preference;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.view.View;
import android.widget.DatePicker;

import be.maxgaj.protripbook.R;
import butterknife.BindView;
import butterknife.ButterKnife;

public class DatePreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat {
    @BindView(R.id.pref_report_datepicker) DatePicker datePicker;

    public static DatePreferenceDialogFragmentCompat newInstance(String key){
        final DatePreferenceDialogFragmentCompat fragment = new DatePreferenceDialogFragmentCompat();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult){
            int day = this.datePicker.getDayOfMonth();
            int month = this.datePicker.getMonth()+1;
            int year = this.datePicker.getYear();
            String dayString;
            String monthString;
            if (day<10)
                dayString = "0"+String.valueOf(day);
            else
                dayString = String.valueOf(day);
            if (month<10)
                monthString = "0"+String.valueOf(month);
            else
                monthString = String.valueOf(month);
            String dateval = dayString+"/"+monthString+"/"+String.valueOf(year);
            DialogPreference preference = getPreference();
            if (preference instanceof DatePreference){
                DatePreference datePreference = ((DatePreference) preference);
                if (datePreference.callChangeListener(dateval)){
                    datePreference.setText(dateval);
                }
            }
        }
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        ButterKnife.bind(this, view);
        String dateval = null;
        DialogPreference preference = getPreference();
        if (preference instanceof DatePreference){
            dateval = ((DatePreference) preference).getText();
        }
        if (dateval != null){
            int day = getDay(dateval);
            int month = getMonth(dateval)-1;
            int year = getYear(dateval);
            this.datePicker.updateDate(year, month, day);
        }
    }

    public static int getDay(String dateval){
        return getDatePart(dateval, 0);
    }
    public static int getMonth(String dateval){
        return getDatePart(dateval, 1);
    }
    public static int getYear(String dateval){
        return getDatePart(dateval, 2);
    }

    private static int getDatePart(String dateval, int part){
        String[] pieces = dateval.split("/");
        return (Integer.parseInt(pieces[part]));
    }
}
