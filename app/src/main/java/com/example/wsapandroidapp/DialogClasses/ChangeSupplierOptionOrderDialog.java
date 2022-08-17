package com.example.wsapandroidapp.DialogClasses;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.example.wsapandroidapp.DataModel.SupplierOption;
import com.example.wsapandroidapp.R;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

public class ChangeSupplierOptionOrderDialog {

    private CardView cardView1, cardView2, cardView3;
    private ConstraintLayout constraintLayout1, constraintLayout2, constraintLayout3;
    private TextView tvFirstOption, tvSecondOption, tvThirdOption;

    private final Context context;
    private Dialog dialog;

    private int selectedOrder = 0;
    private SupplierOption option;

    public ChangeSupplierOptionOrderDialog(Context context) {
        this.context = context;

        createDialog();
    }

    private void createDialog() {
        setDialog();
        setDialogWindow();
    }

    private void setDialog() {
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_change_supplier_option_order_layout);

        cardView1 = dialog.findViewById(R.id.cardView1);
        cardView2 = dialog.findViewById(R.id.cardView2);
        cardView3 = dialog.findViewById(R.id.cardView3);
        constraintLayout1 = dialog.findViewById(R.id.constraintLayout1);
        constraintLayout2 = dialog.findViewById(R.id.constraintLayout2);
        constraintLayout3 = dialog.findViewById(R.id.constraintLayout3);
        tvFirstOption = dialog.findViewById(R.id.tvFirstOption);
        tvSecondOption = dialog.findViewById(R.id.tvSecondOption);
        tvThirdOption = dialog.findViewById(R.id.tvThirdOption);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);

        cardView1.setOnClickListener(view -> {
            if (dialogListener != null&& selectedOrder != 1) dialogListener.onSelect(1, option);
        });

        cardView2.setOnClickListener(view -> {
            if (dialogListener != null && selectedOrder != 2) dialogListener.onSelect(2, option);
        });

        cardView3.setOnClickListener(view -> {
            if (dialogListener != null&& selectedOrder != 3) dialogListener.onSelect(3, option);
        });

        btnCancel.setOnClickListener(view -> dismissDialog());
    }

    private void setDialogWindow() {
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    public void showDialog() {
        dialog.show();
    }

    public void dismissDialog() {
        dialog.dismiss();
    }

    public int getSelectedOrder() {
        return selectedOrder;
    }

    public void setSelectedOrder(int selectedOrder, SupplierOption option) {
        this.selectedOrder = selectedOrder;
        this.option = option;

        cardView1.setEnabled(true);
        cardView2.setEnabled(true);
        cardView3.setEnabled(true);

        constraintLayout1.setBackgroundColor(context.getColor(R.color.white));
        tvFirstOption.setTextColor(context.getColor(R.color.primary));
        constraintLayout3.setBackgroundColor(context.getColor(R.color.white));
        tvSecondOption.setTextColor(context.getColor(R.color.primary));
        constraintLayout2.setBackgroundColor(context.getColor(R.color.white));
        tvThirdOption.setTextColor(context.getColor(R.color.primary));

        switch (selectedOrder) {
            case 1:
                constraintLayout1.setBackgroundColor(context.getColor(R.color.primary));
                tvFirstOption.setTextColor(context.getColor(R.color.white));
                cardView1.setEnabled(false);
                break;
            case 2:
                constraintLayout2.setBackgroundColor(context.getColor(R.color.primary));
                tvSecondOption.setTextColor(context.getColor(R.color.white));
                cardView2.setEnabled(false);
                break;
            case 3:
                constraintLayout3.setBackgroundColor(context.getColor(R.color.primary));
                tvThirdOption.setTextColor(context.getColor(R.color.white));
                cardView3.setEnabled(false);
                break;
        }
    }

    private DialogListener dialogListener;

    public interface DialogListener {
        void onSelect(int order, SupplierOption option);
    }

    public void setDialogListener(DialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }
}
