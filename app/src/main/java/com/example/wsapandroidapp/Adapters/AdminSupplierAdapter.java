package com.example.wsapandroidapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.wsapandroidapp.DataModel.Chapter;
import com.example.wsapandroidapp.DataModel.Supplier;
import com.example.wsapandroidapp.ImageActivity;
import com.example.wsapandroidapp.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AdminSupplierAdapter extends RecyclerView.Adapter<AdminSupplierAdapter.ViewHolder> {

    private final List<Supplier> suppliers;
    private final List<Chapter> chapters;

    private final LayoutInflater layoutInflater;

    private final Context context;

    public AdminSupplierAdapter(Context context, List<Supplier> suppliers, List<Chapter> chapters) {
        this.suppliers = suppliers;
        this.chapters = chapters;
        this.layoutInflater = LayoutInflater.from(context);

        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.custom_admin_supplier_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TextView tvSupplier = holder.tvSupplier,
                tvChapter = holder.tvChapter,
                tvDescription = holder.tvDescription;
        ImageView imgPoster = holder.imgPoster,
                imgMoreInfo = holder.imgMoreInfo,
                imgUpdate = holder.imgUpdate,
                imgDelete = holder.imgDelete,
                imgMove = holder.imgMove;

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

        imgMoreInfo.setOnClickListener(view -> {
            if (adapterListener != null) adapterListener.onClick(supplier);
        });

        Chapter finalChapter = chapter;
        imgUpdate.setOnClickListener(view -> {
            if (adapterListener != null) adapterListener.onEdit(supplier, finalChapter);
        });

        imgDelete.setOnClickListener(view -> {
            if (adapterListener != null) adapterListener.onDelete(supplier);
        });

        imgMove.setOnClickListener(view -> {
            if (adapterListener != null) adapterListener.onMove(supplier);
        });
    }

    @Override
    public int getItemCount() {
        return suppliers.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvSupplier, tvChapter, tvDescription;
        ImageView imgPoster, imgMoreInfo, imgUpdate, imgDelete, imgMove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvSupplier = itemView.findViewById(R.id.tvSupplier);
            tvChapter = itemView.findViewById(R.id.tvChapter);
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
        void onClick(Supplier supplier);
        void onEdit(Supplier supplier, Chapter chapter);
        void onDelete(Supplier supplier);
        void onMove(Supplier supplier);
    }

    public void setAdapterListener(AdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }
}
