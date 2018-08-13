package be.maxgaj.protripbook;


import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import be.maxgaj.protripbook.data.ProtripBookContract;
import butterknife.BindView;
import butterknife.ButterKnife;

public class OdometerFragment extends Fragment implements
        SharedPreferences.OnSharedPreferenceChangeListener,
        LoaderManager.LoaderCallbacks<Cursor> {
    private String carId;

    private static final String TAG = OdometerFragment.class.getSimpleName();
    private static final int ODOMETER_LOADING_ID = 50;

    public static final String INTENT_DATE = "intent_date";
    public static final String INTENT_READING = "intent_reading";


    @BindView(R.id.odometer_date_value) TextView dateTextView;
    @BindView(R.id.odometer_reading_value) TextView readingTextView;
    @BindView(R.id.odometer_unit_value) TextView unitTextView;
    @BindView(R.id.odometer_container) CardView odometerContainer;
    @BindView(R.id.odometer_edit_button) Button updateButton;

    public OdometerFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        this.carId = sharedPreferences.getString(getResources().getString(R.string.pref_car_key), getResources().getString(R.string.pref_car_default));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_odometer, container, false);
        ButterKnife.bind(this, view);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        this.unitTextView.setText(sharedPreferences.getString(getString(R.string.pref_unit_key), getString(R.string.pref_unit_value_km)));

        this.updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), OdometerActivity.class);
                intent.putExtra(INTENT_DATE, dateTextView.getText());
                intent.putExtra(INTENT_READING, readingTextView.getText());
                startActivity(intent);
            }
        });

        if(this.carId.equals(getString(R.string.pref_car_default))) {
            this.odometerContainer.setVisibility(View.INVISIBLE);
        }
        else {
            this.odometerContainer.setVisibility(View.VISIBLE);
        }

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(ODOMETER_LOADING_ID, null, this);
        if (!(this.carId.equals(getString(R.string.pref_car_default))))
            getLoaderManager().getLoader(ODOMETER_LOADING_ID).startLoading();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(this.carId.equals(getString(R.string.pref_car_default))) {
            this.odometerContainer.setVisibility(View.INVISIBLE);
        }
        else {
            this.odometerContainer.setVisibility(View.VISIBLE);
            getLoaderManager().restartLoader(ODOMETER_LOADING_ID, null, this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(getContext()).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_unit_key))){
            this.unitTextView.setText(sharedPreferences.getString(getString(R.string.pref_unit_key), getString(R.string.pref_unit_value_km)));
        }
        else if (key.equals(getString(R.string.pref_car_key))){
            this.carId = sharedPreferences.getString(key, getResources().getString(R.string.pref_car_default));
            if(this.carId.equals(getString(R.string.pref_car_default))) {
                this.odometerContainer.setVisibility(View.INVISIBLE);
            }
            else {
                this.odometerContainer.setVisibility(View.VISIBLE);
                getLoaderManager().restartLoader(ODOMETER_LOADING_ID, null, this);
            }
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        return new AsyncTaskLoader<Cursor>(getActivity()) {
            Cursor odometerData = null;

            @Override
            protected void onStartLoading() {
                if (this.odometerData != null)
                    deliverResult(this.odometerData);
                else
                    forceLoad();
            }

            @Nullable
            @Override
            public Cursor loadInBackground() {
                try {
                    return getContext().getContentResolver().query(ProtripBookContract.OdometerEntry.CONTENT_URI,
                            null,
                            ProtripBookContract.OdometerEntry.COLUMN_CAR+"=?",
                            new String[]{carId},
                            ProtripBookContract.OdometerEntry._ID+" DESC LIMIT 1");
                } catch (Exception e) {
                    Log.e(TAG, "loadInBackground: Failed to asynchronously load data");
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public void deliverResult(@Nullable Cursor data) {
                this.odometerData = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()){
            String date = data.getString(data.getColumnIndex(ProtripBookContract.OdometerEntry.COLUMN_DATE));
            float reading = data.getFloat(data.getColumnIndex(ProtripBookContract.OdometerEntry.COLUMN_READING));
            this.dateTextView.setText(date);
            this.readingTextView.setText(String.valueOf(reading));
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }
}
