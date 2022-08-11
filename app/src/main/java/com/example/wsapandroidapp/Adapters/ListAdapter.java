package com.example.wsapandroidapp.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.wsapandroidapp.DataModel.MoreOptionIcon;
import com.example.wsapandroidapp.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

    private final List<String> list;

    private final  LayoutInflater layoutInflater;

    private final Context context;

    private int selectedPosition = -1;

    public ListAdapter(Context context, List<String> list) {
        this.list = list;
        this.layoutInflater = LayoutInflater.from(context);

        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.custom_list_layout, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CardView cardView = holder.cardView;
        ConstraintLayout backgroundLayout = holder.backgroundLayout;
        TextView tvLabel = holder.tvLabel;

        String string = list.get(position);

        tvLabel.setText(string);

        backgroundLayout.setBackgroundColor(context.getColor(R.color.white));
        tvLabel.setTextColor(context.getColor(R.color.primary));

        if (position == selectedPosition) {
            backgroundLayout.setBackgroundColor(context.getColor(R.color.primary));
            tvLabel.setTextColor(context.getColor(R.color.white));
        }

        cardView.setOnClickListener(view -> {
            selectedPosition = holder.getAdapterPosition();
            if (adapterListener != null) adapterListener.onClick(selectedPosition);
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        CardView cardView;
        ConstraintLayout backgroundLayout;
        TextView tvLabel;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardView);
            backgroundLayout = itemView.findViewById(R.id.backgroundLayout);
            tvLabel = itemView.findViewById(R.id.tvLabel);

            setIsRecyclable(false);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
        notifyDataSetChanged();
    }

    private AdapterListener adapterListener;

    public interface AdapterListener {
        void onClick(int position);
    }

    public void setAdapterListener(AdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }
}
