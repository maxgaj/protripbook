package be.maxgaj.protripbook;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import be.maxgaj.protripbook.data.ProtripBookContract;
import butterknife.BindView;
import butterknife.ButterKnife;

public class TripFragment extends Fragment implements
        SharedPreferences.OnSharedPreferenceChangeListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private TripAdapter tripAdapter;
    private String carId;

    private static final String TAG = TripFragment.class.getSimpleName();
    private static final int TRIP_LOADER_ID = 10;

    @BindView(R.id.trip_recycler_view) RecyclerView tripList;
    @BindView(R.id.trip_fab) FloatingActionButton fab;

    public TripFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        this.carId = sharedPreferences.getString(getResources().getString(R.string.pref_car_key), getResources().getString(R.string.pref_car_default));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_trip, container, false);
        ButterKnife.bind(this, view);

        if (this.carId.equals(getString(R.string.pref_car_default)))
            this.fab.setVisibility(View.INVISIBLE);

        this.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), TripActivity.class);
                startActivity(intent);
            }
        });


        /* RecyclerView */
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        this.tripList.setLayoutManager(layoutManager);
        this.tripList.setHasFixedSize(true);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this.tripList.getContext(), layoutManager.getOrientation());
        this.tripList.addItemDecoration(dividerItemDecoration);
        this.tripAdapter = new TripAdapter(getActivity(), new TripAdapter.AdapterListener() {
            @Override
            public void OnDeleteClick(View v, int position) {
                onResume();
            }
        });
        this.tripList.setAdapter(this.tripAdapter);


        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(TRIP_LOADER_ID, null, this);
        if (!(this.carId.equals(getString(R.string.pref_car_default))))
            getLoaderManager().getLoader(TRIP_LOADER_ID).startLoading();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(this.carId.equals(getString(R.string.pref_car_default))) {
            this.fab.setVisibility(View.INVISIBLE);
        }
        else {
            getLoaderManager().restartLoader(TRIP_LOADER_ID, null, this);
            this.fab.setVisibility(View.VISIBLE);
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
                this.fab.setVisibility(View.INVISIBLE);
            }
            else {
                getLoaderManager().restartLoader(TRIP_LOADER_ID, null, this);
                this.fab.setVisibility(View.VISIBLE);
            }
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        return new AsyncTaskLoader<Cursor>(getActivity()) {
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
                    return getContext().getContentResolver().query(ProtripBookContract.TripEntry.CONTENT_URI,
                            null,
                            ProtripBookContract.TripEntry.COLUMN_CAR+"=?",
                            new String[]{carId},
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
        this.tripAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        this.tripAdapter.swapCursor(null);
    }

}
