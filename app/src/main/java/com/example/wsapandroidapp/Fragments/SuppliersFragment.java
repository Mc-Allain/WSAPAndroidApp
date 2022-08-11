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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.wsapandroidapp.Adapters.SupplierAdapter;
import com.example.wsapandroidapp.Adapters.CategoryImageAdapter;
import com.example.wsapandroidapp.Classes.ComponentManager;
import com.example.wsapandroidapp.Classes.Credentials;
import com.example.wsapandroidapp.Classes.Enums;
import com.example.wsapandroidapp.DataModel.Chapter;
import com.example.wsapandroidapp.DataModel.Supplier;
import com.example.wsapandroidapp.DataModel.CategoryImage;
import com.example.wsapandroidapp.DialogClasses.MessageDialog;
import com.example.wsapandroidapp.R;
import com.example.wsapandroidapp.SupplierDetailsActivity;
import com.example.wsapandroidapp.SuppliersActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static android.app.Activity.RESULT_OK;

public class SuppliersFragment extends Fragment {

    EditText etSearch;
    TextView tvMessage, tvCategory;
    RecyclerView recyclerView, recyclerView1;
    ConstraintLayout constraintLayout;
    ProgressBar pbLoading2;

    Context context;

    MessageDialog messageDialog;

    FirebaseDatabase firebaseDatabase;

    Query supplierCategoriesQuery, chaptersQuery, suppliersQuery;

    boolean isListening;

    List<CategoryImage> supplierCategories = new ArrayList<>();

    List<Supplier> suppliers = new ArrayList<>(), suppliersCopy = new ArrayList<>();

    CategoryImageAdapter categoryImageAdapter;

    SupplierAdapter supplierAdapter;

    List<Chapter> chapters = new ArrayList<>();

    ComponentManager componentManager;

    String searchSupplier = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_suppliers, container, false);

        etSearch = view.findViewById(R.id.etSearch);
        tvMessage = view.findViewById(R.id.tvMessage);
        tvCategory = view.findViewById(R.id.tvCategory);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView1 = view.findViewById(R.id.recyclerView1);
        constraintLayout = view.findViewById(R.id.constraintLayout);
        pbLoading2 = view.findViewById(R.id.pbLoading2);

        context = requireContext();

        messageDialog = new MessageDialog(context);

        firebaseDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_RTDB_url));
        supplierCategoriesQuery = firebaseDatabase.getReference("supplierCategories").orderByChild("category");
        chaptersQuery = firebaseDatabase.getReference("chapters").orderByChild("chapter");
        suppliersQuery = firebaseDatabase.getReference("suppliers").orderByChild("supplier");

        pbLoading2.setVisibility(View.VISIBLE);
        isListening = true;
        etSearch.setVisibility(View.INVISIBLE);
        supplierCategoriesQuery.addValueEventListener(getSupplierCategories());

        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false);
        categoryImageAdapter = new CategoryImageAdapter(context, supplierCategories);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(categoryImageAdapter);

        categoryImageAdapter.setAdapterListener(categoryImage -> {
            Intent intent = new Intent(context, SuppliersActivity.class);
            intent.putExtra("categoryId", categoryImage.getId());
            context.startActivity(intent);
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        supplierAdapter = new SupplierAdapter(context, suppliers, chapters);
        recyclerView1.setLayoutManager(linearLayoutManager);
        recyclerView1.setAdapter(supplierAdapter);

        supplierAdapter.setAdapterListener(supplier -> {
            Intent intent = new Intent(context, SupplierDetailsActivity.class);
            intent.putExtra("supplierId", supplier.getId());
            context.startActivity(intent);
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
                searchSupplier = editable != null ? editable.toString() : "";

                if (Credentials.fullTrim(searchSupplier).length() > 0)
                    constraintLayout.setVisibility(View.VISIBLE);
                else constraintLayout.setVisibility(View.GONE);

                filterSuppliers();
            }
        });

        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void filterSuppliers() {
        List<Supplier> suppliersTemp = new ArrayList<>(suppliersCopy);

        suppliers.clear();

        for (int i = 0; i < suppliersTemp.size(); i++) {
            Supplier supplier = suppliersTemp.get(i);

            boolean isSearched = searchSupplier.trim().length() == 0 ||
                    supplier.getSupplier().toLowerCase().contains(searchSupplier.toLowerCase());

            if (isSearched) suppliers.add(supplier);
        }

        if (suppliers.size() == 0) {
            tvMessage.setVisibility(View.VISIBLE);

            tvMessage.setText(getString(R.string.no_record, "Record"));
        } else tvMessage.setVisibility(View.GONE);
        tvMessage.bringToFront();
        constraintLayout.bringToFront();

        pbLoading2.setVisibility(View.GONE);
        etSearch.setVisibility(View.VISIBLE);

        categoryImageAdapter.notifyDataSetChanged();
        supplierAdapter.notifyDataSetChanged();
    }

    private ValueEventListener getSupplierCategories() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    supplierCategories.clear();

                    if (snapshot.exists())
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            CategoryImage categoryImage = dataSnapshot.getValue(CategoryImage.class);

                            if (categoryImage != null) supplierCategories.add(categoryImage);
                        }

                    chaptersQuery.addValueEventListener(getChapters());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "onCancelled", error.toException());
                pbLoading2.setVisibility(View.GONE);

                messageDialog.setMessage(getString(R.string.fd_on_cancelled, "Supplier Categories"));
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

                    if (snapshot.exists())
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Chapter chapter = dataSnapshot.getValue(Chapter.class);

                            if (chapter != null) chapters.add(chapter);
                        }

                    suppliersQuery.addValueEventListener(getSuppliers());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "onCancelled", error.toException());
                pbLoading2.setVisibility(View.GONE);

                messageDialog.setMessage(getString(R.string.fd_on_cancelled, "Chapters"));
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

                    suppliersCopy = new ArrayList<>(suppliers);

                    filterSuppliers();
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
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Enums.VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            assert data != null;
            etSearch.setText(data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0));

            filterSuppliers();
        }
    }

    @Override
    public void onResume() {
        isListening = true;
        supplierCategoriesQuery.addListenerForSingleValueEvent(getSupplierCategories());

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