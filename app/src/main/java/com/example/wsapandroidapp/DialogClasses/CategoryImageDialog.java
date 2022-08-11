package com.example.wsapandroidapp.DialogClasses;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.wsapandroidapp.Adapters.ListAdapter;
import com.example.wsapandroidapp.Classes.ComponentManager;
import com.example.wsapandroidapp.Classes.Enums;
import com.example.wsapandroidapp.DataModel.CategoryImage;
import com.example.wsapandroidapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CategoryImageDialog {

    private TextView tvMessage;
    private ProgressBar pbLoading2;
    private Button btnConfirm;

    private final Context context;
    private Dialog dialog;

    private MessageDialog messageDialog;

    private boolean isListening;

    private ListAdapter listAdapter;

    private final List<CategoryImage> categoryImages = new ArrayList<>();
    private final List<String> list = new ArrayList<>();

    private ComponentManager componentManager;

    private CategoryImage categoryImage;

    public CategoryImageDialog(Context context) {
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
        dialog.setContentView(R.layout.dialog_category_image_layout);

        tvMessage = dialog.findViewById(R.id.tvMessage);
        pbLoading2 = dialog.findViewById(R.id.pbLoading2);
        RecyclerView recyclerView = dialog.findViewById(R.id.recyclerView);
        btnConfirm = dialog.findViewById(R.id.btnConfirm);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);

        messageDialog = new MessageDialog(context);

        componentManager = new ComponentManager(context);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        listAdapter = new ListAdapter(context, list);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(listAdapter);

        checkCategory();
        listAdapter.setAdapterListener(position -> {
            categoryImage = categoryImages.get(position);
            checkCategory();
        });

        btnConfirm.setOnClickListener(view -> {
            if (dialogListener != null) dialogListener.onSelect(categoryImage);
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

        clearCategory();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void clearCategory() {
        categoryImage = null;

        listAdapter.setSelectedPosition(-1);
        listAdapter.notifyDataSetChanged();

        checkCategory();
    }

    private void checkCategory() {
        componentManager.setPrimaryButtonEnabled(btnConfirm, categoryImage != null);
    }

    private ValueEventListener getCategories() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    categoryImages.clear();
                    list.clear();

                    if (snapshot.exists())
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            CategoryImage categoryImage = dataSnapshot.getValue(CategoryImage.class);

                            if (categoryImage != null) {
                                categoryImages.add(categoryImage);
                                list.add(categoryImage.getCategory());
                            }
                        }


                    if (categoryImages.size() == 0) {
                        tvMessage.setVisibility(View.VISIBLE);

                        tvMessage.setText(context.getString(R.string.no_record, "Category"));
                    } else tvMessage.setVisibility(View.GONE);
                    tvMessage.bringToFront();

                    pbLoading2.setVisibility(View.GONE);

                    listAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "onCancelled", error.toException());
                pbLoading2.setVisibility(View.GONE);

                messageDialog.setMessage(context.getString(R.string.fd_on_cancelled, "Categories"));
                messageDialog.setMessageType(Enums.ERROR_MESSAGE);
                messageDialog.showDialog();
            }
        };
    }

    public void setQuery(Query query) {
        pbLoading2.setVisibility(View.VISIBLE);
        isListening = true;
        query.addValueEventListener(getCategories());
    }

    private DialogListener dialogListener;

    public interface DialogListener {
        void onSelect(CategoryImage categoryImage);
    }

    public void setDialogListener(DialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }
}
