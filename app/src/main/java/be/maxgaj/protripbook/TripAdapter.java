package be.maxgaj.protripbook;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import be.maxgaj.protripbook.data.ProtripBookContract;
import butterknife.BindView;
import butterknife.ButterKnife;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.TripViewHolder> {
    private Cursor cursor;
    private Context context;
    public AdapterListener listener;

    public static final String TAG = TripAdapter.class.getSimpleName();
    public static final String TRIP_ID = "tripId";

    public TripAdapter(Context context, AdapterListener listener){
        this.listener = listener;
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
        long id = this.cursor.getLong(this.cursor.getColumnIndex(ProtripBookContract.TripEntry._ID));
        String idString = String.valueOf(id);
        String startingLocation = this.cursor.getString(this.cursor.getColumnIndex(ProtripBookContract.TripEntry.COLUMN_STARTING_LOCATION));
        String destinationLocation = this.cursor.getString(this.cursor.getColumnIndex(ProtripBookContract.TripEntry.COLUMN_DESTINATION_LOCATION));
        Long distance = this.cursor.getLong(this.cursor.getColumnIndex(ProtripBookContract.TripEntry.COLUMN_DISTANCE));
        String date = this.cursor.getString(this.cursor.getColumnIndex(ProtripBookContract.TripEntry.COLUMN_DATE));
        int round = this.cursor.getInt(this.cursor.getColumnIndex(ProtripBookContract.TripEntry.COLUMN_ROUND_TRIP));
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.context);
        String unit = sharedPreferences.getString(this.context.getString(R.string.pref_unit_key), this.context.getString(R.string.pref_unit_value_km));

        holder.fromTextView.setText(startingLocation);
        holder.toTextView.setText(destinationLocation);
        holder.readingTextView.setText(String.valueOf(distance));
        holder.dateTextView.setText(date);
        holder.unitTextView.setText(unit);
        if (round==1)
            holder.roundTextView.setText(this.context.getString(R.string.trip_input_hint_round_true));
        else
            holder.roundTextView.setText(this.context.getString(R.string.trip_input_hint_round_false));

    }


    @Override
    public int getItemCount() {
        if (this.cursor != null)
            return this.cursor.getCount();
        return 0;
    }

    public void swapCursor(Cursor newCursor){
        if (newCursor != null && newCursor != this.cursor){
            if (this.cursor != null)
                this.cursor.close();
            this.cursor = newCursor;
            this.notifyDataSetChanged();
        }
    }

    private void deleteItem(int position){
        if (!this.cursor.moveToPosition(position))
            return;
        long id = this.cursor.getLong(this.cursor.getColumnIndex(ProtripBookContract.TripEntry._ID));
        Uri uri = ContentUris.withAppendedId(ProtripBookContract.TripEntry.CONTENT_URI, id);
        try{
            this.context.getContentResolver().delete(uri, null, null);
            Toast.makeText(this.context, this.context.getResources().getString(R.string.trip_delete_confirm), Toast.LENGTH_SHORT).show();
            ProtripBookWidgetService.startActionReport(context);
            this.notifyDataSetChanged();
        } catch (Exception e){
            Toast.makeText(this.context, this.context.getResources().getString(R.string.trip_delete_error), Toast.LENGTH_SHORT).show();
            Log.d(TAG, "onClick: Error while deleting car");
            e.printStackTrace();
        }
    }

    private void editItem(int position){
        if (!this.cursor.moveToPosition(position))
            return;
        long id = this.cursor.getLong(this.cursor.getColumnIndex(ProtripBookContract.TripEntry._ID));
        String idString = String.valueOf(id);
        Intent intent = new Intent(this.context, TripActivity.class);
        intent.putExtra(TRIP_ID, idString);
        this.context.startActivity(intent);
    }


    class TripViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.trip_item_from) TextView fromTextView;
        @BindView(R.id.trip_item_to) TextView toTextView;
        @BindView(R.id.trip_item_date) TextView dateTextView;
        @BindView(R.id.trip_item_round) TextView roundTextView;
        @BindView(R.id.trip_item_reading) TextView readingTextView;
        @BindView(R.id.trip_item_unit) TextView unitTextView;
        @BindView(R.id.trip_item_edit_button) Button editButton;
        @BindView(R.id.trip_item_delete_button) Button deleteButton;

        public TripViewHolder(View view){
            super(view);
            ButterKnife.bind(this, view);

            this.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.OnDeleteClick(v, getAdapterPosition());
                    deleteItem(getAdapterPosition());
                }
            });

            this.editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editItem(getAdapterPosition());
                }
            });
        }
    }

    // https://www.codeproject.com/Tips/1229751/Handle-Click-Events-of-Multiple-Buttons-Inside-a
    public interface AdapterListener{
        void OnDeleteClick(View v, int position);
    }
}
