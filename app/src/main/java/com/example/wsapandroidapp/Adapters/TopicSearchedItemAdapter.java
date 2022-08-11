package com.example.wsapandroidapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.wsapandroidapp.DataModel.Topic;
import com.example.wsapandroidapp.DataModel.CategoryImage;
import com.example.wsapandroidapp.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class TopicSearchedItemAdapter extends RecyclerView.Adapter<TopicSearchedItemAdapter.ViewHolder> {

    private final List<Topic> topics;
    private final List<CategoryImage> topicCategories;

    private final LayoutInflater layoutInflater;

    public TopicSearchedItemAdapter(Context context, List<Topic> topics, List<CategoryImage> topicCategories) {
        this.topics = topics;
        this.topicCategories = topicCategories;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.custom_topic_searched_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ConstraintLayout backgroundLayout = holder.backgroundLayout;
        TextView tvTopic = holder.tvTopic,
                tvDescription = holder.tvDescription,
                tvCategory = holder.tvCategory;

        Topic topic = topics.get(position);

        CategoryImage topicCategory = topicCategories.get(0);
        for (CategoryImage topicCategory1 : topicCategories)
            if (topic.getCategory().equals(topicCategory1.getId()))
                topicCategory = topicCategory1;

        tvTopic.setText(topic.getTopic());
        tvDescription.setText(topic.getDescription());
        tvCategory.setText(topicCategory.getCategory());

        backgroundLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return topics.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ConstraintLayout backgroundLayout;
        TextView tvTopic, tvDescription, tvCategory;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            backgroundLayout = itemView.findViewById(R.id.backgroundLayout);
            tvTopic = itemView.findViewById(R.id.tvTopic);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvCategory = itemView.findViewById(R.id.tvCategory);

            setIsRecyclable(false);
        }
    }
}
