package com.example.wsapandroidapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.wsapandroidapp.Adapters.ExpoExhibitorAdapter;
import com.example.wsapandroidapp.Classes.DateTime;
import com.example.wsapandroidapp.Classes.Enums;
import com.example.wsapandroidapp.DataModel.Application;
import com.example.wsapandroidapp.DataModel.Division;
import com.example.wsapandroidapp.DataModel.Exhibitor;
import com.example.wsapandroidapp.DataModel.Expo;
import com.example.wsapandroidapp.DataModel.ExpoExhibitor2;
import com.example.wsapandroidapp.DataModel.ExpoExhibitor;
import com.example.wsapandroidapp.DataModel.ExpoExhibitors;
import com.example.wsapandroidapp.DataModel.Supplier;
import com.example.wsapandroidapp.DialogClasses.AppStatusPromptDialog;
import com.example.wsapandroidapp.DialogClasses.ExhibitorSelectionDialog;
import com.example.wsapandroidapp.DialogClasses.ExpoDivisionDialog;
import com.example.wsapandroidapp.DialogClasses.ExpoFormDialog;
import com.example.wsapandroidapp.DialogClasses.LoadingDialog;
import com.example.wsapandroidapp.DialogClasses.MessageDialog;
import com.example.wsapandroidapp.DialogClasses.NewVersionPromptDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AdminExpoDetailsActivity extends AppCompatActivity {

    ImageView imgPoster, imgUpdateExpo, imgMove, imgUpdateExhibitors;
    TextView tvExpo, tvDivision, tvSchedule, tvDescription;
    ConstraintLayout exhibitorsLayout;
    RecyclerView recyclerView;

    Context context;

    LoadingDialog loadingDialog;
    MessageDialog messageDialog;
    ExpoFormDialog expoFormDialog;
    ExpoDivisionDialog expoDivisionDialog;
    ExhibitorSelectionDialog exhibitorSelectionDialog;

    FirebaseDatabase firebaseDatabase;

    Query divisionsQuery, expoQuery, expoExhibitorsQuery, query;

    boolean isListening;

    List<Division> divisions = new ArrayList<>();

    Expo expo;
    Division division;

    ExpoExhibitorAdapter expoExhibitorAdapter;

    List<ExpoExhibitor2> expoExhibitor2s = new ArrayList<>();
    List<ExpoExhibitor> expoExhibitors2 = new ArrayList<>(),
            selectedExpoExhibitors = new ArrayList<>();

    String selectedExpoId = "";

    AppStatusPromptDialog appStatusPromptDialog;
    NewVersionPromptDialog newVersionPromptDialog;
    Query applicationQuery;
    Application application = new Application();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_expo_details);

        imgPoster = findViewById(R.id.imgPoster);
        imgUpdateExpo = findViewById(R.id.imgUpdateExpo);
        imgMove = findViewById(R.id.imgMove);
        imgUpdateExhibitors = findViewById(R.id.imgUpdateExhibitors);
        tvExpo = findViewById(R.id.tvExpo);
        tvDivision = findViewById(R.id.tvDivision);
        tvSchedule = findViewById(R.id.tvSchedule);
        tvDescription = findViewById(R.id.tvDescription);
        exhibitorsLayout = findViewById(R.id.exhibitorsLayout);
        recyclerView = findViewById(R.id.recyclerView);

        context = AdminExpoDetailsActivity.this;

        loadingDialog = new LoadingDialog(context);
        messageDialog = new MessageDialog(context);

        expoFormDialog = new ExpoFormDialog(context);

        expoFormDialog.setDirectory("expos");
        expoFormDialog.setDialogListener(() -> {
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

        selectedExpoId = getIntent().getStringExtra("expoId");

        expoDivisionDialog = new ExpoDivisionDialog(context);

        expoDivisionDialog.setDialogListener(division -> {
            expo.setDivision(division.getId());
            moveDivision();
        });

        exhibitorSelectionDialog = new ExhibitorSelectionDialog(context);

        exhibitorSelectionDialog.setDialogListener(new ExhibitorSelectionDialog.DialogListener() {
            @Override
            public void activateVoiceRecognition() {
                startActivityForResult(exhibitorSelectionDialog.componentManager
                        .voiceRecognitionIntent(), Enums.VOICE_RECOGNITION_REQUEST_CODE);
            }

            @Override
            public void sendExhibitors(List<ExpoExhibitor> newSelectedExhibitors) {
                selectedExpoExhibitors.clear();
                selectedExpoExhibitors.addAll(newSelectedExhibitors);

                updateExhibitors();
            }
        });

        appStatusPromptDialog = new AppStatusPromptDialog(context);
        newVersionPromptDialog = new NewVersionPromptDialog(context);

        initDatabaseQuery();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        expoExhibitorAdapter = new ExpoExhibitorAdapter(context, selectedExpoExhibitors);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(expoExhibitorAdapter);

        expoExhibitorAdapter.setAdapterListener(expoExhibitor -> {
            Intent intent = new Intent(context, AdminExhibitorDetailsActivity.class);
            if (expoExhibitor.isMember()) {
                intent = new Intent(context, AdminSupplierDetailsActivity.class);
                intent.putExtra("supplierId", expoExhibitor.getId());
            } else intent.putExtra("exhibitorId", expoExhibitor.getId());
            startActivity(intent);
        });

        imgPoster.setOnClickListener(view -> {
            Intent intent = new Intent(context, ImageActivity.class);
            intent.putExtra("image", expo.getImage());
            startActivity(intent);
        });

        imgUpdateExpo.setOnClickListener(view -> {
            expoFormDialog.setUpdateMode(true);
            expoFormDialog.setExpo(expo);
            expoFormDialog.showDialog();
        });

        imgMove.setOnClickListener(view -> expoDivisionDialog.showDialog());

        imgUpdateExhibitors.setOnClickListener(view -> exhibitorSelectionDialog.showDialog());
    }

    private void initDatabaseQuery() {
        firebaseDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_RTDB_url));
        expoQuery = firebaseDatabase.getReference("expos").orderByChild("id").equalTo(selectedExpoId);
        divisionsQuery = firebaseDatabase.getReference("divisions");
        expoExhibitorsQuery = firebaseDatabase.getReference("expoExhibitors").orderByChild("id").equalTo(selectedExpoId);
        query = firebaseDatabase.getReference();
        applicationQuery = firebaseDatabase.getReference("application");

        loadingDialog.showDialog();
        isListening = true;
        expoQuery.addValueEventListener(getExpos());
        applicationQuery.addValueEventListener(getApplicationValue());

        expoFormDialog.setDatabaseReference(expoQuery.getRef());
    }

    private ValueEventListener getExpos() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    if (snapshot.exists())
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            expo = dataSnapshot.getValue(Expo.class);
                            break;
                        }

                    divisionsQuery.addValueEventListener(getDivisions());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "onCancelled", error.toException());
                loadingDialog.showDialog();

                messageDialog.setMessage(getString(R.string.fd_on_cancelled, "Expos"));
                messageDialog.setMessageType(Enums.ERROR_MESSAGE);
                messageDialog.showDialog();
            }
        };
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
                            Division expoDivision = dataSnapshot.getValue(Division.class);

                            if (expoDivision != null) {
                                divisions.add(expoDivision);

                                if (expoDivision.getId().equals(expo.getDivision()))
                                    division = expoDivision;
                            }
                        }

                    expoExhibitorsQuery.addValueEventListener(getExpoExhibitors());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "onCancelled", error.toException());
                loadingDialog.showDialog();

                messageDialog.setMessage(getString(R.string.fd_on_cancelled, "Divisions"));
                messageDialog.setMessageType(Enums.ERROR_MESSAGE);
                messageDialog.showDialog();
            }
        };
    }

    private ValueEventListener getExpoExhibitors() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    expoExhibitor2s.clear();

                    if (snapshot.exists())
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            ExpoExhibitors expoExhibitors2 = dataSnapshot.getValue(ExpoExhibitors.class);
                            if (expoExhibitors2 != null)
                                expoExhibitor2s.addAll(expoExhibitors2.getExpoExhibitors().values());
                            break;
                        }

                    query.addValueEventListener(getExhibitors());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "onCancelled", error.toException());
                loadingDialog.showDialog();

                messageDialog.setMessage(getString(R.string.fd_on_cancelled, "Expo Exhibitors"));
                messageDialog.setMessageType(Enums.ERROR_MESSAGE);
                messageDialog.showDialog();
            }
        };
    }

    private ValueEventListener getExhibitors() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    expoExhibitors2.clear();
                    selectedExpoExhibitors.clear();

                    if (snapshot.exists()) {
                        for (DataSnapshot dataSnapshot : snapshot.child("exhibitors").getChildren()) {
                            Exhibitor exhibitor = dataSnapshot.getValue(Exhibitor.class);

                            if (exhibitor != null) {
                                ExpoExhibitor expoExhibitor =
                                        new ExpoExhibitor(
                                                exhibitor.getId(),
                                                exhibitor.getExhibitor(),
                                                exhibitor.getImage(),
                                                false
                                        );

                                expoExhibitors2.add(expoExhibitor);

                                for (ExpoExhibitor2 expoExhibitor2 : expoExhibitor2s)
                                    if (expoExhibitor2.getId().equals(expoExhibitor.getId()))
                                        selectedExpoExhibitors.add(expoExhibitor);
                            }
                        }

                        for (DataSnapshot dataSnapshot : snapshot.child("suppliers").getChildren()) {
                            Supplier supplier = dataSnapshot.getValue(Supplier.class);

                            if (supplier != null) {
                                ExpoExhibitor expoExhibitor =
                                        new ExpoExhibitor(
                                                supplier.getId(),
                                                supplier.getSupplier(),
                                                supplier.getImage(),
                                                true
                                        );

                                expoExhibitors2.add(expoExhibitor);

                                for (ExpoExhibitor2 expoExhibitor2 : expoExhibitor2s)
                                    if (expoExhibitor2.getId().equals(expoExhibitor.getId()))
                                        selectedExpoExhibitors.add(expoExhibitor);
                            }
                        }
                    }

                    expoExhibitors2.sort((expoExhibitor2, t1) ->
                            expoExhibitor2.getExhibitor().compareToIgnoreCase(t1.getExhibitor()));

                    selectedExpoExhibitors.sort((expoExhibitor2, t1) ->
                            expoExhibitor2.getExhibitor().compareToIgnoreCase(t1.getExhibitor()));

                    loadingDialog.dismissDialog();

                    updateUI();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "onCancelled", error.toException());
                loadingDialog.showDialog();

                messageDialog.setMessage(getString(R.string.fd_on_cancelled, "Records"));
                messageDialog.setMessageType(Enums.ERROR_MESSAGE);
                messageDialog.showDialog();
            }
        };
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateUI() {
        Glide.with(context).load(expo.getImage()).placeholder(R.drawable.ic_wsap).
                error(R.drawable.ic_wsap).into(imgPoster);

        tvExpo.setText(expo.getExpo());
        tvDivision.setText(division.getDivision());

        String startDate, endDate;
        DateTime dateTime = new DateTime(expo.getDateTimeRange().getStartDateTime());
        startDate = dateTime.getDateText();
        dateTime.setDateTime(expo.getDateTimeRange().getEndDateTime());
        endDate = dateTime.getDateText();

        tvSchedule.setText(getString(R.string.expo_schedule, startDate, endDate));
        tvDescription.setText(expo.getDescription());

        expoFormDialog.setDivisionId(expo.getDivision());
        expoDivisionDialog.setDivisions(divisions);

        expoExhibitorAdapter.notifyDataSetChanged();

        exhibitorSelectionDialog.setExhibitors(expoExhibitors2, selectedExpoExhibitors);
    }

    private void moveDivision() {
        loadingDialog.showDialog();

        expoQuery.getRef().child(selectedExpoId)
                .setValue(expo).addOnCompleteListener(task -> {
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

    private void updateExhibitors() {
        loadingDialog.showDialog();

        Map<String, ExpoExhibitor2> exhibitors = new HashMap<>();

        for (ExpoExhibitor selectedExpoExhibitor : selectedExpoExhibitors) {
            ExpoExhibitor2 expoExhibitor2 =
                    new ExpoExhibitor2(
                            selectedExpoExhibitor.getId(),
                            selectedExpoExhibitor.isMember()
                    );

            exhibitors.put(selectedExpoExhibitor.getId(), expoExhibitor2);
        }

        ExpoExhibitors expoExhibitors2 = new ExpoExhibitors(expo.getId(), exhibitors);

        expoExhibitorsQuery.getRef().child(selectedExpoId)
                .setValue(expoExhibitors2).addOnCompleteListener(task -> {
                    loadingDialog.dismissDialog();

                    if (task.isSuccessful()) {
                        exhibitorSelectionDialog.dismissDialog();

                        messageDialog.setMessageType(Enums.SUCCESS_MESSAGE);
                        messageDialog.setMessage("Successfully updated the expo exhibitors.");
                    } else {
                        messageDialog.setMessageType(Enums.ERROR_MESSAGE);
                        messageDialog.setMessage("Failed to update the expo exhibitors.");
                    }

                    messageDialog.showDialog();
                });
    }

    private ValueEventListener getApplicationValue() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    appStatusPromptDialog.dismissDialog();
                    newVersionPromptDialog.dismissDialog();

                    if (snapshot.exists()) {
                        application = snapshot.getValue(Application.class);

                        if (application != null) {
                            if (!application.getStatus().equals(getString(R.string.live)) && !application.isForDeveloper()) {
                                appStatusPromptDialog.setTitle(application.getStatus());
                                appStatusPromptDialog.showDialog();
                            } else if (application.getCurrentVersion() < application.getLatestVersion()
                                    && !application.isForDeveloper()) {
                                newVersionPromptDialog.setVersion(application.getCurrentVersion(), application.getLatestVersion());
                                newVersionPromptDialog.showDialog();
                            }

                            appStatusPromptDialog.setDialogListener(() -> {
                                appStatusPromptDialog.dismissDialog();
                                finish();
                            });

                            newVersionPromptDialog.setDialogListener(new NewVersionPromptDialog.DialogListener() {
                                @Override
                                public void onUpdate() {
                                    Intent intent = new Intent("android.intent.action.VIEW",
                                            Uri.parse(application.getDownloadLink()));
                                    startActivity(intent);

                                    newVersionPromptDialog.dismissDialog();
                                    finish();
                                }

                                @Override
                                public void onCancel() {
                                    newVersionPromptDialog.dismissDialog();
                                    finish();
                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "onCancelled", error.toException());
            }
        };
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
            exhibitorSelectionDialog.etSearch.setText(data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0));
        } else if (requestCode == Enums.PICK_IMAGE_REQUEST_CODE && resultCode == RESULT_OK &&
                data != null && data.getData() != null) {
            expoFormDialog.setImageData(data.getData());
        }
    }

    @Override
    public void onResume() {
        isListening = true;
        expoQuery.addListenerForSingleValueEvent(getExpos());
        applicationQuery.addListenerForSingleValueEvent(getApplicationValue());

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