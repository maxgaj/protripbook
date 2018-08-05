package be.maxgaj.protripbook;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import be.maxgaj.protripbook.data.ProtripBookContract;
import be.maxgaj.protripbook.data.ProtripBookDbHelper;
import be.maxgaj.protripbook.data.TestUtil;
import butterknife.BindView;
import butterknife.ButterKnife;

public class TripFragment extends Fragment{
    private TripAdapter tripAdapter;
    private SQLiteDatabase db;

    @BindView(R.id.trip_recycler_view) RecyclerView tripList;

    public TripFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ProtripBookDbHelper dbHelper = new ProtripBookDbHelper(getActivity());
        this.db = dbHelper.getWritableDatabase();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_trip, container, false);
        ButterKnife.bind(this, view);

        /* Data */
        TestUtil.insertFakeData(this.db);
        Cursor cursor = getAllTrips();


        /* RecyclerView */
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        this.tripList.setLayoutManager(layoutManager);
        this.tripList.setHasFixedSize(true);
        this.tripAdapter = new TripAdapter(cursor);
        this.tripList.setAdapter(this.tripAdapter);

        return view;
    }

    private Cursor getAllTrips(){
        return this.db.query(
                ProtripBookContract.TripEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }
}
