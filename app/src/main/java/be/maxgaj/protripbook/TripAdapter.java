package be.maxgaj.protripbook;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import be.maxgaj.protripbook.data.ProtripBookContract;
import butterknife.BindView;
import butterknife.ButterKnife;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.TripViewHolder> {
    private Cursor cursor;
    private Context context;

    public TripAdapter(Context context){
        this.context = context;
    }

    @NonNull
    @Override
    public TripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(this.context);
        View view = inflater.inflate(R.layout.trip_list_item, parent, false);
        TripViewHolder viewHolder = new TripViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull TripViewHolder holder, int position) {
        if (!this.cursor.moveToPosition(position))
            return;
        String startingLocation = this.cursor.getString(this.cursor.getColumnIndex(ProtripBookContract.TripEntry.COLUMN_STARTING_LOCATION));
        String destinationLocation = this.cursor.getString(this.cursor.getColumnIndex(ProtripBookContract.TripEntry.COLUMN_DESTINATION_LOCATION));
        Long distance = this.cursor.getLong(this.cursor.getColumnIndex(ProtripBookContract.TripEntry.COLUMN_DISTANCE));
        String text = "From " + startingLocation + " To " + destinationLocation + " in " + String.valueOf(distance) + " km";
        holder.tripItemTextView.setText(text);
    }

    @Override
    public int getItemCount() {
        if (this.cursor != null)
            return this.cursor.getCount();
        return 0;
    }

    public void swapCursor(Cursor newCursor){
        if (this.cursor != null)
            this.cursor.close();
        this.cursor = newCursor;
        if (newCursor != null)
            this.notifyDataSetChanged();
    }


    class TripViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.trip_item_tv) TextView tripItemTextView;

        public TripViewHolder(View view){
            super(view);
            ButterKnife.bind(this, view);
        }

//        void bind(int listIndex){
//            tripItemTextView.setText(String.valueOf(listIndex));
//        }
    }
}
