package be.maxgaj.protripbook;

import android.support.v4.app.LoaderManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import be.maxgaj.protripbook.data.ProtripBookContract;
import be.maxgaj.protripbook.data.ProtripBookDbHelper;
import butterknife.BindView;
import butterknife.ButterKnife;

public class TripFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private TripAdapter tripAdapter;

    private static final String TAG = TripFragment.class.getSimpleName();
    private static final int TRIP_LOADER_ID = 10;

    @BindView(R.id.trip_recycler_view) RecyclerView tripList;

    public TripFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_trip, container, false);
        ButterKnife.bind(this, view);


        /* RecyclerView */
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        this.tripList.setLayoutManager(layoutManager);
        this.tripList.setHasFixedSize(true);
        this.tripAdapter = new TripAdapter(getActivity());
        this.tripList.setAdapter(this.tripAdapter);


        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(TRIP_LOADER_ID, null, this);
        getLoaderManager().getLoader(TRIP_LOADER_ID).startLoading();
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(TRIP_LOADER_ID, null, this);
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
        this.tripAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        this.tripAdapter.swapCursor(null);
    }

}
