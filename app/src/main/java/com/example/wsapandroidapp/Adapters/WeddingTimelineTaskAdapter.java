package com.example.wsapandroidapp.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wsapandroidapp.DataModel.UserWeddingTimelineTask;
import com.example.wsapandroidapp.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class WeddingTimelineTaskAdapter extends RecyclerView.Adapter<WeddingTimelineTaskAdapter.ViewHolder> {

    private final List<UserWeddingTimelineTask> tasks;

    private final  LayoutInflater layoutInflater;

    private final Context context;

    public WeddingTimelineTaskAdapter(Context context, List<UserWeddingTimelineTask> tasks) {
        this.tasks = tasks;
        this.layoutInflater = LayoutInflater.from(context);

        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.custom_wedding_timeline_task_layout, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CardView cardView = holder.cardView;
        ConstraintLayout backgroundLayout = holder.backgroundLayout;
        TextView tvLabel = holder.tvLabel;
        ImageView imgStatus = holder.imgStatus;

        UserWeddingTimelineTask task = tasks.get(position);

        tvLabel.setText(task.getTask());

        boolean isCompleted = task.isCompleted();

        backgroundLayout.setBackgroundColor(context.getColor(R.color.white));
        tvLabel.setTextColor(context.getColor(R.color.primary));
        imgStatus.setImageTintList(ColorStateList.valueOf(context.getColor(R.color.gray)));
        imgStatus.setImageResource(R.drawable.ic_baseline_watch_later_24);

        if (isCompleted) {
            backgroundLayout.setBackgroundColor(context.getColor(R.color.primary));
            tvLabel.setTextColor(context.getColor(R.color.white));
            imgStatus.setImageTintList(ColorStateList.valueOf(context.getColor(R.color.white)));
            imgStatus.setImageResource(R.drawable.ic_baseline_check_circle_24);
        }

        cardView.setOnClickListener(view -> {
            if (adapterListener != null) adapterListener.onClick(task);
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        CardView cardView;
        ConstraintLayout backgroundLayout;
        TextView tvLabel;
        ImageView imgStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardView);
            backgroundLayout = itemView.findViewById(R.id.backgroundLayout);
            tvLabel = itemView.findViewById(R.id.tvLabel);
            imgStatus = itemView.findViewById(R.id.imgStatus);

            setIsRecyclable(false);
        }
    }

    private AdapterListener adapterListener;

    public interface AdapterListener {
        void onClick(UserWeddingTimelineTask task);
    }

    public void setAdapterListener(AdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }
}
