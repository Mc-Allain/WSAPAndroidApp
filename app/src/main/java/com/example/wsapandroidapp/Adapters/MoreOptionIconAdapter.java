package com.example.wsapandroidapp.Adapters;

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

public class MoreOptionIconAdapter extends RecyclerView.Adapter<MoreOptionIconAdapter.ViewHolder> {

    private final List<MoreOptionIcon> moreOptionIconList;

    private final  LayoutInflater layoutInflater;

    private final Context context;

    public MoreOptionIconAdapter(Context context, List<MoreOptionIcon> moreOptionIconList) {
        this.moreOptionIconList = moreOptionIconList;
        this.layoutInflater = LayoutInflater.from(context);

        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.custom_more_option_icon_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CardView cardView = holder.cardView;
        ConstraintLayout backgroundLayout = holder.backgroundLayout;
        ImageView imgIcon = holder.imgIcon;
        TextView tvOption = holder.tvOption;

        MoreOptionIcon moreOptionIcon = moreOptionIconList.get(position);

        Glide.with(context).load(moreOptionIcon.getIcon()).centerCrop().placeholder(R.drawable.ic_wsap).
                error(R.drawable.ic_wsap).into(imgIcon);

        tvOption.setText(moreOptionIcon.getOption());

        if (moreOptionIcon.getOption().equals(context.getString(R.string.sign_out)))
            backgroundLayout.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.bg_red_button_ripple, null));

        cardView.setOnClickListener(view -> {
            if (adapterListener != null) adapterListener.onClick(moreOptionIcon);
        });
    }

    @Override
    public int getItemCount() {
        return moreOptionIconList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        CardView cardView;
        ConstraintLayout backgroundLayout;
        ImageView imgIcon;
        TextView tvOption;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardView);
            backgroundLayout = itemView.findViewById(R.id.backgroundLayout);
            imgIcon = itemView.findViewById(R.id.imgIcon);
            tvOption = itemView.findViewById(R.id.tvOption);

            setIsRecyclable(false);
        }
    }

    private AdapterListener adapterListener;

    public interface AdapterListener {
        void onClick(MoreOptionIcon moreOptionIcon);
    }

    public void setAdapterListener(AdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }
}
