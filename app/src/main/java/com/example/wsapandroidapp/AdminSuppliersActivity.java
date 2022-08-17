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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.wsapandroidapp.Adapters.AdminSupplierAdapter;
import com.example.wsapandroidapp.Classes.ComponentManager;
import com.example.wsapandroidapp.Classes.Enums;
import com.example.wsapandroidapp.DataModel.Application;
import com.example.wsapandroidapp.DataModel.CategoryImage;
import com.example.wsapandroidapp.DataModel.Chapter;
import com.example.wsapandroidapp.DataModel.Supplier;
import com.example.wsapandroidapp.DialogClasses.AppStatusPromptDialog;
import com.example.wsapandroidapp.DialogClasses.CategoryImageDialog;
import com.example.wsapandroidapp.DialogClasses.ConfirmationDialog;
import com.example.wsapandroidapp.DialogClasses.LoadingDialog;
import com.example.wsapandroidapp.DialogClasses.MessageDialog;
import com.example.wsapandroidapp.DialogClasses.NewVersionPromptDialog;
import com.example.wsapandroidapp.DialogClasses.SupplierFormDialog;
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

public class AdminSuppliersActivity extends AppCompatActivity {

    EditText etSearch;
    ImageView imgAdd;
    TextView tvActivityTitle, tvMessage, tvCategory;
    ProgressBar pbLoading2;
    RecyclerView recyclerView;

    Context context;

    LoadingDialog loadingDialog;
    MessageDialog messageDialog;
    ConfirmationDialog confirmationDialog;
    SupplierFormDialog supplierFormDialog;
    CategoryImageDialog categoryImageDialog;

    FirebaseDatabase firebaseDatabase;

    Query supplierCategoriesQuery, suppliersQuery, chaptersQuery;

    boolean isListening;

    List<CategoryImage> supplierCategories = new ArrayList<>();

    List<Supplier> suppliers = new ArrayList<>(), suppliersCopy = new ArrayList<>();

    AdminSupplierAdapter adminSupplierAdapter;

    List<Chapter> chapters = new ArrayList<>();

    ComponentManager componentManager;

    String searchSupplier = "", selectedCategoryId = "";

    Supplier selectedSupplier = new Supplier();

    AppStatusPromptDialog appStatusPromptDialog;
    NewVersionPromptDialog newVersionPromptDialog;
    Query applicationQuery;
    Application application = new Application();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_suppliers);

        etSearch = findViewById(R.id.etSearch);
        imgAdd = findViewById(R.id.imgAdd);
        tvActivityTitle = findViewById(R.id.tvActivityTitle);
        tvMessage = findViewById(R.id.tvMessage);
        tvCategory = findViewById(R.id.tvCategory);
        pbLoading2 = findViewById(R.id.pbLoading2);
        recyclerView = findViewById(R.id.recyclerView);

        context = AdminSuppliersActivity.this;

        loadingDialog = new LoadingDialog(context);
        messageDialog = new MessageDialog(context);
        confirmationDialog = new ConfirmationDialog(context);

        confirmationDialog.setDialogListener(() -> {
            supplierFormDialog.deleteSupplier(selectedSupplier, true);

            confirmationDialog.dismissDialog();
        });

        supplierFormDialog = new SupplierFormDialog(context);

        supplierFormDialog.setDirectory("suppliers");
        supplierFormDialog.setDialogListener(() -> {
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
            selectedSupplier.setCategory(categoryImage.getId());
            moveCategory();
        });

        appStatusPromptDialog = new AppStatusPromptDialog(context);
        newVersionPromptDialog = new NewVersionPromptDialog(context);

        selectedCategoryId = getIntent().getStringExtra("categoryId");

        supplierFormDialog.setCategoryId(selectedCategoryId);

        firebaseDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_RTDB_url));
        supplierCategoriesQuery = firebaseDatabase.getReference("supplierCategories").orderByChild("category");
        chaptersQuery = firebaseDatabase.getReference("chapters").orderByChild("chapter");
        suppliersQuery = firebaseDatabase.getReference("suppliers").orderByChild("supplier");
        applicationQuery = firebaseDatabase.getReference("application");

        pbLoading2.setVisibility(View.VISIBLE);
        isListening = true;
        etSearch.setVisibility(View.INVISIBLE);
        supplierCategoriesQuery.addValueEventListener(getSupplierCategories());

        supplierFormDialog.setDatabaseReference(suppliersQuery.getRef());
        categoryImageDialog.setQuery(supplierCategoriesQuery);
        applicationQuery.addValueEventListener(getApplicationValue());

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        adminSupplierAdapter = new AdminSupplierAdapter(context, suppliers, chapters);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adminSupplierAdapter);

        adminSupplierAdapter.setAdapterListener(new AdminSupplierAdapter.AdapterListener() {
            @Override
            public void onClick(Supplier supplier) {
                Intent intent = new Intent(context, AdminSupplierDetailsActivity.class);
                intent.putExtra("supplierId", supplier.getId());
                context.startActivity(intent);
            }

            @Override
            public void onEdit(Supplier supplier, Chapter chapter) {
                supplierFormDialog.setUpdateMode(true);
                supplierFormDialog.setSupplier(supplier, chapter);
                supplierFormDialog.showDialog();
            }

            @Override
            public void onDelete(Supplier supplier) {
                selectedSupplier = supplier;
                confirmationDialog.setMessage(getString(R.string.confirmation_prompt, "delete the supplier"));
                confirmationDialog.showDialog();
            }

            @Override
            public void onMove(Supplier supplier) {
                selectedSupplier = supplier;
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
                searchSupplier = editable != null ? editable.toString() : "";

                filterSuppliers();
            }
        });

        imgAdd.setOnClickListener(view -> {
            supplierFormDialog.setUpdateMode(false);
            supplierFormDialog.showDialog();
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void filterSuppliers() {
        List<Supplier> suppliersTemp = new ArrayList<>(suppliersCopy);

        suppliers.clear();

        for (int i = 0; i < suppliersTemp.size(); i++) {
            Supplier supplier = suppliersTemp.get(i);

            boolean isFiltered = selectedCategoryId == null ||
                    selectedCategoryId.trim().length() == 0 ||
                    supplier.getCategory().equals(selectedCategoryId);

            boolean isSearched = searchSupplier.trim().length() == 0 ||
                    supplier.getSupplier().toLowerCase().contains(searchSupplier.toLowerCase());

            if (isFiltered && isSearched) suppliers.add(supplier);
        }

        if (selectedCategoryId != null) {
            CategoryImage categoryImage = new CategoryImage();
            for (CategoryImage categoryImage1 : supplierCategories)
                if (selectedCategoryId.equals(categoryImage1.getId()))
                    categoryImage = categoryImage1;

            tvActivityTitle.setText(categoryImage.getCategory());
        } else tvActivityTitle.setText(getString(R.string.suppliers_members));

        if (suppliers.size() == 0) {
            tvMessage.setVisibility(View.VISIBLE);

            tvMessage.setText(getString(R.string.no_record, "Record"));
        } else tvMessage.setVisibility(View.GONE);
        tvMessage.bringToFront();

        pbLoading2.setVisibility(View.GONE);
        etSearch.setVisibility(View.VISIBLE);

        adminSupplierAdapter.notifyDataSetChanged();
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

                messageDialog.setMessage(getString(R.string.fd_on_cancelled, "SupplierCategories"));
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

    private void moveCategory() {
        loadingDialog.showDialog();

        suppliersQuery.getRef().child(selectedSupplier.getId())
                .setValue(selectedSupplier).addOnCompleteListener(task -> {
                    loadingDialog.dismissDialog();

                    if (task.isSuccessful()) {
                        categoryImageDialog.dismissDialog();

                        messageDialog.setMessageType(Enums.SUCCESS_MESSAGE);
                        messageDialog.setMessage("Successfully moved the supplier to another category.");
                    } else {
                        messageDialog.setMessageType(Enums.ERROR_MESSAGE);
                        messageDialog.setMessage("Failed to moved the supplier to another category.");
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
            etSearch.setText(data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0));

            filterSuppliers();
        } else if (requestCode == Enums.PICK_IMAGE_REQUEST_CODE && resultCode == RESULT_OK &&
                data != null && data.getData() != null) {
            supplierFormDialog.setImageData(data.getData());
        }
    }

    @Override
    public void onResume() {
        isListening = true;
        supplierCategoriesQuery.addListenerForSingleValueEvent(getSupplierCategories());
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