package com.example.wsapandroidapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.wsapandroidapp.Classes.DateTime;
import com.example.wsapandroidapp.DataModel.Expo;
import com.example.wsapandroidapp.ExpoDetailsActivity;
import com.example.wsapandroidapp.ImageActivity;
import com.example.wsapandroidapp.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ExpoAdapter extends RecyclerView.Adapter<ExpoAdapter.ViewHolder> {

    private final List<Expo> expos;

    private final LayoutInflater layoutInflater;

    private final Context context;

    public ExpoAdapter(Context context, List<Expo> expos) {
        this.expos = expos;
        this.layoutInflater = LayoutInflater.from(context);

        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.custom_expo_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TextView tvExpoPostTitle = holder.tvExpoPostTitle,
                tvDescription = holder.tvDescription,
                tvSchedule = holder.tvSchedule,
                tvMoreInfo = holder.tvMoreInfo;
        ImageView imgPoster = holder.imgPoster;

        Expo expo = expos.get(position);
        DateTime startDateTime = new DateTime(expo.getDateTimeRange().getStartDateTime());
        DateTime endDateTime = new DateTime(expo.getDateTimeRange().getEndDateTime());

        String month = startDateTime.getMonthShortText().toUpperCase();

        tvExpoPostTitle.setText(expo.getExpo());
        tvDescription.setText(expo.getDescription());
        tvSchedule.setText(context.getString(R.string.expo_post_schedule, month,
                Integer.parseInt(startDateTime.getDay()), Integer.parseInt(endDateTime.getDay()),
                Integer.parseInt(startDateTime.getYear())));

        Glide.with(context).load(expo.getImage()).centerCrop().placeholder(R.drawable.ic_wsap).
                error(R.drawable.ic_wsap).into(imgPoster);

        imgPoster.setOnClickListener(view -> {
            Intent intent = new Intent(context, ImageActivity.class);
            intent.putExtra("image", expo.getImage());
            context.startActivity(intent);
        });

        tvMoreInfo.setOnClickListener(view -> {
            Intent intent = new Intent(context, ExpoDetailsActivity.class);
            intent.putExtra("expoId", expo.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return expos.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvExpoPostTitle, tvDescription, tvSchedule, tvMoreInfo;
        ImageView imgPoster;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvExpoPostTitle = itemView.findViewById(R.id.tvExpoPostTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvSchedule = itemView.findViewById(R.id.tvSchedule);
            tvMoreInfo = itemView.findViewById(R.id.tvMoreInfo);
            imgPoster = itemView.findViewById(R.id.imgPoster);

            setIsRecyclable(false);
        }
    }
}
