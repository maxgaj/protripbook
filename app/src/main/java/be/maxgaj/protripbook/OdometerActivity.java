package be.maxgaj.protripbook;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import be.maxgaj.protripbook.data.ProtripBookContract;
import butterknife.BindView;
import butterknife.ButterKnife;

public class OdometerActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    private static final String TAG = OdometerActivity.class.getSimpleName();

    private Calendar calendar;
    private String carId;
    private String unit;
    private String lastDate;
    private String lastReading;


    private static final String CAR_ID_KEY = "carIdKey";
    private static final String UNIT_KEY = "unitKey";
    private static final String LAST_DATE_KEY = "lastDateKey";
    private static final String LAST_READING_KEY = "lastReadingKey";
    private static final String READING_KEY = "readingKey";
    private static final String DAY_KEY = "dayKey";
    private static final String MONTH_KEY = "monthKey";
    private static final String YEAR_KEY = "yearKey";

    @BindView(R.id.odometer_layout_reading) TextInputLayout readingLayout;
    @BindView(R.id.odometer_layout_date) TextInputLayout dateLayout;
    @BindView(R.id.odometer_reading) EditText readingEditText;
    @BindView(R.id.odometer_date) EditText dateEditText;
    @BindView(R.id.odometer_btn_add) Button addButton;
    @BindView(R.id.odometer_btn_cancel) Button cancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_odometer);
        ButterKnife.bind(this);

        this.calendar = Calendar.getInstance();
        if (savedInstanceState!=null) {
            this.lastReading = savedInstanceState.getString(LAST_READING_KEY);
            this.lastDate = savedInstanceState.getString(LAST_DATE_KEY);
            this.carId = savedInstanceState.getString(CAR_ID_KEY);
            this.unit = savedInstanceState.getString(UNIT_KEY);
            this.readingEditText.setText(savedInstanceState.getString(READING_KEY));
            this.calendar.set(
                    savedInstanceState.getInt(YEAR_KEY),
                    savedInstanceState.getInt(MONTH_KEY),
                    savedInstanceState.getInt(DAY_KEY)
            );
            updateDateLabel();
        }
        else {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            this.carId = sharedPreferences.getString(getString(R.string.pref_car_key), getString(R.string.pref_car_default));
            this.unit = sharedPreferences.getString(getString(R.string.pref_unit_key), getString(R.string.pref_unit_value_km));
            Intent intent = getIntent();
            if (intent.hasExtra(OdometerFragment.INTENT_DATE))
                this.lastDate = intent.getStringExtra(OdometerFragment.INTENT_DATE);
            if (intent.hasExtra(OdometerFragment.INTENT_READING))
                this.lastReading = intent.getStringExtra(OdometerFragment.INTENT_READING);
        }

        this.readingEditText.addTextChangedListener(new TextListener(this.readingEditText));
        this.dateEditText.addTextChangedListener(new TextListener(this.dateEditText));

        this.dateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialogFragment = DatePickerFragment.newInstance(
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                );
                dialogFragment.show(getSupportFragmentManager(), DialogFragment.class.getSimpleName());
            }
        });

        /* Cancel button */
        this.cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        /* Add button */
        this.addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitForm();
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(CAR_ID_KEY, this.carId);
        outState.putString(UNIT_KEY, this.unit);
        outState.putString(LAST_DATE_KEY, this.lastDate);
        outState.putString(LAST_READING_KEY, this.lastReading);
        outState.putString(READING_KEY, this.readingEditText.getText().toString());
        outState.putInt(DAY_KEY, this.calendar.get(Calendar.DAY_OF_MONTH));
        outState.putInt(MONTH_KEY, this.calendar.get(Calendar.MONTH));
        outState.putInt(YEAR_KEY, this.calendar.get(Calendar.YEAR));
        super.onSaveInstanceState(outState);
    }

    private void submitForm(){
        if (!validateDate() || !validateReading())
            return;
        String readingString = this.readingEditText.getText().toString().trim();
        Float reading = Float.valueOf(readingString);
        String date = this.dateEditText.getText().toString().trim();
        int id = Integer.parseInt(this.carId);
        ContentValues cvOdometer = new ContentValues();
        cvOdometer.put(ProtripBookContract.OdometerEntry.COLUMN_CAR, id);
        cvOdometer.put(ProtripBookContract.OdometerEntry.COLUMN_READING, reading);
        cvOdometer.put(ProtripBookContract.OdometerEntry.COLUMN_DATE, date);
        try {
            getContentResolver().insert(ProtripBookContract.OdometerEntry.CONTENT_URI, cvOdometer);
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.odometer_confirm), Toast.LENGTH_SHORT).show();
            ProtripBookWidgetService.startActionReport(this);
            finish();
        } catch (Exception e){
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.odometer_error), Toast.LENGTH_SHORT).show();
            Log.d(TAG, "submitForm: Impossible to update Odometer");
            e.printStackTrace();
        }



    }

    private boolean validateReading(){
        float oldReading = Float.valueOf(this.lastReading);
        String newReading = this.readingEditText.getText().toString().trim();
        if (!(newReading.isEmpty()) && Float.valueOf(newReading) > oldReading){
            this.readingLayout.setErrorEnabled(false);
            return true;
        } else {
            this.readingLayout.setError(getString(R.string.odometer_error_reading)+" (in "+this.unit+")");
            this.readingEditText.requestFocus();
            return false;
        }
    }

    private boolean validateDate(){
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        try {
            Date oldDate = format.parse(this.lastDate);
            Date newDate = format.parse(this.dateEditText.getText().toString().trim());
            if (newDate.compareTo(oldDate) <= 0) {
                this.dateLayout.setError(getString(R.string.odometer_error_date));
                this.dateEditText.requestFocus();
                return false;
            }
        } catch (ParseException e){
            this.dateLayout.setError(getString(R.string.odometer_error_date));
            this.dateEditText.requestFocus();
            return false;
        }
        this.dateLayout.setErrorEnabled(false);
        return true;
    }

    private void updateDateLabel(){
        String format = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.FRANCE);
        dateEditText.setText(sdf.format(this.calendar.getTime()));
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        this.calendar.set(year, month, dayOfMonth);
        updateDateLabel();
    }

    private class TextListener implements TextWatcher {
        private View view;

        private TextListener(View view){
            this.view = view;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            switch (this.view.getId()){
                case R.id.odometer_date:
                    validateDate();
                    break;
                case R.id.odometer_reading:
                    validateReading();
                    break;
            }
        }
    }
}
