package com.example.wsapandroidapp.DialogClasses;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.wsapandroidapp.Adapters.ImageSliderFragmentAdapter;
import com.example.wsapandroidapp.Adapters.ListAdapter;
import com.example.wsapandroidapp.Classes.ComponentManager;
import com.example.wsapandroidapp.Classes.Enums;
import com.example.wsapandroidapp.DataModel.Chapter;
import com.example.wsapandroidapp.DataModel.Division;
import com.example.wsapandroidapp.Fragments.ImageSliderFragment;
import com.example.wsapandroidapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

public class SupplierChapterDialog {

    private ViewPager2 viewPager2;
    private ImageView imgBack, imgForward;
    private TextView tvPager, tvMessage;
    private ProgressBar pbLoading, pbLoading2;
    private Button btnConfirm;

    private final Context context;
    private Dialog dialog;

    private MessageDialog messageDialog;

    private Query chaptersQuery;

    private boolean isListening;

    private ListAdapter listAdapter;

    private final List<ImageSliderFragment> imageSliderFragmentList = new ArrayList<>();
    private final List<Division> divisions = new ArrayList<>();
    private final List<Chapter> chapters = new ArrayList<>();
    private final List<String> list = new ArrayList<>();

    private ComponentManager componentManager;

    private ImageSliderFragmentAdapter imageSliderFragmentAdapter;

    private int pagePosition;
    private String selectedDivisionId;

    private Chapter chapter;

    public SupplierChapterDialog(Context context) {
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
        dialog.setContentView(R.layout.dialog_supplier_chapter_layout);

        viewPager2 = dialog.findViewById(R.id.viewPager2);
        imgBack = dialog.findViewById(R.id.imgBack);
        imgForward = dialog.findViewById(R.id.imgForward);
        tvPager = dialog.findViewById(R.id.tvPager);
        tvMessage = dialog.findViewById(R.id.tvMessage);
        pbLoading = dialog.findViewById(R.id.pbLoading);
        pbLoading2 = dialog.findViewById(R.id.pbLoading2);
        RecyclerView recyclerView = dialog.findViewById(R.id.recyclerView);
        btnConfirm = dialog.findViewById(R.id.btnConfirm);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);

        FragmentActivity fragmentActivity = (FragmentActivity) context;

        messageDialog = new MessageDialog(context);

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(context.getString(R.string.firebase_RTDB_url));
        Query divisionsQuery = firebaseDatabase.getReference("divisions");
        chaptersQuery = firebaseDatabase.getReference("chapters").orderByChild("chapter");

        componentManager = new ComponentManager(context);

        pbLoading.setVisibility(View.VISIBLE);
        pbLoading2.setVisibility(View.VISIBLE);
        isListening = true;
        divisionsQuery.addValueEventListener(getDivisions());

        FragmentManager fragmentManager = fragmentActivity.getSupportFragmentManager();

        imageSliderFragmentAdapter = new ImageSliderFragmentAdapter(fragmentManager, fragmentActivity.getLifecycle(), imageSliderFragmentList);
        viewPager2.setAdapter(imageSliderFragmentAdapter);

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                pagePosition = position;

                imgBack.setVisibility(View.VISIBLE);
                imgForward.setVisibility(View.VISIBLE);

                if (position == 0) imgBack.setVisibility(View.GONE);
                else if (position == imageSliderFragmentAdapter.getItemCount() - 1)
                    imgForward.setVisibility(View.GONE);

                tvPager.setVisibility(View.VISIBLE);
                tvPager.setText(context.getString(R.string.pager_text, position + 1, imageSliderFragmentAdapter.getItemCount()));

                selectedDivisionId = divisions.get(position).getId();

                chaptersQuery.addValueEventListener(getChapters());

                clearChapter();
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        listAdapter = new ListAdapter(context, list);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(listAdapter);

        checkChapter();
        listAdapter.setAdapterListener(position -> {
            chapter = chapters.get(position);
            checkChapter();
        });

        imgBack.setOnClickListener(view1 -> {
            pagePosition -= 1;
            viewPager2.setCurrentItem(pagePosition);
        });

        imgForward.setOnClickListener(view1 -> {
            pagePosition += 1;
            viewPager2.setCurrentItem(pagePosition);
        });

        btnConfirm.setOnClickListener(view -> {
            if (dialogListener != null) dialogListener.onSelect(chapter);
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

        clearChapter();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void clearChapter() {
        chapter = null;

        listAdapter.setSelectedPosition(-1);
        listAdapter.notifyDataSetChanged();

        checkChapter();
    }

    private void checkChapter() {
        componentManager.setPrimaryButtonEnabled(btnConfirm, chapter != null);
    }

    private ValueEventListener getDivisions() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    imageSliderFragmentList.clear();
                    divisions.clear();

                    if (snapshot.exists())
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Division division = dataSnapshot.getValue(Division.class);
                            if (division != null) {
                                divisions.add(division);

                                Bundle bundle = new Bundle();
                                bundle.putString("expoVenueImage", division.getImage());

                                ImageSliderFragment imageSliderFragment = new ImageSliderFragment();
                                imageSliderFragment.setArguments(bundle);

                                imageSliderFragmentList.add(imageSliderFragment);
                            }
                        }

                    pbLoading.setVisibility(View.GONE);

                    imageSliderFragmentAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "onCancelled", error.toException());
                pbLoading.setVisibility(View.GONE);
                pbLoading2.setVisibility(View.GONE);

                messageDialog.setMessage(context.getString(R.string.fd_on_cancelled, "Divisions"));
                messageDialog.setMessageType(Enums.ERROR_MESSAGE);
                messageDialog.showDialog();
            }
        };
    }

    private ValueEventListener getChapters() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    chapters.clear();
                    list.clear();

                    if (snapshot.exists())
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Chapter chapter = dataSnapshot.getValue(Chapter.class);

                            if (chapter != null)
                                if (chapter.getDivision().equals(selectedDivisionId)) {
                                    chapters.add(chapter);
                                    list.add(chapter.getChapter());
                                }
                        }


                    if (chapters.size() == 0) {
                        tvMessage.setVisibility(View.VISIBLE);

                        tvMessage.setText(context.getString(R.string.no_record, "Chapter"));
                    } else tvMessage.setVisibility(View.GONE);
                    tvMessage.bringToFront();

                    pbLoading2.setVisibility(View.GONE);

                    listAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "onCancelled", error.toException());
                pbLoading.setVisibility(View.GONE);
                pbLoading2.setVisibility(View.GONE);

                messageDialog.setMessage(context.getString(R.string.fd_on_cancelled, "Chapters"));
                messageDialog.setMessageType(Enums.ERROR_MESSAGE);
                messageDialog.showDialog();
            }
        };
    }

    private DialogListener dialogListener;

    public interface DialogListener {
        void onSelect(Chapter chapter);
    }

    public void setDialogListener(DialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }
}
