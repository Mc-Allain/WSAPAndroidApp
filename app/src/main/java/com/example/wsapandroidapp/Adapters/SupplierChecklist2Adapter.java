package com.example.wsapandroidapp.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.wsapandroidapp.DataModel.UserSupplierChecklist;
import com.example.wsapandroidapp.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class SupplierChecklist2Adapter extends RecyclerView.Adapter<SupplierChecklist2Adapter.ViewHolder> {

    private final List<UserSupplierChecklist> supplierChecklist;

    private final  LayoutInflater layoutInflater;

    private final Context context;

    private int selectedPosition = -1;

    public SupplierChecklist2Adapter(Context context, List<UserSupplierChecklist> supplierChecklist) {
        this.supplierChecklist = supplierChecklist;
        this.layoutInflater = LayoutInflater.from(context);

        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.custom_supplier_checklist_layout, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CardView cardView = holder.cardView,
                cardView1 = holder.cardView1;
        ConstraintLayout backgroundLayout = holder.backgroundLayout,
                backgroundLayout1 = holder.backgroundLayout1,
                constraintLayout = holder.constraintLayout;
        TextView tvTask = holder.tvTask;
        ImageView imgArrow = holder.imgArrow;
        EditText etCompany = holder.etCompany,
                etContactPerson = holder.etContactPerson,
                etPhoneNumber = holder.etPhoneNumber,
                etEmailAddress = holder.etEmailAddress;
        TextView tvCompanyError = holder.tvCompanyError,
                tvContactPersonError = holder.tvContactPersonError,
                tvPhoneNumberError = holder.tvPhoneNumberError,
                tvEmailAddressError = holder.tvEmailAddressError;

        UserSupplierChecklist supplierChecklist1 = supplierChecklist.get(position);

        tvTask.setText(supplierChecklist1.getTask());

        etCompany.setText(supplierChecklist1.getEmailAddress());
        etContactPerson.setText(supplierChecklist1.getEmailAddress());

        String contactNumber = "";
        if (String.valueOf(supplierChecklist1.getContactNumber()).length() > 0 &&
                supplierChecklist1.getContactNumber() != 0)
            contactNumber = String.valueOf(supplierChecklist1.getContactNumber());

        etPhoneNumber.setText(contactNumber);
        etEmailAddress.setText(supplierChecklist1.getEmailAddress());

        backgroundLayout1.setBackgroundColor(context.getColor(R.color.white));
        tvTask.setTextColor(context.getColor(R.color.primary));
        imgArrow.setImageResource(R.drawable.ic_baseline_arrow_drop_down_24);
        imgArrow.setImageTintList(ColorStateList.valueOf(context.getColor(R.color.primary)));
        constraintLayout.setVisibility(View.GONE);

        if (position == selectedPosition) {
            backgroundLayout1.setBackgroundColor(context.getColor(R.color.primary));
            tvTask.setTextColor(context.getColor(R.color.white));
            imgArrow.setImageResource(R.drawable.ic_baseline_arrow_drop_up_24);
            imgArrow.setImageTintList(ColorStateList.valueOf(context.getColor(R.color.white)));
            constraintLayout.setVisibility(View.VISIBLE);
        }

        cardView1.setOnClickListener(view -> {
            if (selectedPosition == holder.getAdapterPosition())
                selectedPosition = -1;
            else selectedPosition = holder.getAdapterPosition();
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return supplierChecklist.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        CardView cardView, cardView1;
        ConstraintLayout backgroundLayout, backgroundLayout1, constraintLayout;
        TextView tvTask;
        ImageView imgArrow;
        EditText etCompany, etContactPerson, etPhoneNumber, etEmailAddress;
        TextView tvCompanyError, tvContactPersonError, tvPhoneNumberError, tvEmailAddressError;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardView);
            cardView1 = itemView.findViewById(R.id.cardView1);
            backgroundLayout = itemView.findViewById(R.id.backgroundLayout);
            backgroundLayout1 = itemView.findViewById(R.id.backgroundLayout1);
            constraintLayout = itemView.findViewById(R.id.constraintLayout);
            tvTask = itemView.findViewById(R.id.tvTask);
            imgArrow = itemView.findViewById(R.id.imgArrow);

            etCompany = itemView.findViewById(R.id.etCompany);
            etContactPerson = itemView.findViewById(R.id.etContactPerson);
            etPhoneNumber = itemView.findViewById(R.id.etPhoneNumber);
            etEmailAddress = itemView.findViewById(R.id.etEmailAddress);

            tvCompanyError = itemView.findViewById(R.id.tvCompanyError);
            tvContactPersonError = itemView.findViewById(R.id.tvContactPersonError);
            tvPhoneNumberError = itemView.findViewById(R.id.tvPhoneNumberError);
            tvEmailAddressError = itemView.findViewById(R.id.tvEmailAddressError);

            setIsRecyclable(false);
        }
    }

    private AdapterListener adapterListener;

    public interface AdapterListener {
        void onClick();
    }

    public void setAdapterListener(AdapterListener adapterListener) {
        this.adapterListener = adapterListener;
    }
}
