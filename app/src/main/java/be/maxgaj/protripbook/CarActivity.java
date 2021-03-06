package be.maxgaj.protripbook;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import be.maxgaj.protripbook.data.ProtripBookContract;
import butterknife.BindView;
import butterknife.ButterKnife;

public class CarActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    private Calendar calendar;

    private static final String NAME_KEY = "nameKey";
    private static final String BRAND_KEY = "BrandKey";
    private static final String PLATE_KEY = "plateKey";
    private static final String READING_KEY = "readingKey";
    private static final String DAY_KEY = "dayKey";
    private static final String MONTH_KEY = "monthKey";
    private static final String YEAR_KEY = "yearKey";

    @BindView(R.id.car_input_layout_name) TextInputLayout nameLayout;
    @BindView(R.id.car_input_layout_brand) TextInputLayout brandLayout;
    @BindView(R.id.car_input_layout_plate) TextInputLayout plateLayout;
    @BindView(R.id.car_input_layout_reading) TextInputLayout readingLayout;
    @BindView(R.id.car_input_layout_date) TextInputLayout dateLayout;
    @BindView(R.id.car_input_name) EditText nameEditText;
    @BindView(R.id.car_input_brand) EditText brandEditText;
    @BindView(R.id.car_input_plate) EditText plateEditText;
    @BindView(R.id.car_input_reading) EditText readingEditText;
    @BindView(R.id.car_input_date) EditText dateEditText;
    @BindView(R.id.car_btn_add) Button addButton;
    @BindView(R.id.car_btn_cancel) Button cancelButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car);
        ButterKnife.bind(this);

        this.calendar = Calendar.getInstance();
        if (savedInstanceState!=null) {
            this.nameEditText.setText(savedInstanceState.getString(NAME_KEY));
            this.brandEditText.setText(savedInstanceState.getString(BRAND_KEY));
            this.plateEditText.setText(savedInstanceState.getString(PLATE_KEY));
            this.readingEditText.setText(savedInstanceState.getString(READING_KEY));
            this.calendar.set(
                    savedInstanceState.getInt(YEAR_KEY),
                    savedInstanceState.getInt(MONTH_KEY),
                    savedInstanceState.getInt(DAY_KEY)
            );
            updateDateLabel();
        }

        //https://www.androidhive.info/2015/09/android-material-design-floating-labels-for-edittext/
        this.nameEditText.addTextChangedListener(new TextListener(this.nameEditText));
        this.brandEditText.addTextChangedListener(new TextListener(this.brandEditText));
        this.plateEditText.addTextChangedListener(new TextListener(this.plateEditText));
        this.readingEditText.addTextChangedListener(new TextListener(this.readingEditText));
        this.dateEditText.addTextChangedListener(new TextListener(this.dateEditText));

        /* Date picker */
        // https://stackoverflow.com/questions/14933330/datepicker-how-to-popup-datepicker-when-click-on-edittext
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
        outState.putString(BRAND_KEY, this.brandEditText.getText().toString());
        outState.putString(NAME_KEY, this.nameEditText.getText().toString());
        outState.putString(PLATE_KEY, this.plateEditText.getText().toString());
        outState.putString(READING_KEY, this.readingEditText.getText().toString());
        outState.putInt(DAY_KEY, this.calendar.get(Calendar.DAY_OF_MONTH));
        outState.putInt(MONTH_KEY, this.calendar.get(Calendar.MONTH));
        outState.putInt(YEAR_KEY, this.calendar.get(Calendar.YEAR));
        super.onSaveInstanceState(outState);
    }

    private void submitForm(){
        if (!validateName() || !validateBrand() || !validatePlate() || !validateReading() || !validateDate())
            return;
        /* Insert form */
        String name = this.nameEditText.getText().toString().trim();
        String brand = this.brandEditText.getText().toString().trim();
        String plate = this.plateEditText.getText().toString().trim();
        String readingString = this.readingEditText.getText().toString().trim();
        Float reading = Float.valueOf(readingString);
        String date = this.dateEditText.getText().toString().trim();


        ContentValues cv = new ContentValues();
        cv.put(ProtripBookContract.CarEntry.COLUMN_NAME, name);
        cv.put(ProtripBookContract.CarEntry.COLUMN_BRAND, brand);
        cv.put(ProtripBookContract.CarEntry.COLUMN_PLATE, plate);
        Uri uri = getContentResolver().insert(ProtripBookContract.CarEntry.CONTENT_URI, cv);

        if (uri != null) {
            String idCarString = uri.getLastPathSegment();
            int idCar = Integer.parseInt(idCarString);
            ContentValues cvOdometer = new ContentValues();
            cvOdometer.put(ProtripBookContract.OdometerEntry.COLUMN_CAR, idCar);
            cvOdometer.put(ProtripBookContract.OdometerEntry.COLUMN_READING, reading);
            cvOdometer.put(ProtripBookContract.OdometerEntry.COLUMN_DATE, date);
            Uri uriOdometer = getContentResolver().insert(ProtripBookContract.OdometerEntry.CONTENT_URI, cvOdometer);

            if (uriOdometer != null) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.car_confirm), Toast.LENGTH_SHORT).show();

                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                String carIdString = sharedPreferences.getString(getResources().getString(R.string.pref_car_key), getResources().getString(R.string.pref_car_default));
                if (carIdString == getResources().getString(R.string.pref_car_default)){
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(getResources().getString(R.string.pref_car_key), idCarString);
                    editor.apply();
                }
                ProtripBookWidgetService.startActionReport(this);
                finish();
            }
        }
    }

    private boolean validateName(){
        if (this.nameEditText.getText().toString().trim().isEmpty()){
            this.nameLayout.setError(getString(R.string.car_input_error_name));
            this.nameEditText.requestFocus();
            return false;
        } else {
            this.nameLayout.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validateBrand(){
        if (this.brandEditText.getText().toString().trim().isEmpty()){
            this.brandLayout.setError(getString(R.string.car_input_error_brand));
            this.brandEditText.requestFocus();
            return false;
        } else {
            this.brandLayout.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validatePlate(){
        if (this.plateEditText.getText().toString().trim().isEmpty()){
            this.plateLayout.setError(getString(R.string.car_input_error_plate));
            this.plateEditText.requestFocus();
            return false;
        } else {
            this.plateLayout.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validateReading(){
        if (this.readingEditText.getText().toString().trim().isEmpty()){
            this.readingLayout.setError(getString(R.string.car_input_error_reading));
            this.readingEditText.requestFocus();
            return false;
        } else {
            this.readingLayout.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validateDate(){
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        try {
            format.parse(this.dateEditText.getText().toString().trim());
            this.dateLayout.setErrorEnabled(false);
            return true;
        } catch (ParseException e){
            this.dateLayout.setError(getString(R.string.car_input_error_date));
            this.dateEditText.requestFocus();
            return false;
        }
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
                case R.id.car_input_name:
                    validateName();
                    break;
                case R.id.car_input_brand:
                    validateBrand();
                    break;
                case R.id.car_input_plate:
                    validatePlate();
                    break;
                case R.id.car_input_reading:
                    validateReading();
                    break;
                case R.id.car_input_date:
                    validateDate();
                    break;
            }
        }
    }

}
