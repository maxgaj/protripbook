package be.maxgaj.protripbook;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.TripViewHolder> {
    private int numberItems;

    public TripAdapter(int numberItems){
        this.numberItems = numberItems;
    }

    @NonNull
    @Override
    public TripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.trip_list_item, parent, false);
        TripViewHolder viewHolder = new TripViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull TripViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return this.numberItems;
    }


    class TripViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.trip_item_tv) TextView tripItemTextView;

        public TripViewHolder(View view){
            super(view);
            ButterKnife.bind(this, view);
        }

        void bind(int listIndex){
            tripItemTextView.setText(String.valueOf(listIndex));
        }
    }
}
