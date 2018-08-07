package be.maxgaj.protripbook;

import android.support.annotation.NonNull;
import android.support.v14.preference.SwitchPreference;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import be.maxgaj.protripbook.data.ProtripBookContract;
import be.maxgaj.protripbook.preference.DatePreference;
import be.maxgaj.protripbook.preference.DatePreferenceDialogFragmentCompat;

public class SettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = SettingsFragment.class.getSimpleName();
    private static final int CARS_LOADER_ID = 20;

    private ListPreference switchCarPreference;
    private SwitchPreference firstOdometerPreference;
    private SwitchPreference lastOdometerPreference;
    private DatePreference firstDatePreference;
    private DatePreference lastDatePreference;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_protripbook);

        this.switchCarPreference = (ListPreference) findPreference(getResources().getString(R.string.pref_car_key)) ;
        this.firstOdometerPreference = (SwitchPreference) findPreference(getResources().getString(R.string.pref_report_first_odometer_key)) ;
        this.lastOdometerPreference = (SwitchPreference) findPreference(getResources().getString(R.string.pref_report_last_odometer_key)) ;
        this.firstDatePreference = (DatePreference) findPreference(getResources().getString(R.string.pref_report_first_date_key)) ;
        this.lastDatePreference = (DatePreference) findPreference(getResources().getString(R.string.pref_report_last_date_key)) ;

        getLoaderManager().initLoader(CARS_LOADER_ID, null, this);
        getLoaderManager().getLoader(CARS_LOADER_ID).startLoading();

        setPreferenceSummaries();

        this.firstDatePreference.setEnabled(!(this.firstOdometerPreference.isChecked()));
        this.lastDatePreference.setEnabled(!(this.lastOdometerPreference.isChecked()));
        this.firstOdometerPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean isChecked = (boolean) newValue;
                firstDatePreference.setEnabled(!isChecked);
                return true;
            }
        });
        this.lastOdometerPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean isChecked = (boolean) newValue;
                lastDatePreference.setEnabled(!isChecked);
                return true;
            }
        });
    }

    private void setPreferenceSummaries(){
        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        preferenceScreen.getSharedPreferences();
        int count = preferenceScreen.getPreferenceCount();

        for (int i=0; i<count; i++){
            Preference p = preferenceScreen.getPreference(i);
            if (p instanceof PreferenceCategory) {
                int nestedCount = ((PreferenceCategory) p).getPreferenceCount();
                for (int j=0; j<nestedCount; j++){
                    Preference nestedPreference = ((PreferenceCategory) p).getPreference(j);
                    if (!( nestedPreference instanceof CheckBoxPreference || nestedPreference instanceof
                    SwitchPreference)) {
                        String value = sharedPreferences.getString( nestedPreference.getKey(), "");
                        setPreferenceSummary( nestedPreference, value);
                    }
                }
            }
            else {
                if (!(p instanceof CheckBoxPreference || p instanceof
                        SwitchPreference)) {
                    String value = sharedPreferences.getString(p.getKey(), "");
                    setPreferenceSummary(p, value);
                }
            }
        }
    }

    private void setPreferenceSummary(Preference preference, String value){
        if (preference instanceof ListPreference){
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(value);
            if (prefIndex >= 0){
                listPreference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);
        if (preference != null){
            if (!(preference instanceof CheckBoxPreference || preference instanceof
                    SwitchPreference)){
                String value = sharedPreferences.getString(preference.getKey(), "");
                setPreferenceSummary(preference, value);
            }
        }
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        DialogFragment dialogFragment = null;
        if (preference instanceof DatePreference){
            dialogFragment = DatePreferenceDialogFragmentCompat.newInstance(preference.getKey());
        }
        if (dialogFragment != null){
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(this.getFragmentManager(), "android.support.v7.preference"+".preferenceFragment.DIALOG");
        }
        else {
            super.onDisplayPreferenceDialog(preference);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(CARS_LOADER_ID, null, this);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<Cursor>(getActivity()) {
            Cursor carsData = null;

            @Override
            protected void onStartLoading() {
                if (this.carsData != null){
                    deliverResult(this.carsData);
                }
                else {
                    forceLoad();
                    switchCarPreference.setEnabled(false);
                }
            }

            @Override
            public Cursor loadInBackground() {
                try {
                    return getContext().getContentResolver().query(ProtripBookContract.CarEntry.CONTENT_URI,
                            null,
                            null,
                            null,
                            null);
                } catch (Exception e){
                    Log.e(TAG, "loadInBackground: Failed to asynchronously load data");
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public void deliverResult(Cursor data) {
                this.carsData = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(@NonNull android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        if (data.getCount() > 0){
            List<String> entriesList = new ArrayList<>();
            List<String> entriesValuesList = new ArrayList<>();
            while(data.moveToNext()){
                long id = data.getLong(data.getColumnIndex(ProtripBookContract.CarEntry._ID));
                String name = data.getString(data.getColumnIndex(ProtripBookContract.CarEntry.COLUMN_NAME));
                entriesList.add(name);
                entriesValuesList.add(String.valueOf(id));
            }
            this.switchCarPreference.setEntries(entriesList.toArray(new CharSequence[entriesList.size()]));
            this.switchCarPreference.setEntryValues(entriesValuesList.toArray(new CharSequence[entriesValuesList.size()]));
            switchCarPreference.setEnabled(true);
            setPreferenceSummaries();
        }
    }

    @Override
    public void onLoaderReset(@NonNull android.support.v4.content.Loader<Cursor> loader) {

    }
}
