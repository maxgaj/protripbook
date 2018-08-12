package be.maxgaj.protripbook;

import android.content.ContentUris;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import be.maxgaj.protripbook.data.ProtripBookContract;
import butterknife.BindView;
import butterknife.ButterKnife;


public class CarInfoFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        SharedPreferences.OnSharedPreferenceChangeListener {
    private String carId;

    private static final String TAG = CarInfoFragment.class.getSimpleName();
    private static final int CAR_INFO_LOADER_ID = 30;

    @BindView(R.id.car_info_name_value) TextView nameTextView;
    @BindView(R.id.car_info_brand_value) TextView brandTextView;
    @BindView(R.id.car_info_plate_value) TextView plateTextView;
    @BindView(R.id.car_info_data_container) ConstraintLayout dataContainer;
    @BindView(R.id.car_info_error_container) ConstraintLayout errorContainer;
    @BindView(R.id.car_info_edit_button) Button editButton;
    @BindView(R.id.car_info_settings_button) Button settingsButton;

    public CarInfoFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        this.carId = sharedPreferences.getString(getResources().getString(R.string.pref_car_key), getResources().getString(R.string.pref_car_default));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_car_info, container, false);
        ButterKnife.bind(this, view);
        this.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), CarEditActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT, carId);
                startActivity(intent);
            }
        });

        this.settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), SettingsActivity.class);
                startActivity(intent);
            }
        });

        if(this.carId.equals(getString(R.string.pref_car_default))) {
            displayError();
        }
        else {
            displayData();
        }
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(CAR_INFO_LOADER_ID, null, this);
        if (!(this.carId.equals(getString(R.string.pref_car_default))))
            getLoaderManager().getLoader(CAR_INFO_LOADER_ID).startLoading();
    }

    private void displayError(){
        this.dataContainer.setVisibility(View.INVISIBLE);
        this.errorContainer.setVisibility(View.VISIBLE);
    }

    private void displayData(){
        this.dataContainer.setVisibility(View.VISIBLE);
        this.errorContainer.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(this.carId.equals(getString(R.string.pref_car_default))) {
            displayError();
        }
        else {
            displayData();
            getLoaderManager().restartLoader(CAR_INFO_LOADER_ID, null, this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(getContext()).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_car_key))){
            this.carId = sharedPreferences.getString(key, getResources().getString(R.string.pref_car_default));
            if(this.carId.equals(getString(R.string.pref_car_default))) {
                displayError();
            }
            else {
                displayData();
                getLoaderManager().restartLoader(CAR_INFO_LOADER_ID, null, this);
            }
        }
        ProtripBookWidgetService.startActionReport(getActivity());
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        return new AsyncTaskLoader<Cursor>(getActivity()) {
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
                    Uri uri = ContentUris.withAppendedId(ProtripBookContract.CarEntry.CONTENT_URI, Long.parseLong(carId));
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
            this.nameTextView.setText(name);
            this.brandTextView.setText(brand);
            this.plateTextView.setText(plate);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }
}
