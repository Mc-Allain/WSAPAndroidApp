package com.example.wsapandroidapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.wsapandroidapp.DataModel.CategoryImage;
import com.example.wsapandroidapp.DataModel.Chapter;
import com.example.wsapandroidapp.DataModel.Supplier;
import com.example.wsapandroidapp.ImageActivity;
import com.example.wsapandroidapp.R;
import com.example.wsapandroidapp.SupplierDetailsActivity;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SupplierAdapter extends RecyclerView.Adapter<SupplierAdapter.ViewHolder> {

    private final List<Supplier> suppliers;
    private final List<Chapter> chapters;

    private final LayoutInflater layoutInflater;

    private final Context context;

    public SupplierAdapter(Context context, List<Supplier> suppliers, List<Chapter> chapters) {
        this.suppliers = suppliers;
        this.chapters = chapters;
        this.layoutInflater = LayoutInflater.from(context);

        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.custom_supplier_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TextView tvSupplier = holder.tvSupplier,
                tvChapter = holder.tvChapter,
                tvDescription = holder.tvDescription,
                tvMoreInfo = holder.tvMoreInfo;
        ImageView imgPoster = holder.imgPoster;

        Supplier supplier = suppliers.get(position);

        Chapter chapter = chapters.get(0);
        for (Chapter chapter1 : chapters)
            if (supplier.getChapter().equals(chapter1.getId()))
                chapter = chapter1;

        tvSupplier.setText(supplier.getSupplier());
        tvChapter.setText(chapter.getChapter());
        tvDescription.setText(supplier.getDescription());

        Glide.with(context).load(supplier.getImage()).centerCrop().placeholder(R.drawable.ic_wsap).
                error(R.drawable.ic_wsap).into(imgPoster);

        imgPoster.setOnClickListener(view -> {
            Intent intent = new Intent(context, ImageActivity.class);
            intent.putExtra("image", supplier.getImage());
            context.startActivity(intent);
        });

        tvMoreInfo.setOnClickListener(view -> {
            if (adapterListener != null) adapterListener.onClick(supplier);
        });
    }

    @Override
    public int getItemCount() {
        return suppliers.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvSupplier, tvChapter, tvDescription, tvMoreInfo;
        ImageView imgPoster;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvSupplier = itemView.findViewById(R.id.tvSupplier);
            tvChapter = itemView.findViewById(R.id.tvChapter);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvMoreInfo = itemView.findViewById(R.id.tvMoreInfo);
            imgPoster = itemView.findViewById(R.id.imgPoster);

            setIsRecyclable(false);
        }
    }

    private AdapterListener adapterListener;

    public interface AdapterListener {
        void onClick(Supplier supplier);
    }

    public void setAdapterListener(AdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }
}
