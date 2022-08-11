package com.example.wsapandroidapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.wsapandroidapp.AdminExhibitorDetailsActivity;
import com.example.wsapandroidapp.DataModel.Exhibitor;
import com.example.wsapandroidapp.DataModel.Supplier;
import com.example.wsapandroidapp.ImageActivity;
import com.example.wsapandroidapp.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ExhibitorAdapter extends RecyclerView.Adapter<ExhibitorAdapter.ViewHolder> {

    private final List<Exhibitor> exhibitors;

    private final LayoutInflater layoutInflater;

    private final Context context;

    public ExhibitorAdapter(Context context, List<Exhibitor> exhibitors) {
        this.exhibitors = exhibitors;
        this.layoutInflater = LayoutInflater.from(context);

        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.custom_exhibitor_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TextView tvSupplier = holder.tvSupplier,
                tvDescription = holder.tvDescription,
                tvMoreInfo = holder.tvMoreInfo;
        ImageView imgPoster = holder.imgPoster;

        Exhibitor exhibitor = exhibitors.get(position);

        tvSupplier.setText(exhibitor.getExhibitor());
        tvDescription.setText(exhibitor.getDescription());

        Glide.with(context).load(exhibitor.getImage()).centerCrop().placeholder(R.drawable.ic_wsap).
                error(R.drawable.ic_wsap).into(imgPoster);

        imgPoster.setOnClickListener(view -> {
            Intent intent = new Intent(context, ImageActivity.class);
            intent.putExtra("image", exhibitor.getImage());
            context.startActivity(intent);
        });

        tvMoreInfo.setOnClickListener(view -> {
            if (adapterListener != null) adapterListener.onClick(exhibitor);
        });
    }

    @Override
    public int getItemCount() {
        return exhibitors.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvSupplier, tvDescription, tvMoreInfo;
        ImageView imgPoster;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvSupplier = itemView.findViewById(R.id.tvSupplier);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvMoreInfo = itemView.findViewById(R.id.tvMoreInfo);
            imgPoster = itemView.findViewById(R.id.imgPoster);

            setIsRecyclable(false);
        }
    }

    private AdapterListener adapterListener;

    public interface AdapterListener {
        void onClick(Exhibitor exhibitor);
    }

    public void setAdapterListener(AdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }
}
