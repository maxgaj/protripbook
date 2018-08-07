package be.maxgaj.protripbook.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.preference.DialogPreference;
import android.util.AttributeSet;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import be.maxgaj.protripbook.R;

// https://stackoverflow.com/questions/4216082/how-to-use-datepickerdialog-as-a-preference
// https://medium.com/@JakobUlbrich/building-a-settings-screen-for-android-part-3-ae9793fd31ec
// TODO add summary
public class DatePreference extends DialogPreference {
    private int lastDay=0;
    private int lastMonth=0;
    private int lastYear=0;
    private String dateval;
    private CharSequence summary;
    //private DatePicker picker=null;

    public DatePreference(Context context){
        this(context, null);
    }

    public DatePreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.dialogPreferenceStyle);
    }

    public DatePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, defStyleAttr);
    }

    public DatePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setPositiveButtonText(context.getResources().getString(R.string.pref_report_button_set));
        setNegativeButtonText(context.getResources().getString(R.string.pref_report_button_cancel));
    }

    @Override
    public int getDialogLayoutResource() {
        return R.layout.pref_dialog_date;
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, 0);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        dateval = null;
        if (restorePersistedValue) {
            if (defaultValue == null) {
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
                String formatted = format1.format(cal.getTime());
                dateval = getPersistedString(formatted);
            } else {
                dateval = getPersistedString(defaultValue.toString());
            }
        } else {
            dateval = defaultValue.toString();
        }
        this.lastYear = getYear(dateval);
        this.lastMonth = getMonth(dateval);
        this.lastDay = getDay(dateval);
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

    public void setText(String text){
        this.dateval=text;
        persistString(text);
    }

    public String getText(){
        return dateval;
    }

    public CharSequence getSummary() {
        return this.summary;
    }

    public void setSummary(CharSequence summary) {
        if (summary == null && this.summary != null || summary != null
                && !summary.equals(this.summary)) {
            this.summary = summary;
            notifyChanged();
        }
    }

}
