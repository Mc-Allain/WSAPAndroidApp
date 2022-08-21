package com.example.wsapandroidapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.wsapandroidapp.Classes.Credentials;
import com.example.wsapandroidapp.DataModel.User;
import com.example.wsapandroidapp.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private final List<User> users;

    private final  LayoutInflater layoutInflater;

    private Context context;

    private int userRoleIndex = 0;

    public UserAdapter(Context context, List<User> users) {
        this.users = users;
        this.layoutInflater = LayoutInflater.from(context);

        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.custom_user_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TextView tvDisplayName = holder.tvDisplayName,
            tvRole = holder.tvRole;
        ImageView imgProfile = holder.imgProfile,
                imgUpdate = holder.imgUpdate;

        User user = users.get(position);
        String role = "Non-Admin";
        int roleIndex = 0;

        if (user.getRole().isAdmin()) {
            role = "Admin";
            roleIndex = 1;
        }
        if (user.getRole().isAdminHead()) {
            role = "Admin Head";
            roleIndex = 2;
        }
        if (user.getRole().isDeveloper()) {
            role = "Developer";
            roleIndex = 3;
        }
        if (user.getRole().isLeadDeveloper()) {
            role = "Lead Developer";
            roleIndex = 4;
        }

        String photoUrl = user.getPhotoUrl();

        if (!Credentials.isEmpty(photoUrl))
            Glide.with(context).load(photoUrl).centerCrop().placeholder(R.drawable.ic_wsap).
                    error(R.drawable.ic_wsap).into(imgProfile);
        else imgProfile.setImageResource(R.drawable.ic_wsap);

        tvDisplayName.setText(user.getDisplayName());
        tvRole.setText(role);

        imgUpdate.setVisibility(View.GONE);
        if (roleIndex < userRoleIndex)
            imgUpdate.setVisibility(View.VISIBLE);

        imgUpdate.setOnClickListener(view -> {
            if (adapterListener != null) adapterListener.onClick(user);
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        CardView cardView;
        TextView tvDisplayName, tvRole;
        ImageView imgProfile, imgUpdate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardView);
            tvDisplayName = itemView.findViewById(R.id.tvDisplayName);
            tvRole = itemView.findViewById(R.id.tvRole);
            imgProfile = itemView.findViewById(R.id.imgProfile);
            imgUpdate = itemView.findViewById(R.id.imgUpdate);

            setIsRecyclable(false);
        }
    }

    public void setUserRoleIndex(int userRoleIndex) {
        this.userRoleIndex = userRoleIndex;
    }

    private AdapterListener adapterListener;

    public interface AdapterListener {
        void onClick(User user);
    }

    public void setAdapterListener(AdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }
}
