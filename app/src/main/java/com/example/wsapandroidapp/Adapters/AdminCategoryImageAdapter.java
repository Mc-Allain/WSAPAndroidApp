package com.example.wsapandroidapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.wsapandroidapp.DataModel.CategoryImage;
import com.example.wsapandroidapp.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class AdminCategoryImageAdapter extends RecyclerView.Adapter<AdminCategoryImageAdapter.ViewHolder> {

    private final List<CategoryImage> categoryImages;

    private final  LayoutInflater layoutInflater;

    private final Context context;

    public AdminCategoryImageAdapter(Context context, List<CategoryImage> categoryImages) {
        this.categoryImages = categoryImages;
        this.layoutInflater = LayoutInflater.from(context);

        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.custom_admin_category_image_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CardView cardView = holder.cardView;
        ImageView imgPoster = holder.imgPoster,
                imgUpdate = holder.imgUpdate,
                imgDelete = holder.imgDelete;
        TextView tvCategory = holder.tvCategory;

        CategoryImage categoryImage = categoryImages.get(position);

        Glide.with(context).load(categoryImage.getImage()).centerCrop().placeholder(R.drawable.ic_wsap).
                error(R.drawable.ic_wsap).into(imgPoster);

        tvCategory.setText(categoryImage.getCategory());

        cardView.setOnClickListener(view -> {
            if (adapterListener != null) adapterListener.onClick(categoryImage);
        });

        imgUpdate.setOnClickListener(view -> {
            if (adapterListener != null) adapterListener.update(categoryImage);
        });

        imgDelete.setOnClickListener(view -> {
            if (adapterListener != null) adapterListener.delete(categoryImage);
        });
    }

    @Override
    public int getItemCount() {
        return categoryImages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        CardView cardView;
        ImageView imgPoster, imgUpdate, imgDelete;
        TextView tvCategory;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardView);
            imgPoster = itemView.findViewById(R.id.imgPoster);
            imgUpdate = itemView.findViewById(R.id.imgUpdate);
            imgDelete = itemView.findViewById(R.id.imgDelete);
            tvCategory = itemView.findViewById(R.id.tvCategory);

            setIsRecyclable(false);
        }
    }

    private AdapterListener adapterListener;

    public interface AdapterListener {
        void onClick(CategoryImage categoryImage);
        void update(CategoryImage categoryImage);
        void delete(CategoryImage categoryImage);
    }

    public void setAdapterListener(AdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }
}
