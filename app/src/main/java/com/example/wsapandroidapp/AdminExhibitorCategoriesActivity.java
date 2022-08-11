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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.wsapandroidapp.Adapters.AdminCategoryImageAdapter;
import com.example.wsapandroidapp.Adapters.ExhibitorAdapter;
import com.example.wsapandroidapp.Classes.ComponentManager;
import com.example.wsapandroidapp.Classes.Credentials;
import com.example.wsapandroidapp.Classes.Enums;
import com.example.wsapandroidapp.DataModel.CategoryImage;
import com.example.wsapandroidapp.DataModel.Exhibitor;
import com.example.wsapandroidapp.DialogClasses.CategoryImageFormDialog;
import com.example.wsapandroidapp.DialogClasses.ConfirmationDialog;
import com.example.wsapandroidapp.DialogClasses.MessageDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AdminExhibitorCategoriesActivity extends AppCompatActivity {

    EditText etSearch;
    ImageView imgAdd;
    TextView tvMessage, tvCategory;
    RecyclerView recyclerView, recyclerView1;
    ConstraintLayout constraintLayout;
    ProgressBar pbLoading2;

    Context context;

    MessageDialog messageDialog;
    ConfirmationDialog confirmationDialog;
    CategoryImageFormDialog categoryImageFormDialog;

    FirebaseDatabase firebaseDatabase;

    Query exhibitorCategoriesQuery, exhibitorsQuery;

    boolean isListening;

    List<CategoryImage> exhibitorCategories = new ArrayList<>();

    List<Exhibitor> exhibitors = new ArrayList<>(), exhibitorsCopy = new ArrayList<>();

    AdminCategoryImageAdapter adminCategoryImageAdapter;

    ExhibitorAdapter exhibitorAdapter;

    ComponentManager componentManager;

    String searchExhibitor = "";

    CategoryImage selectedCategoryImage = new CategoryImage();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_exhibitor_categories);

        etSearch = findViewById(R.id.etSearch);
        imgAdd = findViewById(R.id.imgAdd);
        tvMessage = findViewById(R.id.tvMessage);
        tvCategory = findViewById(R.id.tvCategory);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView1 = findViewById(R.id.recyclerView1);
        constraintLayout = findViewById(R.id.constraintLayout);
        pbLoading2 = findViewById(R.id.pbLoading2);

        context = AdminExhibitorCategoriesActivity.this;

        messageDialog = new MessageDialog(context);
        confirmationDialog = new ConfirmationDialog(context);

        confirmationDialog.setDialogListener(() -> {
            if (getRecordCount(selectedCategoryImage.getId()) == 0)
                categoryImageFormDialog.deleteCategory(selectedCategoryImage, true);
            else {
                messageDialog.setMessage(getString(R.string.delete_record_failed_msg,
                        "the category", "Please remove all of its records first"));
                messageDialog.setMessageType(Enums.ERROR_MESSAGE);
                messageDialog.showDialog();
            }

            confirmationDialog.dismissDialog();
        });

        categoryImageFormDialog = new CategoryImageFormDialog(context);

        categoryImageFormDialog.setDirectory("exhibitorCategories");
        categoryImageFormDialog.setDialogListener(() -> {
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

        firebaseDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_RTDB_url));
        exhibitorCategoriesQuery = firebaseDatabase.getReference("exhibitorCategories").orderByChild("category");
        exhibitorsQuery = firebaseDatabase.getReference("exhibitors").orderByChild("exhibitor");

        pbLoading2.setVisibility(View.VISIBLE);
        isListening = true;
        etSearch.setVisibility(View.INVISIBLE);
        exhibitorCategoriesQuery.addValueEventListener(getExhibitorCategories());

        categoryImageFormDialog.setDatabaseReference(exhibitorCategoriesQuery.getRef());

        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false);
        adminCategoryImageAdapter = new AdminCategoryImageAdapter(context, exhibitorCategories);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adminCategoryImageAdapter);

        adminCategoryImageAdapter.setAdapterListener(new AdminCategoryImageAdapter.AdapterListener() {
            @Override
            public void onClick(CategoryImage categoryImage) {
                Intent intent = new Intent(context, AdminExhibitorsActivity.class);
                intent.putExtra("categoryId", categoryImage.getId());
                context.startActivity(intent);
            }

            @Override
            public void update(CategoryImage categoryImage) {
                categoryImageFormDialog.setUpdateMode(true);
                categoryImageFormDialog.setCategoryImage(categoryImage);
                categoryImageFormDialog.showDialog();
            }

            @Override
            public void delete(CategoryImage categoryImage) {
                selectedCategoryImage = categoryImage;
                confirmationDialog.setMessage(getString(R.string.confirmation_prompt, "delete the category"));
                confirmationDialog.showDialog();
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        exhibitorAdapter = new ExhibitorAdapter(context, exhibitors);
        recyclerView1.setLayoutManager(linearLayoutManager);
        recyclerView1.setAdapter(exhibitorAdapter);

        exhibitorAdapter.setAdapterListener(exhibitor -> {
            Intent intent = new Intent(context, AdminExhibitorDetailsActivity.class);
            intent.putExtra("exhibitorId", exhibitor.getId());
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
                searchExhibitor = editable != null ? editable.toString() : "";

                if (Credentials.fullTrim(searchExhibitor).length() > 0)
                    constraintLayout.setVisibility(View.VISIBLE);
                else constraintLayout.setVisibility(View.GONE);

                filterExhibitors();
            }
        });

        imgAdd.setOnClickListener(view -> {
            categoryImageFormDialog.setUpdateMode(false);
            categoryImageFormDialog.showDialog();
        });
    }

    private int getRecordCount(String categoryId) {
        int count = 0;

        for (Exhibitor exhibitor : exhibitors) {
            boolean isFiltered = exhibitor.getCategory().equals(categoryId);

            if (isFiltered) count++;
        }

        return count;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void filterExhibitors() {
        List<Exhibitor> exhibitorsTemp = new ArrayList<>(exhibitorsCopy);

        exhibitors.clear();

        for (int i = 0; i < exhibitorsTemp.size(); i++) {
            Exhibitor exhibitor = exhibitorsTemp.get(i);

            boolean isSearched = searchExhibitor.trim().length() == 0 ||
                    exhibitor.getExhibitor().toLowerCase().contains(searchExhibitor.toLowerCase());

            if (isSearched) exhibitors.add(exhibitor);
        }

        if (exhibitors.size() == 0) {
            tvMessage.setVisibility(View.VISIBLE);

            tvMessage.setText(getString(R.string.no_record, "Record"));
        } else tvMessage.setVisibility(View.GONE);
        tvMessage.bringToFront();
        constraintLayout.bringToFront();

        pbLoading2.setVisibility(View.GONE);
        etSearch.setVisibility(View.VISIBLE);

        adminCategoryImageAdapter.notifyDataSetChanged();
        exhibitorAdapter.notifyDataSetChanged();
    }

    private ValueEventListener getExhibitorCategories() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    exhibitorCategories.clear();

                    if (snapshot.exists())
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            CategoryImage categoryImage = dataSnapshot.getValue(CategoryImage.class);

                            if (categoryImage != null) exhibitorCategories.add(categoryImage);
                        }

                    exhibitorsQuery.addValueEventListener(getExhibitors());
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

    private ValueEventListener getExhibitors() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    exhibitors.clear();

                    if (snapshot.exists())
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Exhibitor exhibitor = dataSnapshot.getValue(Exhibitor.class);

                            if (exhibitor != null) exhibitors.add(exhibitor);
                        }

                    exhibitorsCopy = new ArrayList<>(exhibitors);

                    filterExhibitors();
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

            filterExhibitors();
        } else if (requestCode == Enums.PICK_IMAGE_REQUEST_CODE && resultCode == RESULT_OK &&
                data != null && data.getData() != null) {
            categoryImageFormDialog.setImageData(data.getData());
        }
    }

    @Override
    public void onResume() {
        isListening = true;
        exhibitorCategoriesQuery.addListenerForSingleValueEvent(getExhibitorCategories());

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