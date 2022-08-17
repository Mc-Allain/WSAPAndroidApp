package com.example.wsapandroidapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.wsapandroidapp.Adapters.SupplierAdapter;
import com.example.wsapandroidapp.Classes.ComponentManager;
import com.example.wsapandroidapp.Classes.Enums;
import com.example.wsapandroidapp.DataModel.Application;
import com.example.wsapandroidapp.DataModel.Chapter;
import com.example.wsapandroidapp.DataModel.Supplier;
import com.example.wsapandroidapp.DataModel.CategoryImage;
import com.example.wsapandroidapp.DialogClasses.AppStatusPromptDialog;
import com.example.wsapandroidapp.DialogClasses.MessageDialog;
import com.example.wsapandroidapp.DialogClasses.NewVersionPromptDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SuppliersActivity extends AppCompatActivity {

    EditText etSearch;
    TextView tvActivityTitle, tvMessage, tvCategory;
    ProgressBar pbLoading2;
    RecyclerView recyclerView;

    Context context;

    MessageDialog messageDialog;

    FirebaseDatabase firebaseDatabase;

    Query supplierCategoriesQuery, suppliersQuery, chaptersQuery;

    boolean isListening;

    List<CategoryImage> supplierCategories = new ArrayList<>();

    List<Supplier> suppliers = new ArrayList<>(), suppliersCopy = new ArrayList<>();

    SupplierAdapter supplierAdapter;

    List<Chapter> chapters = new ArrayList<>();

    ComponentManager componentManager;

    String searchSupplier = "", selectedCategoryId = "";

    AppStatusPromptDialog appStatusPromptDialog;
    NewVersionPromptDialog newVersionPromptDialog;
    Query applicationQuery;
    Application application = new Application();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suppliers);

        etSearch = findViewById(R.id.etSearch);
        tvActivityTitle = findViewById(R.id.tvActivityTitle);
        tvMessage = findViewById(R.id.tvMessage);
        tvCategory = findViewById(R.id.tvCategory);
        pbLoading2 = findViewById(R.id.pbLoading2);
        recyclerView = findViewById(R.id.recyclerView);

        context = SuppliersActivity.this;

        messageDialog = new MessageDialog(context);
        appStatusPromptDialog = new AppStatusPromptDialog(context);
        newVersionPromptDialog = new NewVersionPromptDialog(context);

        selectedCategoryId = getIntent().getStringExtra("categoryId");

        firebaseDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_RTDB_url));
        supplierCategoriesQuery = firebaseDatabase.getReference("supplierCategories");
        chaptersQuery = firebaseDatabase.getReference("chapters").orderByChild("chapter");
        suppliersQuery = firebaseDatabase.getReference("suppliers").orderByChild("supplier");
        applicationQuery = firebaseDatabase.getReference("application");

        pbLoading2.setVisibility(View.VISIBLE);
        isListening = true;
        etSearch.setVisibility(View.INVISIBLE);
        supplierCategoriesQuery.addValueEventListener(getSupplierCategories());
        applicationQuery.addValueEventListener(getApplicationValue());

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        supplierAdapter = new SupplierAdapter(context, suppliers, chapters);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(supplierAdapter);

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

                filterSuppliers();
            }
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