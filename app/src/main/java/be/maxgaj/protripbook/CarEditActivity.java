package be.maxgaj.protripbook;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import be.maxgaj.protripbook.data.ProtripBookContract;
import be.maxgaj.protripbook.models.Odometer;
import butterknife.BindView;
import butterknife.ButterKnife;

public class CarEditActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private String idCar;
    private static final String TAG = CarEditActivity.class.getSimpleName();
    private static final int CAR_EDIT_LOADER_ID = 40;

    @BindView(R.id.car_input_layout_name) TextInputLayout nameLayout;
    @BindView(R.id.car_input_layout_brand) TextInputLayout brandLayout;
    @BindView(R.id.car_input_layout_plate) TextInputLayout plateLayout;
    @BindView(R.id.car_input_name) EditText nameEditText;
    @BindView(R.id.car_input_brand) EditText brandEditText;
    @BindView(R.id.car_input_plate) EditText plateEditText;
    @BindView(R.id.car_btn_add) Button addButton;
    @BindView(R.id.car_btn_cancel) Button cancelButton;
    @BindView(R.id.car_btn_delete) Button deleteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_edit);
        ButterKnife.bind(this);

        this.nameEditText.addTextChangedListener(new TextListener(this.nameEditText));
        this.brandEditText.addTextChangedListener(new TextListener(this.brandEditText));
        this.plateEditText.addTextChangedListener(new TextListener(this.plateEditText));

        Intent intent = getIntent();
        if (intent.hasExtra(Intent.EXTRA_TEXT))
            this.idCar = intent.getStringExtra(Intent.EXTRA_TEXT);

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

        this.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteCar();
            }
        });


        if (this.idCar!=null && (!(this.idCar.equals(getString(R.string.pref_car_default))))){
            getSupportLoaderManager().initLoader(CAR_EDIT_LOADER_ID, null, this);
        }
    }

    private void submitForm(){
        if (!validateName() || !validateBrand() || !validatePlate())
            return;
        /* Update form */
        String name = this.nameEditText.getText().toString().trim();
        String brand = this.brandEditText.getText().toString().trim();
        String plate = this.plateEditText.getText().toString().trim();

        ContentValues cv = new ContentValues();
        cv.put(ProtripBookContract.CarEntry.COLUMN_NAME, name);
        cv.put(ProtripBookContract.CarEntry.COLUMN_BRAND, brand);
        cv.put(ProtripBookContract.CarEntry.COLUMN_PLATE, plate);
        Uri uriUpdate = ContentUris.withAppendedId(ProtripBookContract.CarEntry.CONTENT_URI, Long.parseLong(idCar));

        try {
            int rowsUpdateted = getContentResolver().update(uriUpdate, cv, null, null);
            if (rowsUpdateted > 0) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.car_edit_confirm), Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.car_edit_error), Toast.LENGTH_SHORT).show();
            Log.d(TAG, "submitForm: Error while updating car");
            e.printStackTrace();
        }
    }

    private void deleteCar(){
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.car_confirm_delete_title))
                .setMessage(getString(R.string.car_confirm_delete_message))
                .setPositiveButton(R.string.car_confirm_delete_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Uri uriCar = ContentUris.withAppendedId(ProtripBookContract.CarEntry.CONTENT_URI, Long.parseLong(idCar));
                        try {
                            getContentResolver().delete(ProtripBookContract.OdometerEntry.CONTENT_URI, ProtripBookContract.OdometerEntry.COLUMN_CAR+"=?", new String[]{idCar});
                            getContentResolver().delete(ProtripBookContract.TripEntry.CONTENT_URI, ProtripBookContract.TripEntry.COLUMN_CAR+"=?", new String[]{idCar});
                            int deletedRows = getContentResolver().delete(uriCar, null, null);
                            if (deletedRows > 0){
                                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                String selectedCar = sharedPreferences.getString(getResources().getString(R.string.pref_car_key), getResources().getString(R.string.pref_car_default));
                                if (selectedCar.equals(idCar)){
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString(getResources().getString(R.string.pref_car_key), getResources().getString(R.string.pref_car_default));
                                    editor.apply();
                                }
                                finish();
                            }
                        } catch (Exception e){
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.car_delete_error), Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "onClick: Error while deleting car");
                            e.printStackTrace();
                        }
                    }
                })
                .setNegativeButton(R.string.car_confirm_delete_no, null).show();
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

    @Override
    protected void onResume() {
        super.onResume();
        if (this.idCar!=null && (!(this.idCar.equals(getString(R.string.pref_car_default)))))
            getSupportLoaderManager().restartLoader(CAR_EDIT_LOADER_ID, null, this);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        return new AsyncTaskLoader<Cursor>(this) {
            Cursor carData = null;

            @Override
            protected void onStartLoading() {
                if (this.carData != null)
                    deliverResult(this.carData);
                else
                    forceLoad();
            }

            @Nullable
            @Override
            public Cursor loadInBackground() {
                try {
                    Uri uri = ContentUris.withAppendedId(ProtripBookContract.CarEntry.CONTENT_URI, Long.parseLong(idCar));
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
                this.carData = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()){
            String name = data.getString(data.getColumnIndex(ProtripBookContract.CarEntry.COLUMN_NAME));
            String brand = data.getString(data.getColumnIndex(ProtripBookContract.CarEntry.COLUMN_BRAND));
            String plate = data.getString(data.getColumnIndex(ProtripBookContract.CarEntry.COLUMN_PLATE));
            this.nameEditText.setText(name);
            this.brandEditText.setText(brand);
            this.plateEditText.setText(plate);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

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
            }
        }
    }
}
