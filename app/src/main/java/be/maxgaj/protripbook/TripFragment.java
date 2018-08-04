package be.maxgaj.protripbook;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TripFragment extends Fragment{
    private TripAdapter tripAdapter;

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
        this.tripAdapter = new TripAdapter(100);
        this.tripList.setAdapter(this.tripAdapter);

        return view;
    }
}
