package com.example.wsapandroidapp.DialogClasses;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.wsapandroidapp.Adapters.ExhibitorSelectionAdapter;
import com.example.wsapandroidapp.Classes.ComponentManager;
import com.example.wsapandroidapp.Classes.Enums;
import com.example.wsapandroidapp.DataModel.ExpoExhibitor2;
import com.example.wsapandroidapp.R;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ExhibitorSelectionDialog {

    public EditText etSearch;
    private TextView tvMessage;

    private final Context context;
    private Dialog dialog;

    private ExhibitorSelectionAdapter exhibitorSelectionAdapter;

    List<ExpoExhibitor2> expoExhibitors2 = new ArrayList<>(), expoExhibitors2Copy = new ArrayList<>(),
            selectedExpoExhibitors = new ArrayList<>(), selectedExpoExhibitorsCopy = new ArrayList<>();

    public ComponentManager componentManager;

    private String searchExhibitor = "";

    public ExhibitorSelectionDialog(Context context) {
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
        dialog.setContentView(R.layout.dialog_exhibitor_selection_layout);

        etSearch = dialog.findViewById(R.id.etSearch);
        tvMessage = dialog.findViewById(R.id.tvMessage);
        RecyclerView recyclerView = dialog.findViewById(R.id.recyclerView);
        Button btnConfirm = dialog.findViewById(R.id.btnConfirm);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false);
        exhibitorSelectionAdapter = new ExhibitorSelectionAdapter(context, expoExhibitors2, selectedExpoExhibitors);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(exhibitorSelectionAdapter);

        exhibitorSelectionAdapter.setAdapterListener(new ExhibitorSelectionAdapter.AdapterListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onSelect(ExpoExhibitor2 expoExhibitor2) {
                selectedExpoExhibitors.add(expoExhibitor2);

                exhibitorSelectionAdapter.notifyDataSetChanged();
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onUnselect(ExpoExhibitor2 expoExhibitor2) {
                List<ExpoExhibitor2> selectedExpoExhibitors2 = new ArrayList<>(selectedExpoExhibitors);

                selectedExpoExhibitors.clear();

                for (ExpoExhibitor2 selectedExpoExhibitor : selectedExpoExhibitors2)
                    if (!expoExhibitor2.getId().equals(selectedExpoExhibitor.getId()))
                        selectedExpoExhibitors.add(selectedExpoExhibitor);

                exhibitorSelectionAdapter.notifyDataSetChanged();
            }
        });

        componentManager = new ComponentManager(context);
        componentManager.setInputRightDrawable(etSearch, true, Enums.VOICE_RECOGNITION);
        componentManager.setVoiceRecognitionListener(() -> {
            if (dialogListener != null) dialogListener.activateVoiceRecognition();
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                searchExhibitor = editable != null ? editable.toString() : "";

                filterExhibitors();
            }
        });

        btnConfirm.setOnClickListener(view -> {
            if (dialogListener != null) dialogListener.sendExhibitors(selectedExpoExhibitors);
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

    @SuppressLint("NotifyDataSetChanged")
    public void dismissDialog() {
        dialog.dismiss();

        this.selectedExpoExhibitors.clear();
        this.selectedExpoExhibitors.addAll(selectedExpoExhibitorsCopy);

        exhibitorSelectionAdapter.notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void filterExhibitors() {
        List<ExpoExhibitor2> expoExhibitors2Temp = new ArrayList<>(expoExhibitors2Copy);

        expoExhibitors2.clear();

        for (int i = 0; i < expoExhibitors2Temp.size(); i++) {
            ExpoExhibitor2 expoExhibitor2 = expoExhibitors2Temp.get(i);

            boolean isSearched = searchExhibitor.trim().length() == 0 ||
                    expoExhibitor2.getExhibitor().toLowerCase().contains(searchExhibitor.toLowerCase());

            if (isSearched) expoExhibitors2.add(expoExhibitor2);
        }

        if (expoExhibitors2.size() == 0) {
            tvMessage.setVisibility(View.VISIBLE);

            tvMessage.setText(context.getString(R.string.no_record, "Record"));
        } else tvMessage.setVisibility(View.GONE);
        tvMessage.bringToFront();

        etSearch.setVisibility(View.VISIBLE);

        exhibitorSelectionAdapter.notifyDataSetChanged();
    }

    public void setExhibitors(List<ExpoExhibitor2> expoExhibitors2,
                              List<ExpoExhibitor2> selectedExpoExhibitors) {
        this.expoExhibitors2.clear();
        this.expoExhibitors2Copy.clear();
        this.selectedExpoExhibitors.clear();
        this.selectedExpoExhibitorsCopy.clear();

        this.expoExhibitors2.addAll(expoExhibitors2);
        this.expoExhibitors2Copy.addAll(expoExhibitors2);
        this.selectedExpoExhibitors.addAll(selectedExpoExhibitors);
        this.selectedExpoExhibitorsCopy.addAll(selectedExpoExhibitors);

        filterExhibitors();
    }

    private DialogListener dialogListener;

    public interface DialogListener {
        void activateVoiceRecognition();
        void sendExhibitors(List<ExpoExhibitor2> newSelectedExhibitors);
    }

    public void setDialogListener(DialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }
}
