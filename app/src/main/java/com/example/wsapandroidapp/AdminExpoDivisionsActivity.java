package com.example.wsapandroidapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.wsapandroidapp.Adapters.AdminExpoAdapter;
import com.example.wsapandroidapp.Classes.ComponentManager;
import com.example.wsapandroidapp.Classes.Credentials;
import com.example.wsapandroidapp.Classes.Enums;
import com.example.wsapandroidapp.Classes.Units;
import com.example.wsapandroidapp.DataModel.Division;
import com.example.wsapandroidapp.DataModel.Expo;
import com.example.wsapandroidapp.DialogClasses.ConfirmationDialog;
import com.example.wsapandroidapp.DialogClasses.ExpoDivisionDialog;
import com.example.wsapandroidapp.DialogClasses.ExpoFormWithDivisionDialog;
import com.example.wsapandroidapp.DialogClasses.ExpoStatusDialog;
import com.example.wsapandroidapp.DialogClasses.LoadingDialog;
import com.example.wsapandroidapp.DialogClasses.MessageDialog;
import com.example.wsapandroidapp.Fragments.ImageSliderFragment;
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
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AdminExpoDivisionsActivity extends AppCompatActivity {

    EditText etSearch;
    ImageView imgAdd;
    CardView luzonCard, visayasCard, mindanaoCard;
    ImageView imgLuzon, imgVisayas, imgMindanao;

    ConstraintLayout constraintLayout;
    TextView tvMessage, tvStatus;
    RecyclerView recyclerView;
    ConstraintLayout statusLayout;

    Context context;

    LoadingDialog loadingDialog;
    MessageDialog messageDialog;
    ConfirmationDialog confirmationDialog;
    ExpoStatusDialog expoStatusDialog;
    ExpoFormWithDivisionDialog expoFormWithDivisionDialog;
    ExpoDivisionDialog expoDivisionDialog;

    FirebaseDatabase firebaseDatabase;

    Query divisionsQuery, exposQuery;

    boolean isListening;

    List<Division> divisions = new ArrayList<>();

    List<Expo> expos = new ArrayList<>(), exposCopy = new ArrayList<>();

    AdminExpoAdapter adminExpoAdapter;

    ComponentManager componentManager;

    String searchExpo = "";
    int selectedStatus = 2;

    Expo selectedExpo = new Expo();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_expo_divisions);

        etSearch = findViewById(R.id.etSearch);
        imgAdd = findViewById(R.id.imgAdd);
        luzonCard = findViewById(R.id.luzonCard);
        visayasCard = findViewById(R.id.visayasCard);
        mindanaoCard = findViewById(R.id.mindanaoCard);
        imgLuzon = findViewById(R.id.imgLuzon);
        imgVisayas = findViewById(R.id.imgVisayas);
        imgMindanao = findViewById(R.id.imgMindanao);

        constraintLayout = findViewById(R.id.constraintLayout);
        tvMessage = findViewById(R.id.tvMessage);
        tvStatus = findViewById(R.id.tvStatus);
        recyclerView = findViewById(R.id.recyclerView);
        statusLayout = findViewById(R.id.statusLayout);

        context = AdminExpoDivisionsActivity.this;

        loadingDialog = new LoadingDialog(context);
        messageDialog = new MessageDialog(context);
        confirmationDialog = new ConfirmationDialog(context);

        confirmationDialog.setDialogListener(() -> {
            expoFormWithDivisionDialog.deleteExpo(selectedExpo, true);

            confirmationDialog.dismissDialog();
        });

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

        expoFormWithDivisionDialog = new ExpoFormWithDivisionDialog(context);

        expoFormWithDivisionDialog.setDirectory("expos");
        expoFormWithDivisionDialog.setDialogListener(() -> {
            if (ActivityCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED
            ) openStorage();
            else {
                ActivityCompat.requestPermissions((Activity) context,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        Enums.GENERAL_REQUEST_CODE);
            }
        });

        expoDivisionDialog = new ExpoDivisionDialog(context);

        expoDivisionDialog.setDialogListener(division -> {
            selectedExpo.setDivision(division.getId());
            moveDivision();
        });

        firebaseDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_RTDB_url));
        divisionsQuery = firebaseDatabase.getReference("divisions");
        exposQuery = firebaseDatabase.getReference("expos");

        loadingDialog.showDialog();
        isListening = true;
        etSearch.setVisibility(View.INVISIBLE);
        divisionsQuery.addValueEventListener(getDivisions());

        expoFormWithDivisionDialog.setDatabaseReference(exposQuery.getRef());

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        adminExpoAdapter = new AdminExpoAdapter(context, expos);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adminExpoAdapter);

        adminExpoAdapter.setAdapterListener(new AdminExpoAdapter.AdapterListener() {
            @Override
            public void onClick(Expo expo) {
                Intent intent = new Intent(context, AdminExpoDetailsActivity.class);
                intent.putExtra("expoId", expo.getId());
                context.startActivity(intent);
            }

            @Override
            public void onEdit(Expo expo) {
                Division division = divisions.get(0);
                for (Division division1 : divisions)
                    if (expo.getDivision().equals(division1.getId()))
                        division = division1;

                expoFormWithDivisionDialog.setUpdateMode(true);
                expoFormWithDivisionDialog.setExpo(expo, division);
                expoFormWithDivisionDialog.showDialog();
            }

            @Override
            public void onDelete(Expo expo) {
                selectedExpo = expo;
                confirmationDialog.setMessage(getString(R.string.confirmation_prompt, "delete the expo"));
                confirmationDialog.showDialog();
            }

            @Override
            public void onMove(Expo expo) {
                selectedExpo = expo;
                expoDivisionDialog.showDialog();
            }
        });

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

                if (Credentials.fullTrim(searchExpo).length() > 0)
                    constraintLayout.setVisibility(View.VISIBLE);
                else constraintLayout.setVisibility(View.GONE);

                filterExpos();
            }
        });

        luzonCard.setOnClickListener(view -> {
            Intent intent = new Intent(context, AdminExposActivity.class);
            intent.putExtra("divisionId", divisions.get(0).getId());
            startActivity(intent);
        });

        visayasCard.setOnClickListener(view -> {
            Intent intent = new Intent(context, AdminExposActivity.class);
            intent.putExtra("divisionId", divisions.get(1).getId());
            startActivity(intent);
        });

        mindanaoCard.setOnClickListener(view -> {
            Intent intent = new Intent(context, AdminExposActivity.class);
            intent.putExtra("divisionId", divisions.get(2).getId());
            startActivity(intent);
        });

        statusLayout.setOnClickListener(view1 -> expoStatusDialog.showDialog());

        imgAdd.setOnClickListener(view -> {
            expoFormWithDivisionDialog.setUpdateMode(false);
            expoFormWithDivisionDialog.showDialog();
        });
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
        constraintLayout.bringToFront();

        loadingDialog.dismissDialog();
        etSearch.setVisibility(View.VISIBLE);

        Glide.with(context).load(divisions.get(0).getImage()).placeholder(R.drawable.ic_wsap).
                error(R.drawable.ic_wsap).into(imgLuzon);

        Glide.with(context).load(divisions.get(1).getImage()).placeholder(R.drawable.ic_wsap).
                error(R.drawable.ic_wsap).into(imgVisayas);

        Glide.with(context).load(divisions.get(2).getImage()).placeholder(R.drawable.ic_wsap).
                error(R.drawable.ic_wsap).into(imgMindanao);

        adminExpoAdapter.notifyDataSetChanged();

        expoFormWithDivisionDialog.setDivisions(divisions);
        expoDivisionDialog.setDivisions(divisions);
    }

    private ValueEventListener getDivisions() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
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
                            }
                        }

                    exposQuery.addValueEventListener(getExpos());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "onCancelled", error.toException());
                loadingDialog.dismissDialog();

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

                            if (expo != null) expos.add(expo);
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
                loadingDialog.dismissDialog();

                messageDialog.setMessage(getString(R.string.fd_on_cancelled, "Expos"));
                messageDialog.setMessageType(Enums.ERROR_MESSAGE);
                messageDialog.showDialog();
            }
        };
    }

    private void moveDivision() {
        loadingDialog.showDialog();

        exposQuery.getRef().child(selectedExpo.getId())
                .setValue(selectedExpo).addOnCompleteListener(task -> {
                    loadingDialog.dismissDialog();

                    if (task.isSuccessful()) {
                        expoDivisionDialog.dismissDialog();

                        messageDialog.setMessageType(Enums.SUCCESS_MESSAGE);
                        messageDialog.setMessage("Successfully moved the expo to another division.");
                    } else {
                        messageDialog.setMessageType(Enums.ERROR_MESSAGE);
                        messageDialog.setMessage("Failed to moved the expo to another division.");
                    }

                    messageDialog.showDialog();
                });
    }

    @SuppressWarnings("deprecation")
    private void openStorage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, Enums.PICK_IMAGE_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == Enums.GENERAL_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(context,
                        Manifest.permission.READ_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED
                ) openStorage();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Enums.VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            assert data != null;
            etSearch.setText(data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0));

            filterExpos();
        } else if (requestCode == Enums.PICK_IMAGE_REQUEST_CODE && resultCode == RESULT_OK &&
                data != null && data.getData() != null) {
            expoFormWithDivisionDialog.setImageData(data.getData());
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