package com.example.wsapandroidapp.DialogClasses;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.wsapandroidapp.DataModel.Division;
import com.example.wsapandroidapp.R;

import java.util.ArrayList;
import java.util.List;

import androidx.cardview.widget.CardView;

public class ExpoDivisionDialog {

    private ImageView imgLuzon, imgVisayas, imgMindanao;;

    private final Context context;
    private Dialog dialog;

    private List<Division> divisions = new ArrayList<>();

    public ExpoDivisionDialog(Context context) {
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
        dialog.setContentView(R.layout.dialog_expo_division_layout);

        CardView luzonCard = dialog.findViewById(R.id.luzonCard);
        CardView visayasCard = dialog.findViewById(R.id.visayasCard);
        CardView mindanaoCard = dialog.findViewById(R.id.mindanaoCard);
        imgLuzon = dialog.findViewById(R.id.imgLuzon);
        imgVisayas = dialog.findViewById(R.id.imgVisayas);
        imgMindanao = dialog.findViewById(R.id.imgMindanao);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);

        luzonCard.setOnClickListener(view -> {
            if (dialogListener != null) dialogListener.onSelect(divisions.get(0));
        });

        visayasCard.setOnClickListener(view -> {
            if (dialogListener != null) dialogListener.onSelect(divisions.get(1));
        });

        mindanaoCard.setOnClickListener(view -> {
            if (dialogListener != null) dialogListener.onSelect(divisions.get(2));
        });

        btnCancel.setOnClickListener(view -> dismissDialog());
    }

    private void setDialogWindow() {
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    public void showDialog() {
        dialog.show();
    }

    public void dismissDialog() {
        dialog.dismiss();
    }

    public void setDivisions(List<Division> divisions) {
        this.divisions = divisions;

        Glide.with(context).load(divisions.get(0).getImage()).placeholder(R.drawable.ic_wsap).
                error(R.drawable.ic_wsap).into(imgLuzon);

        Glide.with(context).load(divisions.get(1).getImage()).placeholder(R.drawable.ic_wsap).
                error(R.drawable.ic_wsap).into(imgVisayas);

        Glide.with(context).load(divisions.get(2).getImage()).placeholder(R.drawable.ic_wsap).
                error(R.drawable.ic_wsap).into(imgMindanao);
    }

    private DialogListener dialogListener;

    public interface DialogListener {
        void onSelect(Division division);
    }

    public void setDialogListener(DialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }
}
