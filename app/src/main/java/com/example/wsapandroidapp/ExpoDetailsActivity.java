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
import com.example.wsapandroidapp.DataModel.Division;
import com.example.wsapandroidapp.DataModel.Exhibitor;
import com.example.wsapandroidapp.DataModel.Expo;
import com.example.wsapandroidapp.DataModel.ExpoExhibitor;
import com.example.wsapandroidapp.DataModel.ExpoExhibitor2;
import com.example.wsapandroidapp.DataModel.ExpoExhibitors;
import com.example.wsapandroidapp.DataModel.Supplier;
import com.example.wsapandroidapp.DialogClasses.LoadingDialog;
import com.example.wsapandroidapp.DialogClasses.MessageDialog;
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
    TextView tvExpo, tvDivision, tvSchedule, tvDescription;
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

    List<ExpoExhibitor> expoExhibitors = new ArrayList<>();
    List<ExpoExhibitor2> expoExhibitors2 = new ArrayList<>(),
            selectedExpoExhibitors = new ArrayList<>();

    String selectedExpoId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expo_details);

        imgPoster = findViewById(R.id.imgPoster);
        tvExpo = findViewById(R.id.tvExpo);
        tvDivision = findViewById(R.id.tvDivision);
        tvSchedule = findViewById(R.id.tvSchedule);
        tvDescription = findViewById(R.id.tvDescription);
        exhibitorsLayout = findViewById(R.id.exhibitorsLayout);
        recyclerView = findViewById(R.id.recyclerView);
        btnVisitWSAP = findViewById(R.id.btnVisitWSAP);

        context = ExpoDetailsActivity.this;

        loadingDialog = new LoadingDialog(context);
        messageDialog = new MessageDialog(context);

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

        loadingDialog.showDialog();
        isListening = true;
        exhibitorsLayout.setVisibility(View.GONE);
        expoQuery.addValueEventListener(getExpos());
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
                    expoExhibitors.clear();

                    if (snapshot.exists())
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            ExpoExhibitors expoExhibitors2 = dataSnapshot.getValue(ExpoExhibitors.class);
                            if (expoExhibitors2 != null)
                                expoExhibitors.addAll(expoExhibitors2.getExpoExhibitors().values());
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
                                ExpoExhibitor2 expoExhibitor2 =
                                        new ExpoExhibitor2(
                                                exhibitor.getId(),
                                                exhibitor.getExhibitor(),
                                                exhibitor.getImage(),
                                                false
                                        );

                                expoExhibitors2.add(expoExhibitor2);

                                for (ExpoExhibitor expoExhibitor : expoExhibitors)
                                    if (expoExhibitor.getId().equals(expoExhibitor2.getId()))
                                        selectedExpoExhibitors.add(expoExhibitor2);
                            }
                        }

                        for (DataSnapshot dataSnapshot : snapshot.child("suppliers").getChildren()) {
                            Supplier supplier = dataSnapshot.getValue(Supplier.class);

                            if (supplier != null) {
                                ExpoExhibitor2 expoExhibitor2 =
                                        new ExpoExhibitor2(
                                                supplier.getId(),
                                                supplier.getSupplier(),
                                                supplier.getImage(),
                                                true
                                        );

                                expoExhibitors2.add(expoExhibitor2);

                                for (ExpoExhibitor expoExhibitor : expoExhibitors)
                                    if (expoExhibitor.getId().equals(expoExhibitor2.getId()))
                                        selectedExpoExhibitors.add(expoExhibitor2);
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

    private void updateUI() {
        Glide.with(context).load(expo.getImage()).centerCrop().placeholder(R.drawable.ic_wsap).
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

        if (expoExhibitors2.size() == 0) exhibitorsLayout.setVisibility(View.GONE);
        else exhibitorsLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        isListening = true;
        expoQuery.addListenerForSingleValueEvent(getExpos());

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