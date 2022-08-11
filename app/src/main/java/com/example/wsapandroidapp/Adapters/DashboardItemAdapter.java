package com.example.wsapandroidapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.wsapandroidapp.DataModel.DashboardItem;
import com.example.wsapandroidapp.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class DashboardItemAdapter extends RecyclerView.Adapter<DashboardItemAdapter.ViewHolder> {

    private final List<DashboardItem> dashboardItemList;
    private final List<Integer> recordCounts;

    private final  LayoutInflater layoutInflater;

    private final Context context;

    public DashboardItemAdapter(Context context, List<DashboardItem> dashboardItemList, List<Integer> recordCounts) {
        this.dashboardItemList = dashboardItemList;
        this.recordCounts = recordCounts;
        this.layoutInflater = LayoutInflater.from(context);

        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.custom_dashboard_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CardView cardView = holder.cardView;
        ImageView imgPoster = holder.imgPoster;
        TextView tvItemName = holder.tvItemName,
                tvCount = holder.tvCount;

        DashboardItem dashboardItem = dashboardItemList.get(position);
        int recordCount = recordCounts.size() > 0 ? recordCounts.get(position) : 0;

        Glide.with(context).load(dashboardItem.getImage()).centerCrop().placeholder(R.drawable.ic_wsap).
                error(R.drawable.ic_wsap).into(imgPoster);

        tvItemName.setText(dashboardItem.getDashboard());
        tvCount.setText(String.valueOf(recordCount));

        cardView.setOnClickListener(view -> {
            if (adapterListener != null) adapterListener.onClick(dashboardItem);
        });
    }

    @Override
    public int getItemCount() {
        return dashboardItemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        CardView cardView;
        ImageView imgPoster;
        TextView tvItemName, tvCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardView);
            imgPoster = itemView.findViewById(R.id.imgPoster);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvCount = itemView.findViewById(R.id.tvCount);

            setIsRecyclable(false);
        }
    }

    private AdapterListener adapterListener;

    public interface AdapterListener {
        void onClick(DashboardItem dashboardItem);
    }

    public void setAdapterListener(AdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }
}
