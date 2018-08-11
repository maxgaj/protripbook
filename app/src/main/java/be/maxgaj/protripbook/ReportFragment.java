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
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import be.maxgaj.protripbook.data.ProtripBookContract;
import be.maxgaj.protripbook.drive.GenerateReportActivity;
import be.maxgaj.protripbook.models.Report;
import be.maxgaj.protripbook.models.Trip;
import butterknife.BindView;
import butterknife.ButterKnife;

public class ReportFragment extends Fragment implements
        SharedPreferences.OnSharedPreferenceChangeListener,
        LoaderManager.LoaderCallbacks<Report> {
    private String carId;
    private Report report;
    private boolean isFirstDate;
    private String firstPrefDate;
    private boolean isLastDate;
    private String lastPrefDate;
    private String unit;

    @BindView(R.id.report_from) TextView fromTextView;
    @BindView(R.id.report_to) TextView toTextView;
    @BindView(R.id.report_trip) TextView tripTextView;
    @BindView(R.id.report_trip_unit) TextView tripUnitTextView;
    @BindView(R.id.report_odometer) TextView odometerTextView;
    @BindView(R.id.report_odometer_unit) TextView odometerUnitTextView;
    @BindView(R.id.report_ratio) TextView ratioTextView;
    @BindView(R.id.report_button) TextView reportButton;
    @BindView(R.id.report_container) CardView reportContainer;

    private static final String TAG = ReportFragment.class.getSimpleName();
    private static final int REPORT_LOADING_ID = 60;
    public static final String REPORT_EXTRA = "reportExtra";

    public ReportFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        this.carId = sharedPreferences.getString(getResources().getString(R.string.pref_car_key), getResources().getString(R.string.pref_car_default));
        this.isFirstDate = sharedPreferences.getBoolean(getString(R.string.pref_report_first_odometer_key), true);
        this.isLastDate = sharedPreferences.getBoolean(getString(R.string.pref_report_last_odometer_key), true);
        this.firstPrefDate = sharedPreferences.getString(getString(R.string.pref_report_first_date_key), getString(R.string.pref_report_first_date_default));
        this.lastPrefDate = sharedPreferences.getString(getString(R.string.pref_report_last_date_key), getString(R.string.pref_report_last_date_default));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_report, container, false);
        ButterKnife.bind(this, view);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        this.unit = sharedPreferences.getString(getString(R.string.pref_unit_key), getString(R.string.pref_unit_value_km));
        this.tripUnitTextView.setText(this.unit);
        this.odometerUnitTextView.setText(this.unit);

        this.reportButton.setEnabled(false);
        this.reportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateFileReport();
            }
        });

        if(this.carId.equals(getString(R.string.pref_car_default))) {
            this.reportContainer.setVisibility(View.INVISIBLE);
        }
        else {
            this.reportContainer.setVisibility(View.VISIBLE);
        }

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(REPORT_LOADING_ID, null, this);
        if (!(this.carId.equals(getString(R.string.pref_car_default)))){
            getLoaderManager().getLoader(REPORT_LOADING_ID).startLoading();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(this.carId.equals(getString(R.string.pref_car_default))) {
            this.reportContainer.setVisibility(View.INVISIBLE);
        }
        else {
            this.reportContainer.setVisibility(View.VISIBLE);
            restartLoaders();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(getContext()).unregisterOnSharedPreferenceChangeListener(this);
    }

    private void generateFileReport(){
        Intent intent = new Intent(getContext(), GenerateReportActivity.class);
        intent.putExtra(REPORT_EXTRA, this.report);
        startActivity(intent);
    }

    private void restartLoaders(){
        getLoaderManager().restartLoader(REPORT_LOADING_ID, null, this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_unit_key))){
            this.unit = sharedPreferences.getString(getString(R.string.pref_unit_key), getString(R.string.pref_unit_value_km));
            this.tripUnitTextView.setText(this.unit);
            this.odometerUnitTextView.setText(this.unit);
        }
        else if (key.equals(getString(R.string.pref_car_key))){
            this.carId = sharedPreferences.getString(key, getResources().getString(R.string.pref_car_default));
            if(this.carId.equals(getString(R.string.pref_car_default))) {
                this.reportContainer.setVisibility(View.INVISIBLE);
            }
            else {
                this.reportContainer.setVisibility(View.VISIBLE);
                restartLoaders();
            }
        }
        else if (key.equals(getString(R.string.pref_report_first_odometer_key))) {
            this.isFirstDate = sharedPreferences.getBoolean(getString(R.string.pref_report_first_odometer_key), true);
            restartLoaders();
        }
        else if (key.equals(getString(R.string.pref_report_last_odometer_key))) {
            this.isLastDate = sharedPreferences.getBoolean(getString(R.string.pref_report_last_odometer_key), true);
            restartLoaders();
        }
        else if (key.equals(getString(R.string.pref_report_first_date_key))) {
            this.firstPrefDate = sharedPreferences.getString(getString(R.string.pref_report_first_date_key), getString(R.string.pref_report_first_date_default));
            restartLoaders();
        }
        else if (key.equals(getString(R.string.pref_report_last_date_key))) {
            this.lastPrefDate = sharedPreferences.getString(getString(R.string.pref_report_last_date_key), getString(R.string.pref_report_last_date_default));
            restartLoaders();
        }
    }

    @NonNull
    @Override
    public Loader<Report> onCreateLoader(int id, @Nullable Bundle args) {
        return new AsyncTaskLoader<Report>(getActivity()) {
            Report report = null;

            @Override
            protected void onStartLoading() {
                if (this.report != null)
                    deliverResult(this.report);
                else
                    forceLoad();
            }

            @Nullable
            @Override
            public Report loadInBackground() {
                Date fPDate;
                Date lPDate;
                try {
                    fPDate = new SimpleDateFormat(getString(R.string.date_pattern)).parse(firstPrefDate);
                    lPDate = new SimpleDateFormat(getString(R.string.date_pattern)).parse(lastPrefDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                    return null;
                }

                Cursor odometerCursor;
                try {
                    odometerCursor = getContext().getContentResolver().query(ProtripBookContract.OdometerEntry.CONTENT_URI,
                            null,
                            ProtripBookContract.OdometerEntry.COLUMN_CAR+"=?",
                            new String[]{carId},
                            ProtripBookContract.OdometerEntry.COLUMN_DATE+" ASC");
                } catch (Exception e) {
                    Log.e(TAG, "loadInBackground: Failed to asynchronously load data");
                    e.printStackTrace();
                    return null;
                }

                if (odometerCursor==null || odometerCursor.getCount()<1){
                    Log.d(TAG, "loadInBackground: Odometer cursor empty");
                    return null;
                }
                String firstDate=null;
                String lastDate=null;
                float firstDistance=-1;
                float lastDistance=0;
                while (odometerCursor.moveToNext()){
                    String dateString = odometerCursor.getString(odometerCursor.getColumnIndex(ProtripBookContract.OdometerEntry.COLUMN_DATE));
                    Date odometerDate;
                    try {
                        odometerDate = new SimpleDateFormat(getString(R.string.date_pattern)).parse(dateString);
                    } catch (ParseException e) {
                        e.printStackTrace();
                        return null;
                    }
                    if ((isFirstDate && isLastDate) ||
                            (!isFirstDate && isLastDate && (fPDate.before(odometerDate) || fPDate.equals(odometerDate))) ||
                            (isFirstDate && !isLastDate && (lPDate.after(odometerDate) || lPDate.equals(odometerDate))) ||
                            (!isFirstDate && !isLastDate && (fPDate.before(odometerDate) || fPDate.equals(odometerDate)) && (lPDate.after(odometerDate) || lPDate.equals(odometerDate)))){
                        if (firstDate==null)
                            firstDate=dateString;
                        lastDate=dateString;
                        if (firstDistance==-1)
                            firstDistance=odometerCursor.getFloat(odometerCursor.getColumnIndex(ProtripBookContract.OdometerEntry.COLUMN_READING));
                        lastDistance=odometerCursor.getFloat(odometerCursor.getColumnIndex(ProtripBookContract.OdometerEntry.COLUMN_READING));
                    }
                }
                if (firstDate==null || lastDate==null)
                    return null;

                float odometerDistance=0;
                if (lastDistance >= firstDistance)
                    odometerDistance=lastDistance-firstDistance;

                Cursor tripCursor;
                try {
                    tripCursor = getContext().getContentResolver().query(ProtripBookContract.TripEntry.CONTENT_URI,
                            null,
                            ProtripBookContract.TripEntry.COLUMN_CAR+"=?",
                            new String[]{carId},
                            ProtripBookContract.TripEntry.COLUMN_DATE+" ASC");
                } catch (Exception e) {
                    Log.e(TAG, "loadInBackground: Failed to asynchronously load data");
                    e.printStackTrace();
                    return null;
                }

                Date tripFirstDate;
                Date tripLastDate;
                try {
                    tripFirstDate = new SimpleDateFormat(getString(R.string.date_pattern)).parse(firstDate);
                    tripLastDate = new SimpleDateFormat(getString(R.string.date_pattern)).parse(lastDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                    return null;
                }

                List<Trip> tripList = new ArrayList<>();
                float tripDistance = 0;
                if (tripCursor == null)
                    return new Report(firstDate, lastDate, tripDistance, odometerDistance, tripList, unit);
                while (tripCursor.moveToNext()){
                    String tripDateString = tripCursor.getString(tripCursor.getColumnIndex(ProtripBookContract.TripEntry.COLUMN_DATE));
                    Date tripDate;
                    try {
                        tripDate = new SimpleDateFormat(getString(R.string.date_pattern)).parse(tripDateString);
                    } catch (ParseException e) {
                        e.printStackTrace();
                        return null;
                    }

                    if ((isFirstDate && isLastDate) ||
                            (!isFirstDate && isLastDate && (tripFirstDate.before(tripDate) || tripFirstDate.equals(tripDate))) ||
                            (isFirstDate && !isLastDate && (tripLastDate.after(tripDate) || tripLastDate.equals(tripDate))) ||
                            (!isFirstDate && !isLastDate && (tripFirstDate.before(tripDate) || tripFirstDate.equals(tripDate)) && (tripLastDate.after(tripDate) || tripLastDate.equals(tripDate)))){
                        tripDistance += tripCursor.getFloat(tripCursor.getColumnIndex(ProtripBookContract.TripEntry.COLUMN_DISTANCE));
                        tripList.add(new Trip(
                                tripCursor.getInt(tripCursor.getColumnIndex(ProtripBookContract.TripEntry._ID)),
                                tripCursor.getInt(tripCursor.getColumnIndex(ProtripBookContract.TripEntry.COLUMN_CAR)),
                                tripCursor.getString(tripCursor.getColumnIndex(ProtripBookContract.TripEntry.COLUMN_STARTING_LOCATION)),
                                tripCursor.getString(tripCursor.getColumnIndex(ProtripBookContract.TripEntry.COLUMN_DESTINATION_LOCATION)),
                                tripCursor.getInt(tripCursor.getColumnIndex(ProtripBookContract.TripEntry.COLUMN_ROUND_TRIP))==1,
                                tripCursor.getFloat(tripCursor.getColumnIndex(ProtripBookContract.TripEntry.COLUMN_DISTANCE)),
                                tripCursor.getString(tripCursor.getColumnIndex(ProtripBookContract.TripEntry.COLUMN_DATE))
                        ));
                    }
                }

                return new Report(firstDate, lastDate, tripDistance, odometerDistance, tripList, unit);
            }

            @Override
            public void deliverResult(@Nullable Report data) {
                this.report = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Report> loader, Report data) {
        if (data != null) {
            this.report = data;
            this.fromTextView.setText(data.getFirstDate());
            this.toTextView.setText(data.getLastDate());
            this.odometerTextView.setText(String.valueOf(data.getOdometerDistance()));
            this.tripTextView.setText(String.valueOf(data.getTripDistance()));
            this.ratioTextView.setText(String.valueOf(data.getRatio()));
            this.reportButton.setEnabled(true);
        } else {
            Log.d(TAG, "onLoadFinished: Impossible to retrieve report");
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Report> loader) {
        this.report = null;
        this.reportButton.setEnabled(false);
    }

}
