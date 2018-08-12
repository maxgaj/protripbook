package be.maxgaj.protripbook;

import android.app.DatePickerDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import be.maxgaj.protripbook.data.ProtripBookContract;
import be.maxgaj.protripbook.utils.DistanceUtils;
import butterknife.BindView;
import butterknife.ButterKnife;

public class TripActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private Calendar calendar;
    private String carId;
    private String unit;
    private String tripId;
    private String mode;

    private FusedLocationProviderClient fusedLocationClient;
    private Location lastKnowLocation;
    private String addressOutput;
    private ResultReceiver resultReceiver;

    private static final String EDIT = "edit";
    private static final String CREATE = "create";
    private static final String TAG = TripActivity.class.getSimpleName();
    private static final int TRIP_EDIT_LOADER_ID = 60;

    @BindView(R.id.trip_input_layout_from) TextInputLayout fromLayout;
    @BindView(R.id.trip_input_layout_to) TextInputLayout toLayout;
    @BindView(R.id.trip_input_layout_distance) TextInputLayout distanceLayout;
    @BindView(R.id.trip_input_layout_date) TextInputLayout dateLayout;
    @BindView(R.id.trip_input_from) EditText fromEditText;
    @BindView(R.id.trip_input_to) EditText toEditText;
    @BindView(R.id.trip_input_distance) EditText distanceEditText;
    @BindView(R.id.trip_input_round) SwitchCompat roundSwitch;
    @BindView(R.id.trip_input_date) EditText dateEditText;
    @BindView(R.id.trip_btn_add) Button addButton;
    @BindView(R.id.trip_btn_cancel) Button cancelButton;
    @BindView(R.id.trip_switch_button) Button switchButton;
    @BindView(R.id.trip_calculate_button) Button calculateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        if (intent.hasExtra(TripAdapter.TRIP_ID)){
            this.tripId = intent.getStringExtra(TripAdapter.TRIP_ID);
            this.mode = EDIT;
        }
        else {
            this.mode = CREATE;
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        this.carId = sharedPreferences.getString(getString(R.string.pref_car_key), getString(R.string.pref_car_default));
        this.unit = sharedPreferences.getString(getString(R.string.pref_unit_key), getString(R.string.pref_unit_value_km));

        this.fromEditText.addTextChangedListener(new TextListener(this.fromEditText));
        this.toEditText.addTextChangedListener(new TextListener(this.toEditText));
        this.distanceEditText.addTextChangedListener(new TextListener(this.distanceEditText));
        this.dateEditText.addTextChangedListener(new TextListener(this.dateEditText));
        this.roundSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    buttonView.setText(R.string.trip_input_hint_round_true);
                else
                    buttonView.setText(R.string.trip_input_hint_round_false);
            }
        });

        this.calendar = Calendar.getInstance();
        final DatePickerDialog.OnDateSetListener dateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDateLabel();
            }
        };
        dateEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    new DatePickerDialog(
                            TripActivity.this,
                            dateListener,
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)).show();
                }
            }
        });

        /* Switch button */
        this.switchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchLocation();
            }
        });

        /* Calculate button */
        this.calculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateDistance();
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

        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        this.resultReceiver = new AddressResultReceiver(new Handler());

        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.ACCESS_FINE_LOCATION  }, FetchAddressIntentService.MY_PERMISSION_ACCESS_LOCATION );
        }


        if(this.mode.equals(EDIT)){
            this.addButton.setText(R.string.trip_button_update);
            getSupportActionBar().setTitle(R.string.trip_activity_edit_label);
            getSupportLoaderManager().initLoader(TRIP_EDIT_LOADER_ID, null, this);
        }
        else {
            if ( Build.VERSION.SDK_INT >= 23 &&
                    ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "onCreate: No Permission");
            }
            this.fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            Log.e(TAG, "onSuccess: ");
                            lastKnowLocation = location;
                            if (lastKnowLocation == null)
                                return;
                            if (!Geocoder.isPresent()){
                                Toast.makeText(TripActivity.this,
                                        R.string.address_error_no_geocoder_available,
                                        Toast.LENGTH_LONG).show();
                                return;
                            }
                            Log.e(TAG, "onSuccess: "+lastKnowLocation.getLatitude() );
                            startFetchAddressIntentService();
                        }
                    });
        }
    }

    protected void startFetchAddressIntentService(){
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(FetchAddressIntentService.RECEIVER, this.resultReceiver);
        intent.putExtra(FetchAddressIntentService.LOCATION_DATA_EXTRA, this.lastKnowLocation);
        startService(intent);
    }

    protected void displayAddressOutput(){
        this.fromEditText.setText(this.addressOutput);
    }


    private void submitForm(){
        if (!validateFrom() || !validateTo() || !validateDistance() || !validateDate())
            return;
        String from = this.fromEditText.getText().toString().trim();
        String to = this.toEditText.getText().toString().trim();
        String distanceString = this.distanceEditText.getText().toString().trim();
        String date = this.dateEditText.getText().toString().trim();
        float distance = Float.valueOf(distanceString);
        boolean round = this.roundSwitch.isChecked();

        ContentValues cv = new ContentValues();
        cv.put(ProtripBookContract.TripEntry.COLUMN_STARTING_LOCATION, from);
        cv.put(ProtripBookContract.TripEntry.COLUMN_DESTINATION_LOCATION, to);
        cv.put(ProtripBookContract.TripEntry.COLUMN_DISTANCE, distance);
        cv.put(ProtripBookContract.TripEntry.COLUMN_DATE, date);
        cv.put(ProtripBookContract.TripEntry.COLUMN_ROUND_TRIP, round);
        cv.put(ProtripBookContract.TripEntry.COLUMN_CAR, carId);

        switch (mode){
            case EDIT:
                try {
                    Uri uri = ContentUris.withAppendedId(ProtripBookContract.TripEntry.CONTENT_URI, Long.parseLong(tripId));
                    getContentResolver().update(uri, cv, null, null);
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.trip_update_confirm), Toast.LENGTH_SHORT).show();
                    ProtripBookWidgetService.startActionReport(this);
                    finish();
                } catch (Exception e){
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.trip_update_error), Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "submitForm: Impossible to edit Trip");
                    e.printStackTrace();
                }
                break;
            case CREATE:
                try {
                    getContentResolver().insert(ProtripBookContract.TripEntry.CONTENT_URI, cv);
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.trip_add_confirm), Toast.LENGTH_SHORT).show();
                    ProtripBookWidgetService.startActionReport(this);
                    finish();
                } catch (Exception e){
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.trip_add_error), Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "submitForm: Impossible to add Trip");
                    e.printStackTrace();
                }
                break;
        }
    }

    private void switchLocation(){
        String start = this.fromEditText.getText().toString();
        this.fromEditText.setText(this.toEditText.getText());
        this.toEditText.setText(start);

    }

    private void calculateDistance(){
        String unitQuery = (this.unit.equals(getString(R.string.pref_unit_value_mi)))?getString(R.string.distance_imperial):getString(R.string.distance_metric);
        String origin = this.fromEditText.getText().toString();
        String destination = this.toEditText.getText().toString();

        URL url = DistanceUtils.buildURL(unitQuery, origin, destination);
        if (url!=null){
            new DistanceQueryTask().execute(url);

        }
    }

    private void updateDistance(int distInt){
        float dist = (float) distInt/1000;
        if (this.roundSwitch.isChecked()){
            dist*=2;
        }
        this.distanceEditText.setText(String.format("%.1f",dist).replace(',', '.'));
    }

    private boolean validateFrom(){
        if (this.fromEditText.getText().toString().trim().isEmpty()){
            this.fromLayout.setError(getString(R.string.car_input_error_name));
            this.fromEditText.requestFocus();
            return false;
        } else {
            this.fromLayout.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validateTo(){
        if (this.toEditText.getText().toString().trim().isEmpty()){
            this.toLayout.setError(getString(R.string.car_input_error_name));
            this.toEditText.requestFocus();
            return false;
        } else {
            this.toLayout.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validateDistance(){
        if (this.distanceEditText.getText().toString().trim().isEmpty()){
            this.distanceLayout.setError(getString(R.string.car_input_error_reading));
            this.distanceEditText.requestFocus();
            return false;
        } else {
            this.distanceLayout.setErrorEnabled(false);
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
            this.dateLayout.setError(getString(R.string.trip_input_error_date));
            this.dateEditText.requestFocus();
            return false;
        }
    }

    private void updateDateLabel(){
        String format = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.FRANCE);
        dateEditText.setText(sdf.format(this.calendar.getTime()));
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
                case R.id.trip_input_from:
                    validateFrom();
                    break;
                case R.id.trip_input_to:
                    validateTo();
                    break;
                case R.id.trip_input_distance:
                    validateDistance();
                    break;
                case R.id.trip_input_date:
                    validateDate();
                    break;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(this.mode.equals(EDIT)){
            getSupportLoaderManager().initLoader(TRIP_EDIT_LOADER_ID, null, this);
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        return new AsyncTaskLoader<Cursor>(this) {
            Cursor tripData = null;

            @Override
            protected void onStartLoading() {
                if (this.tripData != null)
                    deliverResult(this.tripData);
                else
                    forceLoad();
            }

            @Nullable
            @Override
            public Cursor loadInBackground() {
                try {
                    Uri uri = ContentUris.withAppendedId(ProtripBookContract.TripEntry.CONTENT_URI, Long.parseLong(tripId));
                    return getContext().getContentResolver().query(uri,
                            null,
                            null,
                            null,
                            null);
                } catch (Exception e) {
                    Log.e(TAG, "loadInBackground: Failed to asynchronously load data");
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public void deliverResult(@Nullable Cursor data) {
                this.tripData = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()){
            String from = data.getString(data.getColumnIndex(ProtripBookContract.TripEntry.COLUMN_STARTING_LOCATION));
            String to = data.getString(data.getColumnIndex(ProtripBookContract.TripEntry.COLUMN_DESTINATION_LOCATION));
            float distance = data.getFloat(data.getColumnIndex(ProtripBookContract.TripEntry.COLUMN_DISTANCE));
            int isRound = data.getInt(data.getColumnIndex(ProtripBookContract.TripEntry.COLUMN_ROUND_TRIP));
            String date = data.getString(data.getColumnIndex(ProtripBookContract.TripEntry.COLUMN_DATE));
            this.fromEditText.setText(from);
            this.toEditText.setText(to);
            this.distanceEditText.setText(String.valueOf(distance));
            this.dateEditText.setText(date);
            this.roundSwitch.setChecked(isRound==1);
            if (isRound==1) {
                this.roundSwitch.setText(R.string.trip_input_hint_round_true);
                this.roundSwitch.setChecked(true);
            }
            else {
                this.roundSwitch.setText(R.string.trip_input_hint_round_false);
                this.roundSwitch.setChecked(false);
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }

    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler){
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (resultData == null)
                return;
            addressOutput = resultData.getString(FetchAddressIntentService.RESULT_DATA_KEY);
            if (addressOutput == null){
                addressOutput="";
            }
            displayAddressOutput();
        }
    }

    public class DistanceQueryTask extends AsyncTask<URL, Void, String> {
        @Override
        protected String doInBackground(URL... urls) {
            URL url = urls[0];
            String distanceJSON = null;
            try {
                distanceJSON = DistanceUtils.getResponseFromHttpUrl(url);
            } catch (IOException e) {
                Log.e(TAG, "doInBackground: ", e);
            }
            return distanceJSON;
        }

        @Override
        protected void onPostExecute(String s) {
            if (s!=null && !s.equals("")){
                String status = DistanceUtils.getStatusFromJSON(s);
                if (status!=null) {
                    if (!status.equals(getString(R.string.distance_ok))) {
                        Log.e(TAG, "onPostExecute: Failed to fetch distance, status="+status);
                    }
                    else {
                            updateDistance(DistanceUtils.getDistanceFromJSON(s));
                    }
                }
            }
        }
    }

}
