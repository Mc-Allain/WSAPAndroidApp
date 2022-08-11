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

import com.example.wsapandroidapp.Adapters.AdminExhibitorAdapter;
import com.example.wsapandroidapp.Classes.ComponentManager;
import com.example.wsapandroidapp.Classes.Enums;
import com.example.wsapandroidapp.DataModel.CategoryImage;
import com.example.wsapandroidapp.DataModel.Exhibitor;
import com.example.wsapandroidapp.DialogClasses.CategoryImageDialog;
import com.example.wsapandroidapp.DialogClasses.ConfirmationDialog;
import com.example.wsapandroidapp.DialogClasses.ExhibitorFormDialog;
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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AdminExhibitorsActivity extends AppCompatActivity {

    EditText etSearch;
    ImageView imgAdd;
    TextView tvActivityTitle, tvMessage, tvCategory;
    ProgressBar pbLoading2;
    RecyclerView recyclerView;

    Context context;

    LoadingDialog loadingDialog;
    MessageDialog messageDialog;
    ConfirmationDialog confirmationDialog;
    CategoryImageDialog categoryImageDialog;
    ExhibitorFormDialog exhibitorFormDialog;

    FirebaseDatabase firebaseDatabase;

    Query exhibitorCategoriesQuery, exhibitorsQuery;

    boolean isListening;

    List<CategoryImage> exhibitorCategories = new ArrayList<>();

    List<Exhibitor> exhibitors = new ArrayList<>(), exhibitorsCopy = new ArrayList<>();

    AdminExhibitorAdapter adminExhibitorAdapter;

    ComponentManager componentManager;

    String searchExhibitor = "", selectedCategoryId = "";

    Exhibitor selectedExhibitor = new Exhibitor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_exhibitors);

        etSearch = findViewById(R.id.etSearch);
        imgAdd = findViewById(R.id.imgAdd);
        tvActivityTitle = findViewById(R.id.tvActivityTitle);
        tvMessage = findViewById(R.id.tvMessage);
        tvCategory = findViewById(R.id.tvCategory);
        pbLoading2 = findViewById(R.id.pbLoading2);
        recyclerView = findViewById(R.id.recyclerView);

        context = AdminExhibitorsActivity.this;

        loadingDialog = new LoadingDialog(context);
        messageDialog = new MessageDialog(context);
        confirmationDialog = new ConfirmationDialog(context);

        confirmationDialog.setDialogListener(() -> {
            exhibitorFormDialog.deleteExhibitor(selectedExhibitor, true);

            confirmationDialog.dismissDialog();
        });

        exhibitorFormDialog = new ExhibitorFormDialog(context);

        exhibitorFormDialog.setDirectory("exhibitors");
        exhibitorFormDialog.setDialogListener(() -> {
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

        categoryImageDialog = new CategoryImageDialog(context);

        categoryImageDialog.setDialogListener(categoryImage -> {
            selectedExhibitor.setCategory(categoryImage.getId());
            moveCategory();
        });

        selectedCategoryId = getIntent().getStringExtra("categoryId");

        exhibitorFormDialog.setCategoryId(selectedCategoryId);

        firebaseDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_RTDB_url));
        exhibitorCategoriesQuery = firebaseDatabase.getReference("exhibitorCategories").orderByChild("category");
        exhibitorsQuery = firebaseDatabase.getReference("exhibitors").orderByChild("exhibitor");

        pbLoading2.setVisibility(View.VISIBLE);
        isListening = true;
        etSearch.setVisibility(View.INVISIBLE);
        exhibitorCategoriesQuery.addValueEventListener(getExhibitorCategories());

        exhibitorFormDialog.setDatabaseReference(exhibitorsQuery.getRef());
        categoryImageDialog.setQuery(exhibitorCategoriesQuery);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        adminExhibitorAdapter = new AdminExhibitorAdapter(context, exhibitors);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adminExhibitorAdapter);

        adminExhibitorAdapter.setAdapterListener(new AdminExhibitorAdapter.AdapterListener() {
            @Override
            public void onClick(Exhibitor exhibitor) {
                Intent intent = new Intent(context, AdminExhibitorDetailsActivity.class);
                intent.putExtra("exhibitorId", exhibitor.getId());
                context.startActivity(intent);
            }

            @Override
            public void onEdit(Exhibitor exhibitor) {
                exhibitorFormDialog.setUpdateMode(true);
                exhibitorFormDialog.setExhibitor(exhibitor);
                exhibitorFormDialog.showDialog();
            }

            @Override
            public void onDelete(Exhibitor exhibitor) {
                selectedExhibitor = exhibitor;
                confirmationDialog.setMessage(getString(R.string.confirmation_prompt, "delete the exhibitor"));
                confirmationDialog.showDialog();
            }

            @Override
            public void onMove(Exhibitor exhibitor) {
                selectedExhibitor = exhibitor;
                categoryImageDialog.showDialog();
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
                searchExhibitor = editable != null ? editable.toString() : "";

                filterExhibitors();
            }
        });

        imgAdd.setOnClickListener(view -> {
            exhibitorFormDialog.setUpdateMode(false);
            exhibitorFormDialog.showDialog();
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void filterExhibitors() {
        List<Exhibitor> exhibitorsTemp = new ArrayList<>(exhibitorsCopy);

        exhibitors.clear();

        for (int i = 0; i < exhibitorsTemp.size(); i++) {
            Exhibitor exhibitor = exhibitorsTemp.get(i);

            boolean isFiltered = selectedCategoryId == null ||
                    selectedCategoryId.trim().length() == 0 ||
                    exhibitor.getCategory().equals(selectedCategoryId);

            boolean isSearched = searchExhibitor.trim().length() == 0 ||
                    exhibitor.getExhibitor().toLowerCase().contains(searchExhibitor.toLowerCase());

            if (isFiltered && isSearched) exhibitors.add(exhibitor);
        }

        if (selectedCategoryId != null) {
            CategoryImage categoryImage = new CategoryImage();
            for (CategoryImage categoryImage1 : exhibitorCategories)
                if (selectedCategoryId.equals(categoryImage1.getId()))
                    categoryImage = categoryImage1;

            tvActivityTitle.setText(categoryImage.getCategory());
        } else tvActivityTitle.setText(getString(R.string.exhibitors));

        if (exhibitors.size() == 0) {
            tvMessage.setVisibility(View.VISIBLE);

            tvMessage.setText(getString(R.string.no_record, "Record"));
        } else tvMessage.setVisibility(View.GONE);
        tvMessage.bringToFront();

        pbLoading2.setVisibility(View.GONE);
        etSearch.setVisibility(View.VISIBLE);

        adminExhibitorAdapter.notifyDataSetChanged();
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

                messageDialog.setMessage(getString(R.string.fd_on_cancelled, "SupplierCategories"));
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

    private void moveCategory() {
        loadingDialog.showDialog();

        exhibitorsQuery.getRef().child(selectedExhibitor.getId())
                .setValue(selectedExhibitor).addOnCompleteListener(task -> {
                    loadingDialog.dismissDialog();

                    if (task.isSuccessful()) {
                        categoryImageDialog.dismissDialog();

                        messageDialog.setMessageType(Enums.SUCCESS_MESSAGE);
                        messageDialog.setMessage("Successfully moved the exhibitor to another category.");
                    } else {
                        messageDialog.setMessageType(Enums.ERROR_MESSAGE);
                        messageDialog.setMessage("Failed to moved the exhibitor to another category.");
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

            filterExhibitors();
        } else if (requestCode == Enums.PICK_IMAGE_REQUEST_CODE && resultCode == RESULT_OK &&
                data != null && data.getData() != null) {
            exhibitorFormDialog.setImageData(data.getData());
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