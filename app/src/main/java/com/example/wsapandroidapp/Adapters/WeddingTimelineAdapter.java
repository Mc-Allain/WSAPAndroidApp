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

import com.example.wsapandroidapp.DataModel.UserWeddingTimeline;
import com.example.wsapandroidapp.DataModel.UserWeddingTimelineTask;
import com.example.wsapandroidapp.DialogClasses.ConfirmationDialog;
import com.example.wsapandroidapp.R;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class WeddingTimelineAdapter extends RecyclerView.Adapter<WeddingTimelineAdapter.ViewHolder> {

    private final List<UserWeddingTimeline> weddingTimeline;

    private final  LayoutInflater layoutInflater;

    private final Context context;

    private final ConfirmationDialog confirmationDialog;

    private int selectedPosition = -1;

    private long duration = 0;

    public WeddingTimelineAdapter(Context context, List<UserWeddingTimeline> weddingTimeline) {
        this.weddingTimeline = weddingTimeline;
        this.layoutInflater = LayoutInflater.from(context);

        this.context = context;

        confirmationDialog = new ConfirmationDialog(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.custom_wedding_timeline_layout, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CardView cardView1 = holder.cardView1;
        ConstraintLayout backgroundLayout1 = holder.backgroundLayout1;
        TextView tvTitle = holder.tvTitle,
                tvStatus = holder.tvStatus;
        ImageView imgStatus = holder.imgStatus,
                imgArrow = holder.imgArrow;
        RecyclerView recyclerView = holder.recyclerView;

        UserWeddingTimeline weddingTimeline1 = weddingTimeline.get(position);

        tvTitle.setText(weddingTimeline1.getTitle());

        List<UserWeddingTimelineTask> userWeddingTimelineTasks =
                new ArrayList<>(weddingTimeline1.getTasks().values());

        userWeddingTimelineTasks.sort(Comparator.comparingInt(UserWeddingTimelineTask::getTaskNo));

        int completed = 0, total = 0;
        for (UserWeddingTimelineTask userWeddingTimelineTask : userWeddingTimelineTasks) {
            total += 1;
            if (userWeddingTimelineTask.isCompleted()) completed += 1;
        }

        backgroundLayout1.setBackgroundColor(context.getColor(R.color.white));
        tvTitle.setTextColor(context.getColor(R.color.primary));
        tvStatus.setTextColor(context.getColor(R.color.gray));
        imgStatus.setImageResource(R.drawable.ic_baseline_watch_later_24);
        imgStatus.setImageTintList(ColorStateList.valueOf(context.getColor(R.color.gray)));
        imgArrow.setImageResource(R.drawable.ic_baseline_arrow_drop_down_24);
        imgArrow.setImageTintList(ColorStateList.valueOf(context.getColor(R.color.primary)));
        recyclerView.setVisibility(View.GONE);

        if (duration < weddingTimeline1.getDuration()) {
            backgroundLayout1.setBackgroundColor(context.getColor(R.color.light_gray));
            tvTitle.setTextColor(context.getColor(R.color.black));
            imgStatus.setImageResource(R.drawable.ic_baseline_clear_24);
            imgStatus.setImageTintList(ColorStateList.valueOf(context.getColor(R.color.white)));
            imgArrow.setImageTintList(ColorStateList.valueOf(context.getColor(R.color.white)));
        }

        if (completed == total) {
            backgroundLayout1.setBackgroundColor(context.getColor(R.color.white));
            tvTitle.setTextColor(context.getColor(R.color.primary));
            imgStatus.setImageResource(R.drawable.ic_baseline_check_circle_24);
            imgStatus.setImageTintList(ColorStateList.valueOf(context.getColor(R.color.green)));
            imgArrow.setImageTintList(ColorStateList.valueOf(context.getColor(R.color.primary)));
        }

        if (position == selectedPosition) {
            backgroundLayout1.setBackgroundColor(context.getColor(R.color.primary));
            tvTitle.setTextColor(context.getColor(R.color.white));
            tvStatus.setTextColor(context.getColor(R.color.white));
            imgStatus.setImageTintList(ColorStateList.valueOf(context.getColor(R.color.white)));
            imgArrow.setImageResource(R.drawable.ic_baseline_arrow_drop_up_24);
            imgArrow.setImageTintList(ColorStateList.valueOf(context.getColor(R.color.white)));
            recyclerView.setVisibility(View.VISIBLE);
        }

        tvStatus.setText(context.getString(R.string.wedding_timeline_completed_task, completed, total));

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        WeddingTimelineTaskAdapter weddingTimelineTaskAdapter = new WeddingTimelineTaskAdapter(context, userWeddingTimelineTasks, duration < weddingTimeline1.getDuration());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(weddingTimelineTaskAdapter);

        weddingTimelineTaskAdapter.setAdapterListener(task -> {
            Map<String, UserWeddingTimelineTask> userWeddingTimelineTaskMap = new HashMap<>();

            final boolean isCompleted = !task.isCompleted();

            if (isCompleted) {
                for (UserWeddingTimelineTask userWeddingTimelineTask : userWeddingTimelineTasks) {
                    if (userWeddingTimelineTask.getId().equals(task.getId()))
                        userWeddingTimelineTask.setCompleted(true);

                    userWeddingTimelineTaskMap.put(userWeddingTimelineTask.getId(), userWeddingTimelineTask);
                }

                weddingTimeline1.setTasks(userWeddingTimelineTaskMap);

                if (adapterListener != null) adapterListener.onClick(weddingTimeline1);

                weddingTimelineTaskAdapter.notifyDataSetChanged();
            } else {
                confirmationDialog.setMessage(context.getString(R.string.confirmation_prompt, "set the task as pending"));
                confirmationDialog.showDialog();

                confirmationDialog.setDialogListener(() -> {
                    for (UserWeddingTimelineTask userWeddingTimelineTask : userWeddingTimelineTasks) {
                        if (userWeddingTimelineTask.getId().equals(task.getId()))
                            userWeddingTimelineTask.setCompleted(false);

                        userWeddingTimelineTaskMap.put(userWeddingTimelineTask.getId(), userWeddingTimelineTask);
                    }

                    weddingTimeline1.setTasks(userWeddingTimelineTaskMap);

                    if (adapterListener != null) adapterListener.onClick(weddingTimeline1);

                    weddingTimelineTaskAdapter.notifyDataSetChanged();

                    confirmationDialog.dismissDialog();
                });
            }
        });

        cardView1.setOnClickListener(view -> {
            if (selectedPosition == holder.getAdapterPosition())
                selectedPosition = -1;
            else selectedPosition = holder.getAdapterPosition();
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return weddingTimeline.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        CardView cardView1;
        ConstraintLayout backgroundLayout1;
        TextView tvTitle, tvStatus;
        ImageView imgStatus, imgArrow;
        RecyclerView recyclerView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView1 = itemView.findViewById(R.id.cardView1);
            backgroundLayout1 = itemView.findViewById(R.id.backgroundLayout1);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            imgStatus = itemView.findViewById(R.id.imgStatus);
            imgArrow = itemView.findViewById(R.id.imgArrow);
            recyclerView = itemView.findViewById(R.id.recyclerView);

            setIsRecyclable(false);
        }
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    private AdapterListener adapterListener;

    public interface AdapterListener {
        void onClick(UserWeddingTimeline weddingTimeline);
    }

    public void setAdapterListener(AdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }
}
