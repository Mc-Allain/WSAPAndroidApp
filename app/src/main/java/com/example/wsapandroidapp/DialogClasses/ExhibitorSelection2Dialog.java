package com.example.wsapandroidapp.DialogClasses;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.example.wsapandroidapp.Adapters.ExhibitorSelectionFragmentAdapter;
import com.example.wsapandroidapp.Adapters.ListHorizontalMDAdapter;
import com.example.wsapandroidapp.Classes.ComponentManager;
import com.example.wsapandroidapp.Classes.Enums;
import com.example.wsapandroidapp.DataModel.Exhibitor;
import com.example.wsapandroidapp.Fragments.ExhibitorSelectionAllFragment;
import com.example.wsapandroidapp.Fragments.ExhibitorSelectionSelectedFragment;
import com.example.wsapandroidapp.Fragments.ExhibitorSelectionUnselectedFragment;
import com.example.wsapandroidapp.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

public class ExhibitorSelection2Dialog {

    public EditText etSearch;

    private final Context context;
    private Dialog dialog;

    ExhibitorSelectionFragmentAdapter exhibitorSelectionFragmentAdapter;

    ExhibitorSelectionAllFragment exhibitorSelectionAllFragment = new ExhibitorSelectionAllFragment();
    ExhibitorSelectionUnselectedFragment exhibitorSelectionUnselectedFragment = new ExhibitorSelectionUnselectedFragment();
    ExhibitorSelectionSelectedFragment exhibitorSelectionSelectedFragment = new ExhibitorSelectionSelectedFragment();

    private List<Exhibitor> exhibitors = new ArrayList<>(),
            exhibitorsCopy = new ArrayList<>(),
            newSelectedExhibitors = new ArrayList<>(),
            newSelectedExhibitorsCopy = new ArrayList<>();

    private ListHorizontalMDAdapter listHorizontalMDAdapter;

    public ComponentManager componentManager;

    private String searchExhibitor = "";

    public ExhibitorSelection2Dialog(Context context) {
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
        dialog.setContentView(R.layout.dialog_exhibitor_selection_layout_2);

        etSearch = dialog.findViewById(R.id.etSearch);
        RecyclerView recyclerView = dialog.findViewById(R.id.recyclerView);
        ViewPager2 viewPager2 = dialog.findViewById(R.id.viewPager2);
        Button btnConfirm = dialog.findViewById(R.id.btnConfirm);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);

        List<String> list = Arrays.asList(
                context.getString(R.string.all_exhibitors),
                context.getString(R.string.unselected_exhibitors),
                context.getString(R.string.selected_exhibitors)
        );

        FragmentActivity fragmentActivity = (FragmentActivity) context;

        FragmentManager fragmentManager = fragmentActivity.getSupportFragmentManager();

        exhibitorSelectionAllFragment.setFragmentListener(selectedExhibitors ->
                newSelectedExhibitors = new ArrayList<>(selectedExhibitors));

        exhibitorSelectionUnselectedFragment.setFragmentListener(selectedExhibitors ->
                newSelectedExhibitors = new ArrayList<>(selectedExhibitors));

        exhibitorSelectionSelectedFragment.setFragmentListener(selectedExhibitors ->
                newSelectedExhibitors = new ArrayList<>(selectedExhibitors));

        exhibitorSelectionFragmentAdapter =
                new ExhibitorSelectionFragmentAdapter(fragmentManager, fragmentActivity.getLifecycle(),
                        exhibitorSelectionAllFragment, exhibitorSelectionUnselectedFragment, exhibitorSelectionSelectedFragment);
        viewPager2.setAdapter(exhibitorSelectionFragmentAdapter);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        listHorizontalMDAdapter = new ListHorizontalMDAdapter(context, list);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(listHorizontalMDAdapter);

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                listHorizontalMDAdapter.setSelectedPosition(position);

                filterExhibitors();
            }
        });

        listHorizontalMDAdapter.setAdapterListener(viewPager2::setCurrentItem);

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
            if (dialogListener != null) dialogListener.sendExhibitors(newSelectedExhibitors);
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

    @SuppressLint("NotifyDataSetChanged")
    public void filterExhibitors() {
        List<Exhibitor> exhibitorsTemp = new ArrayList<>(exhibitorsCopy);
        List<Exhibitor> overallSelectedExhibitorsTemp = new ArrayList<>(newSelectedExhibitorsCopy);

        exhibitors.clear();
        newSelectedExhibitors.clear();

        for (int i = 0; i < exhibitorsTemp.size(); i++) {
            Exhibitor exhibitor = exhibitorsTemp.get(i);

            boolean isSearched = searchExhibitor.trim().length() == 0 ||
                    exhibitor.getExhibitor().toLowerCase().contains(searchExhibitor.toLowerCase());

            if (isSearched) exhibitors.add(exhibitor);
        }

        for (int i = 0; i < overallSelectedExhibitorsTemp.size(); i++) {
            Exhibitor exhibitor = overallSelectedExhibitorsTemp.get(i);

            boolean isSearched = searchExhibitor.trim().length() == 0 ||
                    exhibitor.getExhibitor().toLowerCase().contains(searchExhibitor.toLowerCase());

            if (isSearched) newSelectedExhibitors.add(exhibitor);
        }

        updateAdapters();
    }

    public void setExhibitors(List<Exhibitor> exhibitors, List<Exhibitor> selectedExhibitors) {
        this.exhibitors = new ArrayList<>(exhibitors);
        this.exhibitorsCopy = new ArrayList<>(exhibitors);
        this.newSelectedExhibitors = new ArrayList<>(selectedExhibitors);
        this.newSelectedExhibitorsCopy = new ArrayList<>(selectedExhibitors);

        filterExhibitors();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateAdapters() {
        if (exhibitorSelectionAllFragment != null)
            exhibitorSelectionAllFragment.setExhibitors(exhibitors, newSelectedExhibitors);

        if (exhibitorSelectionUnselectedFragment != null)
            exhibitorSelectionUnselectedFragment.setExhibitors(exhibitors, newSelectedExhibitors);

        if (exhibitorSelectionSelectedFragment != null)
            exhibitorSelectionSelectedFragment.setExhibitors(exhibitors, newSelectedExhibitors);

        if (exhibitorSelectionFragmentAdapter != null)
            exhibitorSelectionFragmentAdapter.notifyDataSetChanged();
    }

    private DialogListener dialogListener;

    public interface DialogListener {
        void activateVoiceRecognition();
        void sendExhibitors(List<Exhibitor> newSelectedExhibitors);
    }

    public void setDialogListener(DialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }
}
