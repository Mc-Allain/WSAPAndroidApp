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

public class CategoryImageAdapter extends RecyclerView.Adapter<CategoryImageAdapter.ViewHolder> {

    private final List<CategoryImage> categoryImages;

    private final  LayoutInflater layoutInflater;

    private final Context context;

    public CategoryImageAdapter(Context context, List<CategoryImage> categoryImages) {
        this.categoryImages = categoryImages;
        this.layoutInflater = LayoutInflater.from(context);

        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.custom_category_image_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CardView cardView = holder.cardView;
        ImageView imgPoster = holder.imgPoster;
        TextView tvCategory = holder.tvCategory;

        CategoryImage categoryImage = categoryImages.get(position);

        Glide.with(context).load(categoryImage.getImage()).centerCrop().placeholder(R.drawable.ic_wsap).
                error(R.drawable.ic_wsap).into(imgPoster);

        tvCategory.setText(categoryImage.getCategory());

        cardView.setOnClickListener(view -> {
            if (adapterListener != null) adapterListener.onClick(categoryImage);
        });
    }

    @Override
    public int getItemCount() {
        return categoryImages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        CardView cardView;
        ImageView imgPoster;
        TextView tvCategory;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardView);
            imgPoster = itemView.findViewById(R.id.imgPoster);
            tvCategory = itemView.findViewById(R.id.tvCategory);

            setIsRecyclable(false);
        }
    }

    private AdapterListener adapterListener;

    public interface AdapterListener {
        void onClick(CategoryImage categoryImage);
    }

    public void setAdapterListener(AdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }
}
