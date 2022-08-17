package com.example.wsapandroidapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.example.wsapandroidapp.DialogClasses.LoadingDialog;
import com.example.wsapandroidapp.DialogClasses.MessageDialog;
import com.example.wsapandroidapp.DialogClasses.NewVersionPromptDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ExpoDetailsActivity extends AppCompatActivity {

    ImageView imgPoster;
    TextView tvExpo, tvDivision, tvSchedule, tvDescription, tvExhibitors;
    ConstraintLayout exhibitorsLayout;
    RecyclerView recyclerView;
    Button btnVisitWSAP;

    Context context;

    LoadingDialog loadingDialog;
    MessageDialog messageDialog;

    FirebaseDatabase firebaseDatabase;

    Query divisionsQuery, expoQuery, expoExhibitorsQuery, query;

    boolean isListening;

    Expo expo;
    Division division;

    ExpoExhibitorAdapter expoExhibitorAdapter;

    List<ExpoExhibitor> expoExhibitors = new ArrayList<>(),
            selectedExpoExhibitors = new ArrayList<>();
    List<ExpoExhibitor2> expoExhibitors2 = new ArrayList<>();

    String selectedExpoId = "";

    AppStatusPromptDialog appStatusPromptDialog;
    NewVersionPromptDialog newVersionPromptDialog;
    Query applicationQuery;
    Application application = new Application();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expo_details);

        imgPoster = findViewById(R.id.imgPoster);
        tvExpo = findViewById(R.id.tvExpo);
        tvDivision = findViewById(R.id.tvDivision);
        tvSchedule = findViewById(R.id.tvSchedule);
        tvDescription = findViewById(R.id.tvDescription);
        tvExhibitors = findViewById(R.id.tvExhibitors);
        exhibitorsLayout = findViewById(R.id.exhibitorsLayout);
        recyclerView = findViewById(R.id.recyclerView);
        btnVisitWSAP = findViewById(R.id.btnVisitWSAP);

        context = ExpoDetailsActivity.this;

        loadingDialog = new LoadingDialog(context);
        messageDialog = new MessageDialog(context);
        appStatusPromptDialog = new AppStatusPromptDialog(context);
        newVersionPromptDialog = new NewVersionPromptDialog(context);

        selectedExpoId = getIntent().getStringExtra("expoId");

        initDatabaseQuery();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        expoExhibitorAdapter = new ExpoExhibitorAdapter(context, selectedExpoExhibitors);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(expoExhibitorAdapter);

        expoExhibitorAdapter.setAdapterListener(expoExhibitor -> {
            Intent intent = new Intent(context, ExhibitorDetailsActivity.class);
            if (expoExhibitor.isMember()) {
                intent = new Intent(context, SupplierDetailsActivity.class);
                intent.putExtra("supplierId", expoExhibitor.getId());
            } else intent.putExtra("exhibitorId", expoExhibitor.getId());
            startActivity(intent);
        });

        imgPoster.setOnClickListener(view -> {
            Intent intent = new Intent(context, ImageActivity.class);
            intent.putExtra("image", expo.getImage());
            startActivity(intent);
        });

        btnVisitWSAP.setOnClickListener(view -> {
            Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(getString(R.string.expo_url)));
            startActivity(intent);
        });
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
        exhibitorsLayout.setVisibility(View.GONE);
        expoQuery.addValueEventListener(getExpos());
        applicationQuery.addValueEventListener(getApplicationValue());
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
                    if (snapshot.exists())
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Division expoDivision = dataSnapshot.getValue(Division.class);

                            if (expoDivision != null && expoDivision.getId().equals(expo.getDivision())) {
                                division = expoDivision;
                                break;
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
                    expoExhibitors2.clear();

                    if (snapshot.exists())
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            ExpoExhibitors expoExhibitors = dataSnapshot.getValue(ExpoExhibitors.class);
                            if (expoExhibitors != null)
                                expoExhibitors2.addAll(expoExhibitors.getExpoExhibitors().values());
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
                    expoExhibitors.clear();
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

                                expoExhibitors.add(expoExhibitor);

                                for (ExpoExhibitor2 expoExhibitor2 : expoExhibitors2)
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

                                expoExhibitors.add(expoExhibitor);

                                for (ExpoExhibitor2 expoExhibitor2 : expoExhibitors2)
                                    if (expoExhibitor2.getId().equals(expoExhibitor.getId()))
                                        selectedExpoExhibitors.add(expoExhibitor);
                            }
                        }
                    }

                    expoExhibitors.sort((expoExhibitor2, t1) ->
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

        if (selectedExpoExhibitors.size() == 0) {
            exhibitorsLayout.setVisibility(View.GONE);
            tvExhibitors.setVisibility(View.GONE);
        } else {
            exhibitorsLayout.setVisibility(View.VISIBLE);
            tvExhibitors.setVisibility(View.VISIBLE);
        }
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