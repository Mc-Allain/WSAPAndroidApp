package com.example.wsapandroidapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.wsapandroidapp.DataModel.Exhibitor;
import com.example.wsapandroidapp.ImageActivity;
import com.example.wsapandroidapp.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AdminExhibitorAdapter extends RecyclerView.Adapter<AdminExhibitorAdapter.ViewHolder> {

    private final List<Exhibitor> exhibitors;

    private final LayoutInflater layoutInflater;

    private final Context context;

    public AdminExhibitorAdapter(Context context, List<Exhibitor> exhibitors) {
        this.exhibitors = exhibitors;
        this.layoutInflater = LayoutInflater.from(context);

        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.custom_admin_exhibitor_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TextView tvExhibitor = holder.tvExhibitor,
                tvDescription = holder.tvDescription;
        ImageView imgPoster = holder.imgPoster,
                imgMoreInfo = holder.imgMoreInfo,
                imgUpdate = holder.imgUpdate,
                imgDelete = holder.imgDelete,
                imgMove = holder.imgMove;

        Exhibitor exhibitor = exhibitors.get(position);

        tvExhibitor.setText(exhibitor.getExhibitor());
        tvDescription.setText(exhibitor.getDescription());

        Glide.with(context).load(exhibitor.getImage()).centerCrop().placeholder(R.drawable.ic_wsap).
                error(R.drawable.ic_wsap).into(imgPoster);

        imgPoster.setOnClickListener(view -> {
            Intent intent = new Intent(context, ImageActivity.class);
            intent.putExtra("image", exhibitor.getImage());
            context.startActivity(intent);
        });

        imgMoreInfo.setOnClickListener(view -> {
            if (adapterListener != null) adapterListener.onClick(exhibitor);
        });

        imgUpdate.setOnClickListener(view -> {
            if (adapterListener != null) adapterListener.onEdit(exhibitor);
        });

        imgDelete.setOnClickListener(view -> {
            if (adapterListener != null) adapterListener.onDelete(exhibitor);
        });

        imgMove.setOnClickListener(view -> {
            if (adapterListener != null) adapterListener.onMove(exhibitor);
        });
    }

    @Override
    public int getItemCount() {
        return exhibitors.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvExhibitor, tvDescription;
        ImageView imgPoster, imgMoreInfo, imgUpdate, imgDelete, imgMove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvExhibitor = itemView.findViewById(R.id.tvExhibitor);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            imgMoreInfo = itemView.findViewById(R.id.imgMoreInfo);
            imgUpdate = itemView.findViewById(R.id.imgUpdate);
            imgDelete = itemView.findViewById(R.id.imgDelete);
            imgMove = itemView.findViewById(R.id.imgMove);
            imgPoster = itemView.findViewById(R.id.imgPoster);

            setIsRecyclable(false);
        }
    }

    private AdapterListener adapterListener;

    public interface AdapterListener {
        void onClick(Exhibitor exhibitor);
        void onEdit(Exhibitor exhibitor);
        void onDelete(Exhibitor exhibitor);
        void onMove(Exhibitor exhibitor);
    }

    public void setAdapterListener(AdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }
}
