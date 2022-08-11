package com.example.wsapandroidapp.Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.wsapandroidapp.Adapters.ImageSliderFragmentAdapter;
import com.example.wsapandroidapp.Adapters.ExpoAdapter;
import com.example.wsapandroidapp.Classes.ComponentManager;
import com.example.wsapandroidapp.Classes.Enums;
import com.example.wsapandroidapp.Classes.Units;
import com.example.wsapandroidapp.DataModel.Division;
import com.example.wsapandroidapp.DataModel.Expo;
import com.example.wsapandroidapp.DialogClasses.ExpoStatusDialog;
import com.example.wsapandroidapp.DialogClasses.MessageDialog;
import com.example.wsapandroidapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import static android.app.Activity.RESULT_OK;

public class ExposFragment extends Fragment {

    ViewPager2 viewPager2;
    EditText etSearch;
    ImageView imgBack, imgForward;
    TextView tvPager, tvMessage, tvStatus;
    ProgressBar pbLoading, pbLoading2;
    RecyclerView recyclerView;
    ConstraintLayout statusLayout;

    FragmentActivity fragmentActivity;
    Context context;

    MessageDialog messageDialog;
    ExpoStatusDialog expoStatusDialog;

    FirebaseDatabase firebaseDatabase;

    Query divisionsQuery, exposQuery;

    boolean isListening;

    List<ImageSliderFragment> imageSliderFragmentList = new ArrayList<>();
    List<Division> divisions = new ArrayList<>();

    List<Expo> expos = new ArrayList<>(), exposCopy = new ArrayList<>();

    ImageSliderFragmentAdapter imageSliderFragmentAdapter;
    ExpoAdapter expoAdapter;

    ComponentManager componentManager;

    int pagePosition;
    String selectedDivisionId;

    String searchExpo = "";
    int selectedStatus = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_expos, container, false);

        viewPager2 = view.findViewById(R.id.viewPager2);
        etSearch = view.findViewById(R.id.etSearch);
        imgBack = view.findViewById(R.id.imgBack);
        imgForward = view.findViewById(R.id.imgForward);
        tvPager = view.findViewById(R.id.tvPager);
        tvMessage = view.findViewById(R.id.tvMessage);
        tvStatus = view.findViewById(R.id.tvStatus);
        pbLoading = view.findViewById(R.id.pbLoading);
        pbLoading2 = view.findViewById(R.id.pbLoading2);
        recyclerView = view.findViewById(R.id.recyclerView);
        statusLayout = view.findViewById(R.id.statusLayout);

        fragmentActivity = (FragmentActivity) requireContext();

        context = fragmentActivity;

        messageDialog = new MessageDialog(context);
        expoStatusDialog = new ExpoStatusDialog(context);

        expoStatusDialog.setCurrentSelectedIndex(selectedStatus);
        expoStatusDialog.setDialogListener(currentSelectedIndex -> {
            selectedStatus = currentSelectedIndex;
            switch (currentSelectedIndex) {
                case 0:
                    tvStatus.setText(getString(R.string.upcoming));
                    break;
                case 1:
                    tvStatus.setText(getString(R.string.past));
                    break;
                case 2:
                    tvStatus.setText(getString(R.string.all));
                    break;
            }

            expoStatusDialog.dismissDialog();

            filterExpos();
        });

        firebaseDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_RTDB_url));
        divisionsQuery = firebaseDatabase.getReference("divisions");
        exposQuery = firebaseDatabase.getReference("expos");

        pbLoading.setVisibility(View.VISIBLE);
        pbLoading2.setVisibility(View.VISIBLE);
        isListening = true;
        etSearch.setVisibility(View.INVISIBLE);
        divisionsQuery.addValueEventListener(getDivisions());

        FragmentManager fragmentManager = fragmentActivity.getSupportFragmentManager();

        imageSliderFragmentAdapter = new ImageSliderFragmentAdapter(fragmentManager, getLifecycle(), imageSliderFragmentList);
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
                tvPager.setText(getString(R.string.pager_text, position + 1, imageSliderFragmentAdapter.getItemCount()));

                statusLayout.setVisibility(View.VISIBLE);

                selectedDivisionId = divisions.get(position).getId();

                exposQuery.addValueEventListener(getExpos());
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        expoAdapter = new ExpoAdapter(context, expos);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(expoAdapter);

        componentManager = new ComponentManager(context);
        componentManager.setInputRightDrawable(etSearch, true, Enums.VOICE_RECOGNITION);
        componentManager.setVoiceRecognitionListener(() -> startActivityForResult(componentManager.voiceRecognitionIntent(), Enums.VOICE_RECOGNITION_REQUEST_CODE));

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                searchExpo = editable != null ? editable.toString() : "";

                filterExpos();
            }
        });

        imgBack.setOnClickListener(view1 -> {
            pagePosition -= 1;
            viewPager2.setCurrentItem(pagePosition);
        });

        imgForward.setOnClickListener(view1 -> {
            pagePosition += 1;
            viewPager2.setCurrentItem(pagePosition);
        });

        statusLayout.setOnClickListener(view1 -> expoStatusDialog.showDialog());

        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void filterExpos() {
        List<Expo> exposTemp = new ArrayList<>(exposCopy);

        expos.clear();

        for (int i = 0; i < exposTemp.size(); i++) {
            Expo expo = exposTemp.get(i);

            boolean isUpcomingExpo = expo.getDateTimeRange().getEndDateTime() >= new Date().getTime();

            boolean isSearched = searchExpo.trim().length() == 0 ||
                    expo.getExpo().toLowerCase().contains(searchExpo.toLowerCase());

            if ((isUpcomingExpo && selectedStatus == 0 || selectedStatus == 2) && isSearched)
                expos.add(expo);
            else if ((!isUpcomingExpo && selectedStatus == 1) && isSearched) expos.add(expo);
        }

        expos.sort((expo, t1) ->
                (int) (Units.msToDay(expo.getDateTimeRange().getStartDateTime()) - Units.msToDay(t1.getDateTimeRange().getStartDateTime()))
        );

        if (selectedStatus != 0)
            Collections.reverse(expos);

        if (expos.size() == 0) {
            tvMessage.setVisibility(View.VISIBLE);

            String noRecordsError = getString(R.string.no_record, "Expo");
            if (selectedStatus == 0)
                noRecordsError = getString(R.string.no_record, "Upcoming Expo");
            else if (selectedStatus == 1)
                noRecordsError = getString(R.string.no_record, "Past Expo");

            tvMessage.setText(noRecordsError);
        } else tvMessage.setVisibility(View.GONE);
        tvMessage.bringToFront();

        pbLoading2.setVisibility(View.GONE);
        etSearch.setVisibility(View.VISIBLE);

        expoAdapter.notifyDataSetChanged();
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
                                bundle.putString("image", division.getImage());

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

                messageDialog.setMessage(getString(R.string.fd_on_cancelled, "Divisions"));
                messageDialog.setMessageType(Enums.ERROR_MESSAGE);
                messageDialog.showDialog();
            }
        };
    }

    private ValueEventListener getExpos() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    expos.clear();

                    if (snapshot.exists())
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Expo expo = dataSnapshot.getValue(Expo.class);

                            if (expo != null)
                                if (expo.getDivision().equals(selectedDivisionId))
                                    expos.add(expo);
                        }

                    expos.sort((expo, t1) ->
                            (int) (Units.msToDay(expo.getDateTimeRange().getStartDateTime()) - Units.msToDay(t1.getDateTimeRange().getStartDateTime()))
                    );

                    exposCopy = new ArrayList<>(expos);

                    filterExpos();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "onCancelled", error.toException());
                pbLoading.setVisibility(View.GONE);
                pbLoading2.setVisibility(View.GONE);

                messageDialog.setMessage(getString(R.string.fd_on_cancelled, "Expos"));
                messageDialog.setMessageType(Enums.ERROR_MESSAGE);
                messageDialog.showDialog();
            }
        };
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Enums.VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            assert data != null;
            etSearch.setText(data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0));

            filterExpos();
        }
    }

    @Override
    public void onResume() {
        isListening = true;
        divisionsQuery.addListenerForSingleValueEvent(getDivisions());

        super.onResume();
    }

    @Override
    public void onStop() {
        isListening = false;

        super.onStop();
    }

    @Override
    public void onDestroy() {
        isListening = false;

        super.onDestroy();
    }
}