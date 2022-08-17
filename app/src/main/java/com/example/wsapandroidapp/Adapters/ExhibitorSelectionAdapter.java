package com.example.wsapandroidapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.wsapandroidapp.DataModel.ExpoExhibitor;
import com.example.wsapandroidapp.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class ExhibitorSelectionAdapter extends RecyclerView.Adapter<ExhibitorSelectionAdapter.ViewHolder> {

    private final List<ExpoExhibitor> expoExhibitors;
    private final List<ExpoExhibitor> selectedExpoExhibitors;

    private final  LayoutInflater layoutInflater;

    private final Context context;

    public ExhibitorSelectionAdapter(Context context, List<ExpoExhibitor> expoExhibitors,
                                     List<ExpoExhibitor> selectedExpoExhibitors) {
        this.expoExhibitors = expoExhibitors;
        this.selectedExpoExhibitors = selectedExpoExhibitors;
        this.layoutInflater = LayoutInflater.from(context);

        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.custom_exhibitor_selection_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CardView cardView = holder.cardView;
        ConstraintLayout checkLayout = holder.checkLayout;
        ImageView imgPoster = holder.imgPoster;

        ExpoExhibitor expoExhibitor = expoExhibitors.get(position);

        boolean isSelected = false;
        for (ExpoExhibitor selectedExhibitor : selectedExpoExhibitors)
            if (expoExhibitor.getId().equals(selectedExhibitor.getId())) {
                isSelected = true;
                break;
            }

        Glide.with(context).load(expoExhibitor.getImage()).centerCrop().placeholder(R.drawable.ic_wsap).
                error(R.drawable.ic_wsap).into(imgPoster);

        checkLayout.setVisibility(View.GONE);
        if (isSelected) checkLayout.setVisibility(View.VISIBLE);

        boolean finalIsSelected = isSelected;
        cardView.setOnClickListener(view -> {
            if (adapterListener != null) {
                if (finalIsSelected) adapterListener.onUnselect(expoExhibitor);
                else adapterListener.onSelect(expoExhibitor);
            }
        });
    }

    @Override
    public int getItemCount() {
        return expoExhibitors.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        CardView cardView;
        ConstraintLayout checkLayout;
        ImageView imgPoster;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardView);
            checkLayout = itemView.findViewById(R.id.checkLayout);
            imgPoster = itemView.findViewById(R.id.imgPoster);

            setIsRecyclable(false);
        }
    }

    private AdapterListener adapterListener;

    public interface AdapterListener {
        void onSelect(ExpoExhibitor expoExhibitor);
        void onUnselect(ExpoExhibitor expoExhibitor);
    }

    public void setAdapterListener(AdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }
}
