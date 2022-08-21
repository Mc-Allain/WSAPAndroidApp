package com.example.wsapandroidapp.Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.wsapandroidapp.Classes.Enums;
import com.example.wsapandroidapp.Classes.Units;
import com.example.wsapandroidapp.DataModel.Expo;
import com.example.wsapandroidapp.DataModel.Supplier;
import com.example.wsapandroidapp.DialogClasses.MessageDialog;
import com.example.wsapandroidapp.ExpoDetailsActivity;
import com.example.wsapandroidapp.ExposActivity;
import com.example.wsapandroidapp.R;
import com.example.wsapandroidapp.SupplierDetailsActivity;
import com.example.wsapandroidapp.SuppliersActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {

    ConstraintLayout constraintLayout;
    ProgressBar pbLoading2;
    CardView upcomingExposCard, featuredWeddingTipCard, featuredSupplierCard, featuredTriviaCard;
    TextView tvUpcomingExpo, tvFeaturedSupplier;
    Button btnViewAllExpos, btnViewAllWeddingTips, btnViewAllSuppliers, btnViewAllTrivia;

    Context context;

    MessageDialog messageDialog;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;

    Query exposQuery, suppliersQuery;

    boolean isListening;

    List<Expo> expos = new ArrayList<>();
    List<Supplier> suppliers = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        constraintLayout = view.findViewById(R.id.constraintLayout);
        pbLoading2 = view.findViewById(R.id.pbLoading2);

        upcomingExposCard = view.findViewById(R.id.upcomingExposCard);
        featuredWeddingTipCard = view.findViewById(R.id.featuredWeddingTipCard);
        featuredSupplierCard = view.findViewById(R.id.featuredSupplierCard);
        featuredTriviaCard = view.findViewById(R.id.featuredTriviaCard);

        tvUpcomingExpo = view.findViewById(R.id.tvUpcomingExpo);
        tvFeaturedSupplier = view.findViewById(R.id.tvFeaturedSupplier);

        btnViewAllExpos = view.findViewById(R.id.btnViewAllExpos);
        btnViewAllWeddingTips = view.findViewById(R.id.btnViewAllWeddingTips);
        btnViewAllSuppliers = view.findViewById(R.id.btnViewAllSuppliers);
        btnViewAllTrivia = view.findViewById(R.id.btnViewAllTrivia);

        context = getContext();

        messageDialog = new MessageDialog(context);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_RTDB_url));
        exposQuery = firebaseDatabase.getReference("expos");
        suppliersQuery = firebaseDatabase.getReference("suppliers").orderByChild("supplier");

        isListening = true;
        pbLoading2.setVisibility(View.VISIBLE);
        constraintLayout.setVisibility(View.GONE);
        exposQuery.addValueEventListener(getExpos());

        btnViewAllExpos.setOnClickListener(view1 -> {
            Intent intent = new Intent(context, ExposActivity.class);
            startActivity(intent);
        });

        btnViewAllWeddingTips.setOnClickListener(view1 -> {
            messageDialog.setMessage(getString(R.string.coming_soon));
            messageDialog.setMessageType(Enums.INFO_MESSAGE);
            messageDialog.showDialog();
        });

        btnViewAllSuppliers.setOnClickListener(view1 -> {
            Intent intent = new Intent(context, SuppliersActivity.class);
            startActivity(intent);
        });

        return view;
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

                            if (expo != null) {
                                boolean isUpcomingExpo =
                                        expo.getDateTimeRange().getEndDateTime() >= new Date().getTime();

                                if (isUpcomingExpo) expos.add(expo);
                            }
                        }

                    expos.sort((expo, t1) ->
                            (int) (Units.msToDay(expo.getDateTimeRange().getStartDateTime()) - Units.msToDay(t1.getDateTimeRange().getStartDateTime()))
                    );

                    if (expos.size() == 0)
                        tvUpcomingExpo.setText(getString(R.string.no_record, "Upcoming Expo"));
                    else {
                        Expo expo = expos.get(0);
                        tvUpcomingExpo.setText(expo.getExpo());

                        upcomingExposCard.setOnClickListener(view -> {
                            Intent intent = new Intent(context, ExpoDetailsActivity.class);
                            intent.putExtra("expoId", expo.getId());
                            context.startActivity(intent);
                        });
                    }

                    suppliersQuery.addValueEventListener(getSuppliers());
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

    private ValueEventListener getSuppliers() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    suppliers.clear();

                    if (snapshot.exists())
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Supplier supplier = dataSnapshot.getValue(Supplier.class);

                            if (supplier != null) suppliers.add(supplier);
                        }

                    if (suppliers.size() == 0)
                        tvUpcomingExpo.setText(getString(R.string.no_record, "Featured Supplier"));
                    else {
                        Random rnd = new Random();

                        Supplier supplier = suppliers.get(rnd.nextInt(suppliers.size() - 1));
                        tvFeaturedSupplier.setText(supplier.getSupplier());

                        featuredSupplierCard.setOnClickListener(view -> {
                            Intent intent = new Intent(context, SupplierDetailsActivity.class);
                            intent.putExtra("supplierId", supplier.getId());
                            context.startActivity(intent);
                        });
                    }

                    pbLoading2.setVisibility(View.GONE);
                    constraintLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "onCancelled", error.toException());
                pbLoading2.setVisibility(View.GONE);

                messageDialog.setMessage(getString(R.string.fd_on_cancelled, "Suppliers"));
                messageDialog.setMessageType(Enums.ERROR_MESSAGE);
                messageDialog.showDialog();
            }
        };
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