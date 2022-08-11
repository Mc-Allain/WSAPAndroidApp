package com.example.wsapandroidapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.wsapandroidapp.Adapters.ExpoAdapter;
import com.example.wsapandroidapp.Classes.ComponentManager;
import com.example.wsapandroidapp.Classes.Enums;
import com.example.wsapandroidapp.Classes.Units;
import com.example.wsapandroidapp.DataModel.Expo;
import com.example.wsapandroidapp.DialogClasses.ExpoStatusDialog;
import com.example.wsapandroidapp.DialogClasses.MessageDialog;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ExposActivity extends AppCompatActivity {

    EditText etSearch;
    TextView tvMessage, tvStatus;
    ProgressBar pbLoading2;
    RecyclerView recyclerView;
    ConstraintLayout statusLayout;

    Context context;

    MessageDialog messageDialog;
    ExpoStatusDialog expoStatusDialog;

    FirebaseDatabase firebaseDatabase;

    Query exposQuery;

    boolean isListening;

    List<Expo> expos = new ArrayList<>(), exposCopy = new ArrayList<>();

    ExpoAdapter expoAdapter;

    ComponentManager componentManager;

    String searchExpo = "";
    int selectedStatus = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expos);

        etSearch = findViewById(R.id.etSearch);
        tvMessage = findViewById(R.id.tvMessage);
        tvStatus = findViewById(R.id.tvStatus);
        pbLoading2 = findViewById(R.id.pbLoading2);
        recyclerView = findViewById(R.id.recyclerView);
        statusLayout = findViewById(R.id.statusLayout);

        context = ExposActivity.this;

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
        exposQuery = firebaseDatabase.getReference("expos");

        pbLoading2.setVisibility(View.VISIBLE);
        isListening = true;
        etSearch.setVisibility(View.INVISIBLE);
        exposQuery.addValueEventListener(getExpos());

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

        statusLayout.setOnClickListener(view1 -> expoStatusDialog.showDialog());
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

        statusLayout.setVisibility(View.VISIBLE);

        expoAdapter.notifyDataSetChanged();
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
        exposQuery.addListenerForSingleValueEvent(getExpos());

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