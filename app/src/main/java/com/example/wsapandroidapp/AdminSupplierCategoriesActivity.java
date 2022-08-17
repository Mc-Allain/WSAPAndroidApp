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

import com.example.wsapandroidapp.Adapters.AdminCategoryImageAdapter;
import com.example.wsapandroidapp.Adapters.SupplierAdapter;
import com.example.wsapandroidapp.Classes.ComponentManager;
import com.example.wsapandroidapp.Classes.Credentials;
import com.example.wsapandroidapp.Classes.Enums;
import com.example.wsapandroidapp.DataModel.Application;
import com.example.wsapandroidapp.DataModel.CategoryImage;
import com.example.wsapandroidapp.DataModel.Chapter;
import com.example.wsapandroidapp.DataModel.Supplier;
import com.example.wsapandroidapp.DialogClasses.AppStatusPromptDialog;
import com.example.wsapandroidapp.DialogClasses.CategoryImageFormDialog;
import com.example.wsapandroidapp.DialogClasses.ConfirmationDialog;
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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AdminSupplierCategoriesActivity extends AppCompatActivity {

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

    Query supplierCategoriesQuery, chaptersQuery, suppliersQuery;

    boolean isListening;

    List<CategoryImage> supplierCategories = new ArrayList<>();

    List<Supplier> suppliers = new ArrayList<>(), suppliersCopy = new ArrayList<>();

    AdminCategoryImageAdapter adminCategoryImageAdapter;

    SupplierAdapter supplierAdapter;

    List<Chapter> chapters = new ArrayList<>();

    ComponentManager componentManager;

    String searchSupplier = "";

    CategoryImage selectedCategoryImage = new CategoryImage();

    AppStatusPromptDialog appStatusPromptDialog;
    NewVersionPromptDialog newVersionPromptDialog;
    Query applicationQuery;
    Application application = new Application();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_supplier_categories);

        etSearch = findViewById(R.id.etSearch);
        imgAdd = findViewById(R.id.imgAdd);
        tvMessage = findViewById(R.id.tvMessage);
        tvCategory = findViewById(R.id.tvCategory);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView1 = findViewById(R.id.recyclerView1);
        constraintLayout = findViewById(R.id.constraintLayout);
        pbLoading2 = findViewById(R.id.pbLoading2);

        context = AdminSupplierCategoriesActivity.this;

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

        categoryImageFormDialog.setDirectory("supplierCategories");
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

        appStatusPromptDialog = new AppStatusPromptDialog(context);
        newVersionPromptDialog = new NewVersionPromptDialog(context);

        firebaseDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_RTDB_url));
        supplierCategoriesQuery = firebaseDatabase.getReference("supplierCategories").orderByChild("category");
        chaptersQuery = firebaseDatabase.getReference("chapters").orderByChild("chapter");
        suppliersQuery = firebaseDatabase.getReference("suppliers").orderByChild("supplier");
        applicationQuery = firebaseDatabase.getReference("application");

        pbLoading2.setVisibility(View.VISIBLE);
        isListening = true;
        etSearch.setVisibility(View.INVISIBLE);
        supplierCategoriesQuery.addValueEventListener(getSupplierCategories());
        applicationQuery.addValueEventListener(getApplicationValue());

        categoryImageFormDialog.setDatabaseReference(supplierCategoriesQuery.getRef());

        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false);
        adminCategoryImageAdapter = new AdminCategoryImageAdapter(context, supplierCategories);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adminCategoryImageAdapter);

        adminCategoryImageAdapter.setAdapterListener(new AdminCategoryImageAdapter.AdapterListener() {
            @Override
            public void onClick(CategoryImage categoryImage) {
                Intent intent = new Intent(context, AdminSuppliersActivity.class);
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
        supplierAdapter = new SupplierAdapter(context, suppliers, chapters);
        recyclerView1.setLayoutManager(linearLayoutManager);
        recyclerView1.setAdapter(supplierAdapter);

        supplierAdapter.setAdapterListener(supplier -> {
            Intent intent = new Intent(context, AdminSuppliersActivity.class);
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

        imgAdd.setOnClickListener(view -> {
            categoryImageFormDialog.setUpdateMode(false);
            categoryImageFormDialog.showDialog();
        });
    }

    private int getRecordCount(String categoryId) {
        int count = 0;

        for (Supplier supplier : suppliers) {
            boolean isFiltered = supplier.getCategory().equals(categoryId);

            if (isFiltered) count++;
        }

        return count;
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

        adminCategoryImageAdapter.notifyDataSetChanged();
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
            categoryImageFormDialog.setImageData(data.getData());
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