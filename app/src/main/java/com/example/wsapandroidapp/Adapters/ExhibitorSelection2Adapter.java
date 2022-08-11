package com.example.wsapandroidapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.wsapandroidapp.DataModel.Exhibitor;
import com.example.wsapandroidapp.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class ExhibitorSelection2Adapter extends RecyclerView.Adapter {

    private final List<Exhibitor> exhibitors;
    private final List<Exhibitor> selectedExhibitors;

    private final  LayoutInflater layoutInflater;

    private final Context context;

    public ExhibitorSelection2Adapter(Context context, List<Exhibitor> exhibitors,
                                      List<Exhibitor> selectedExhibitors) {
        this.exhibitors = exhibitors;
        this.selectedExhibitors = selectedExhibitors;
        this.layoutInflater = LayoutInflater.from(context);

        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (getItemViewType(viewType) == 0) {
            View view = layoutInflater.inflate(R.layout.custom_add_icon_layout, parent, false);
            return new AddIconHolder(view);
        } else {
            View view = layoutInflater.inflate(R.layout.custom_exhibitor_selection_layout, parent, false);
            return new ExhibitorHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == 0) {
            AddIconHolder addIconHolder = (AddIconHolder) holder;
            CardView cardView = addIconHolder.cardView;

            cardView.setOnClickListener(view -> {
                if (adapterListener != null) adapterListener.onAdd();
            });
        } else {
            ExhibitorHolder exhibitorHolder = (ExhibitorHolder) holder;

            CardView cardView = exhibitorHolder.cardView;
            ConstraintLayout checkLayout = exhibitorHolder.checkLayout;
            ImageView imgPoster = exhibitorHolder.imgPoster;

            Exhibitor exhibitor = exhibitors.get(position - 1);

            boolean isSelected = false;
            for (Exhibitor selectedExhibitor : selectedExhibitors)
                if (exhibitor.getId().equals(selectedExhibitor.getId())) {
                    isSelected = true;
                    break;
                }

            Glide.with(context).load(exhibitor.getImage()).centerCrop().placeholder(R.drawable.ic_wsap).
                    error(R.drawable.ic_wsap).into(imgPoster);

            checkLayout.setVisibility(View.GONE);
            if (isSelected) checkLayout.setVisibility(View.VISIBLE);

            boolean finalIsSelected = isSelected;
            cardView.setOnClickListener(view -> {
                if (adapterListener != null) {
                    if (finalIsSelected) adapterListener.onUnselect(exhibitor);
                    else adapterListener.onSelect(exhibitor);
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return 0;
        else return 1;
    }

    @Override
    public int getItemCount() {
        return exhibitors.size() + 1;
    }

    public static class ExhibitorHolder extends RecyclerView.ViewHolder {

        CardView cardView;
        ConstraintLayout checkLayout;
        ImageView imgPoster;

        public ExhibitorHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardView);
            checkLayout = itemView.findViewById(R.id.checkLayout);
            imgPoster = itemView.findViewById(R.id.imgPoster);

            setIsRecyclable(false);
        }
    }

    public static class AddIconHolder extends RecyclerView.ViewHolder {

        CardView cardView;

        public AddIconHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardView);

            setIsRecyclable(false);
        }
    }

    private AdapterListener adapterListener;

    public interface AdapterListener {
        void onAdd();
        void onSelect(Exhibitor exhibitor);
        void onUnselect(Exhibitor exhibitor);
    }

    public void setAdapterListener(AdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }
}
