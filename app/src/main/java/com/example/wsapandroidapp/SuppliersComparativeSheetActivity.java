package com.example.wsapandroidapp;

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
import android.widget.TextView;

import com.example.wsapandroidapp.Adapters.SupplierComparativeSheetAdapter;
import com.example.wsapandroidapp.Classes.ComponentManager;
import com.example.wsapandroidapp.Classes.Credentials;
import com.example.wsapandroidapp.Classes.Enums;
import com.example.wsapandroidapp.DataModel.Application;
import com.example.wsapandroidapp.DataModel.SupplierComparativeSheetCategory;
import com.example.wsapandroidapp.DataModel.SupplierComparativeSheetItem;
import com.example.wsapandroidapp.DataModel.SupplierOption;
import com.example.wsapandroidapp.DataModel.UserSupplierComparativeSheetCategory;
import com.example.wsapandroidapp.DataModel.UserSupplierComparativeSheetCategoryList;
import com.example.wsapandroidapp.DataModel.UserSupplierComparativeSheetItem;
import com.example.wsapandroidapp.DialogClasses.AppStatusPromptDialog;
import com.example.wsapandroidapp.DialogClasses.LoadingDialog;
import com.example.wsapandroidapp.DialogClasses.MessageDialog;
import com.example.wsapandroidapp.DialogClasses.NewVersionPromptDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SuppliersComparativeSheetActivity extends AppCompatActivity {

    EditText etSearch;
    TextView tvMessage;
    RecyclerView recyclerView;

    Context context;

    LoadingDialog loadingDialog;
    MessageDialog messageDialog;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;

    Query supplierComparativeSheetQuery, userSupplierComparativeSheetQuery;

    boolean isListening;
    boolean isUpdateMode;

    List<UserSupplierComparativeSheetCategory> userSupplierComparativeSheetCategories = new ArrayList<>(),
            userSupplierComparativeSheetCategoriesCopy = new ArrayList<>();

    List list = new ArrayList();

    SupplierComparativeSheetAdapter supplierComparativeSheetAdapter;

    UserSupplierComparativeSheetCategoryList userSupplierComparativeSheetCategoryList;

    ComponentManager componentManager;

    String searchValue = "";

    AppStatusPromptDialog appStatusPromptDialog;
    NewVersionPromptDialog newVersionPromptDialog;
    Query applicationQuery;
    Application application = new Application();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suppliers_comparative_sheet);

        etSearch = findViewById(R.id.etSearch);
        tvMessage = findViewById(R.id.tvMessage);
        recyclerView = findViewById(R.id.recyclerView);

        context = SuppliersComparativeSheetActivity.this;

        loadingDialog = new LoadingDialog(context);
        messageDialog = new MessageDialog(context);
        appStatusPromptDialog = new AppStatusPromptDialog(context);
        newVersionPromptDialog = new NewVersionPromptDialog(context);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        firebaseDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_RTDB_url));
        supplierComparativeSheetQuery = firebaseDatabase.getReference("supplierComparativeSheet");
        userSupplierComparativeSheetQuery = firebaseDatabase.getReference("userSupplierComparativeSheet").orderByChild("id").equalTo(firebaseUser.getUid());
        applicationQuery = firebaseDatabase.getReference("application");

        loadingDialog.showDialog();
        isListening = true;
        isUpdateMode = false;
        userSupplierComparativeSheetQuery.addValueEventListener(getUserSupplierComparativeSheetCategory());
        applicationQuery.addValueEventListener(getApplicationValue());

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        supplierComparativeSheetAdapter = new SupplierComparativeSheetAdapter(context, list);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(supplierComparativeSheetAdapter);

        supplierComparativeSheetAdapter.setAdapterListener(supplierComparativeSheetItem -> {
            String categoryId = supplierComparativeSheetItem.getId(), checklistId = supplierComparativeSheetItem.getId();

            for (UserSupplierComparativeSheetCategory supplierComparativeSheetCategory : userSupplierComparativeSheetCategories)
                for (UserSupplierComparativeSheetItem userSupplierComparativeSheetItem : supplierComparativeSheetCategory.getItems().values())
                    if (userSupplierComparativeSheetItem.getId().equals(checklistId)) {
                        categoryId = supplierComparativeSheetCategory.getId();
                        break;
                    }

            updateSupplierComparativeSheetItem(categoryId, checklistId, supplierComparativeSheetItem);
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
                searchValue = editable != null ? editable.toString() : "";

                filterItems();
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void filterItems() {
        List<UserSupplierComparativeSheetCategory> userSupplierChecklistCategoriesTemp =
                new ArrayList<>(userSupplierComparativeSheetCategoriesCopy);

        userSupplierComparativeSheetCategories.clear();

        for (int i = 0; i < userSupplierChecklistCategoriesTemp.size(); i++) {
            UserSupplierComparativeSheetCategory userSupplierChecklistCategory = userSupplierChecklistCategoriesTemp.get(i);

            if (isSearched(userSupplierChecklistCategory)) userSupplierComparativeSheetCategories.add(userSupplierChecklistCategory);
        }

        list.clear();

        for (UserSupplierComparativeSheetCategory userSupplierComparativeSheetCategory : userSupplierComparativeSheetCategories) {
            list.add(userSupplierComparativeSheetCategory);

            List<UserSupplierComparativeSheetItem> userSupplierComparativeSheetItemItems =
                    new ArrayList<>(userSupplierComparativeSheetCategory.getItems().values());

            userSupplierComparativeSheetItemItems.sort(Comparator.comparingInt(UserSupplierComparativeSheetItem::getOrder));

            for (UserSupplierComparativeSheetItem userSupplierComparativeSheetItem : userSupplierComparativeSheetItemItems)
                if (userSupplierComparativeSheetItem.getItem().toLowerCase().contains(searchValue.toLowerCase()) ||
                        userSupplierComparativeSheetCategory.getCategory().toLowerCase().contains(searchValue.toLowerCase()))
                    list.add(userSupplierComparativeSheetItem);
        }

        if (userSupplierComparativeSheetCategories.size() == 0) {
            tvMessage.setVisibility(View.VISIBLE);

            String noRecordsError = getString(R.string.no_record, "Record");

            tvMessage.setText(noRecordsError);
        } else tvMessage.setVisibility(View.GONE);
        tvMessage.bringToFront();

        etSearch.setVisibility(View.VISIBLE);

        supplierComparativeSheetAdapter.notifyDataSetChanged();

        loadingDialog.dismissDialog();
    }

    private boolean isSearched(UserSupplierComparativeSheetCategory userSupplierComparativeSheetCategory) {
        boolean isSearched = userSupplierComparativeSheetCategory.getCategory().toLowerCase().contains(searchValue.toLowerCase());

        for (UserSupplierComparativeSheetItem userSupplierChecklist : userSupplierComparativeSheetCategory.getItems().values())
            if (userSupplierChecklist.getItem().toLowerCase().contains(searchValue.toLowerCase())) {
                isSearched = true;
                break;
            }

        return searchValue.trim().length() == 0 || isSearched;
    }

    private ValueEventListener getUserSupplierComparativeSheetCategory() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    boolean isRead = false;
                    userSupplierComparativeSheetCategories.clear();

                    if (snapshot.exists())
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            userSupplierComparativeSheetCategoryList = dataSnapshot.getValue(UserSupplierComparativeSheetCategoryList.class);

                            if (userSupplierComparativeSheetCategoryList != null) {
                                userSupplierComparativeSheetCategories.addAll(userSupplierComparativeSheetCategoryList.getSupplierComparativeSheetCategories().values());
                                isRead = true;
                                isUpdateMode = true;
                                break;
                            }
                        }
                    else supplierComparativeSheetQuery.addListenerForSingleValueEvent(getSupplierComparativeSheetCategory());

                    if (isRead) {
                        userSupplierComparativeSheetCategories.sort(Comparator.comparingInt(UserSupplierComparativeSheetCategory::getOrder));

                        userSupplierComparativeSheetCategoriesCopy = new ArrayList<>(userSupplierComparativeSheetCategories);

                        updateUI();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "onCancelled", error.toException());
                loadingDialog.showDialog();

                messageDialog.setMessage(getString(R.string.fd_on_cancelled, "User Supplier Comparative Sheet"));
                messageDialog.setMessageType(Enums.ERROR_MESSAGE);
                messageDialog.showDialog();
            }
        };
    }

    private ValueEventListener getSupplierComparativeSheetCategory() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    List<SupplierComparativeSheetItem> supplierComparativeSheetItems = new ArrayList<>();
                    Map<String, UserSupplierComparativeSheetCategory> userSupplierChecklistCategoryMap = new HashMap<>();

                    if (snapshot.exists())
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            SupplierComparativeSheetCategory supplierComparativeSheetCategory =
                                    dataSnapshot.getValue(SupplierComparativeSheetCategory.class);

                            if (supplierComparativeSheetCategory != null) {
                                supplierComparativeSheetItems.clear();
                                supplierComparativeSheetItems.addAll(supplierComparativeSheetCategory.getItems().values());

                                Map<String, UserSupplierComparativeSheetItem> userSupplierComparativeSheetMap = new HashMap<>();

                                Map<String, SupplierOption> supplierOptionMap = new HashMap<>();

                                String id = Credentials.getUniqueId();
                                supplierOptionMap.put(id, new SupplierOption(id, "", 1));
                                id = Credentials.getUniqueId();
                                supplierOptionMap.put(id, new SupplierOption(id, "", 2));
                                id = Credentials.getUniqueId();
                                supplierOptionMap.put(id, new SupplierOption(id, "", 3));

                                for (SupplierComparativeSheetItem supplierComparativeSheetItem : supplierComparativeSheetItems)
                                    userSupplierComparativeSheetMap.put(
                                            supplierComparativeSheetItem.getId(),
                                            new UserSupplierComparativeSheetItem(
                                                    supplierComparativeSheetItem.getId(),
                                                    supplierComparativeSheetItem.getItem(),
                                                    "",
                                                    supplierComparativeSheetItem.getOrder(),
                                                    supplierOptionMap
                                            )
                                    );

                                UserSupplierComparativeSheetCategory userSupplierChecklistCategory =
                                        new UserSupplierComparativeSheetCategory(
                                                supplierComparativeSheetCategory.getId(),
                                                supplierComparativeSheetCategory.getCategory(),
                                                supplierComparativeSheetCategory.getOrder(),
                                                userSupplierComparativeSheetMap
                                        );

                                userSupplierChecklistCategoryMap.put(userSupplierChecklistCategory.getId(), userSupplierChecklistCategory);
                            }
                        }

                    userSupplierComparativeSheetCategoryList = new UserSupplierComparativeSheetCategoryList(firebaseUser.getUid(), userSupplierChecklistCategoryMap);

                    userSupplierComparativeSheetQuery.getRef().child(firebaseUser.getUid()).setValue(userSupplierComparativeSheetCategoryList)
                            .addOnCompleteListener(task -> {
                                if (!task.isSuccessful() && task.getException() != null) {
                                    messageDialog.setMessageType(Enums.ERROR_MESSAGE);
                                    messageDialog.setMessage(task.getException().toString());
                                    messageDialog.showDialog();
                                }
                            });;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "onCancelled", error.toException());
                loadingDialog.showDialog();

                messageDialog.setMessage(getString(R.string.fd_on_cancelled, "Supplier Comparative Sheet"));
                messageDialog.setMessageType(Enums.ERROR_MESSAGE);
                messageDialog.showDialog();
            }
        };
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateUI() {
        filterItems();
    }

    private void updateSupplierComparativeSheetItem(String categoryId, String itemId, UserSupplierComparativeSheetItem supplierComparativeSheetItem) {
        userSupplierComparativeSheetQuery.getRef().child(firebaseUser.getUid())
                .child("supplierComparativeSheetCategories").child(categoryId)
                .child("items").child(itemId).setValue(supplierComparativeSheetItem)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                        supplierComparativeSheetAdapter.setChangesSaved();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Enums.VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            assert data != null;
            etSearch.setText(data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0));
        }
    }

    @Override
    public void onResume() {
        isListening = true;
        userSupplierComparativeSheetQuery.addListenerForSingleValueEvent(getUserSupplierComparativeSheetCategory());
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