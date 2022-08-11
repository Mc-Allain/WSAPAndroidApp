package com.example.wsapandroidapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.wsapandroidapp.DataModel.ExpoExhibitor2;
import com.example.wsapandroidapp.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class ExpoExhibitorAdapter extends RecyclerView.Adapter<ExpoExhibitorAdapter.ViewHolder> {

    private final List<ExpoExhibitor2> expoExhibitors;

    private final  LayoutInflater layoutInflater;

    private final Context context;

    public ExpoExhibitorAdapter(Context context, List<ExpoExhibitor2> expoExhibitors) {
        this.expoExhibitors = expoExhibitors;
        this.layoutInflater = LayoutInflater.from(context);

        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.custom_expo_exhibitor_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CardView cardView = holder.cardView;
        ImageView imgPoster = holder.imgPoster;

        ExpoExhibitor2 expoExhibitor = expoExhibitors.get(position);

        Glide.with(context).load(expoExhibitor.getImage()).centerCrop().placeholder(R.drawable.ic_wsap).
                error(R.drawable.ic_wsap).into(imgPoster);

        cardView.setOnClickListener(view -> {
            if (adapterListener != null) adapterListener.onClick(expoExhibitor);
        });
    }

    @Override
    public int getItemCount() {
        return expoExhibitors.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        CardView cardView;
        ImageView imgPoster;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardView);
            imgPoster = itemView.findViewById(R.id.imgPoster);

            setIsRecyclable(false);
        }
    }

    private AdapterListener adapterListener;

    public interface AdapterListener {
        void onClick(ExpoExhibitor2 expoExhibitor);
    }

    public void setAdapterListener(AdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }
}
