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

import com.example.wsapandroidapp.Adapters.UserAdapter;
import com.example.wsapandroidapp.Classes.ComponentManager;
import com.example.wsapandroidapp.Classes.Enums;
import com.example.wsapandroidapp.DataModel.Application;
import com.example.wsapandroidapp.DataModel.User;
import com.example.wsapandroidapp.DataModel.UserRole;
import com.example.wsapandroidapp.DialogClasses.AppStatusPromptDialog;
import com.example.wsapandroidapp.DialogClasses.ChangeUserRoleDialog;
import com.example.wsapandroidapp.DialogClasses.LoadingDialog;
import com.example.wsapandroidapp.DialogClasses.MessageDialog;
import com.example.wsapandroidapp.DialogClasses.NewVersionPromptDialog;
import com.example.wsapandroidapp.DialogClasses.UserRoleDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AdminUsersActivity extends AppCompatActivity {

    EditText etSearch;
    TextView tvMessage, tvRole;
    RecyclerView recyclerView;
    ConstraintLayout roleLayout;

    Context context;

    LoadingDialog loadingDialog;
    MessageDialog messageDialog;
    UserRoleDialog userRoleDialog;
    ChangeUserRoleDialog changeUserRoleDialog;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;

    Query usersQuery;

    boolean isListening;

    List<User> users = new ArrayList<>(), usersCopy = new ArrayList<>();

    UserAdapter userAdapter;

    ComponentManager componentManager;

    String searchUser = "";
    int selectedRole = 0;

    User selectedUser, currentUser;

    AppStatusPromptDialog appStatusPromptDialog;
    NewVersionPromptDialog newVersionPromptDialog;
    Query applicationQuery;
    Application application = new Application();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_users);

        etSearch = findViewById(R.id.etSearch);
        tvMessage = findViewById(R.id.tvMessage);
        tvRole = findViewById(R.id.tvRole);
        recyclerView = findViewById(R.id.recyclerView);
        roleLayout = findViewById(R.id.roleLayout);

        context = AdminUsersActivity.this;

        loadingDialog = new LoadingDialog(context);
        messageDialog = new MessageDialog(context);
        userRoleDialog = new UserRoleDialog(context);

        userRoleDialog.setCurrentSelectedIndex(selectedRole);
        userRoleDialog.setDialogListener(currentSelectedIndex -> {
            selectedRole = currentSelectedIndex;
            switch (currentSelectedIndex) {
                case 0:
                    tvRole.setText(getString(R.string.all));
                    break;
                case 1:
                    tvRole.setText(getString(R.string.non_admin));
                    break;
                case 2:
                    tvRole.setText(getString(R.string.admin));
                    break;
            }

            userRoleDialog.dismissDialog();

            filterUsers();
        });

        changeUserRoleDialog = new ChangeUserRoleDialog(context);

        changeUserRoleDialog.setDialogListener(new ChangeUserRoleDialog.DialogListener() {
            @Override
            public void onSelect(int currentSelectedIndex) {
                UserRole role = new UserRole(false, false, false, false);
                if (currentSelectedIndex == 1)
                    role.setAdmin(true);
                if (currentSelectedIndex == 2)
                    role.setAdminHead(true);
                if (currentSelectedIndex == 3)
                    role.setDeveloper(true);
                if (currentSelectedIndex == 4)
                    role.setLeadDeveloper(true);

                selectedUser.setRole(role);

                updateUser();

                changeUserRoleDialog.dismissDialog();
            }

            @Override
            public void onFailed() {
                messageDialog.setMessage(getString(R.string.change_role_error));
                messageDialog.setMessageType(Enums.ERROR_MESSAGE);
                messageDialog.showDialog();
            }
        });

        appStatusPromptDialog = new AppStatusPromptDialog(context);
        newVersionPromptDialog = new NewVersionPromptDialog(context);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        firebaseDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_RTDB_url));
        usersQuery = firebaseDatabase.getReference("users").orderByChild("displayName");
        applicationQuery = firebaseDatabase.getReference("application");

        loadingDialog.showDialog();
        isListening = true;
        etSearch.setVisibility(View.INVISIBLE);
        usersQuery.addValueEventListener(getUsers());
        applicationQuery.addValueEventListener(getApplicationValue());

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        userAdapter = new UserAdapter(context, users);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(userAdapter);

        userAdapter.setAdapterListener(user -> {
            selectedUser = user;

            int index = 0;
            if (user.getRole().isAdmin()) index = 1;
            if (user.getRole().isAdminHead()) index = 2;
            if (user.getRole().isDeveloper()) index = 3;
            if (user.getRole().isLeadDeveloper()) index = 4;

            changeUserRoleDialog.setCurrentSelectedIndex(index);
            changeUserRoleDialog.showDialog();
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
                searchUser = editable != null ? editable.toString() : "";

                filterUsers();
            }
        });

        roleLayout.setOnClickListener(view1 -> userRoleDialog.showDialog());
    }

    @SuppressLint("NotifyDataSetChanged")
    private void filterUsers() {
        List<User> usersTemp = new ArrayList<>(usersCopy);

        users.clear();

        for (int i = 0; i < usersTemp.size(); i++) {
            User user = usersTemp.get(i);

            boolean isSearched = searchUser.trim().length() == 0 ||
                    user.getDisplayName().toLowerCase().contains(searchUser.toLowerCase());

            boolean nonAdmin = !(user.getRole().isAdmin() || user.getRole().isAdminHead() ||
                    user.getRole().isDeveloper() || user.getRole().isLeadDeveloper());

            if ((nonAdmin && selectedRole == 1 || selectedRole == 0) && isSearched) users.add(user);
            else if ((user.getRole().isAdmin() && selectedRole == 2) && isSearched) users.add(user);
            else if ((user.getRole().isAdminHead() && selectedRole == 3) && isSearched) users.add(user);
            else if ((user.getRole().isDeveloper() && selectedRole == 4) && isSearched) users.add(user);
            else if ((user.getRole().isLeadDeveloper() && selectedRole == 5) && isSearched) users.add(user);
        }

        if (users.size() == 0) {
            tvMessage.setVisibility(View.VISIBLE);

            String noRecordsError = getString(R.string.no_record, "User");
            if (selectedRole == 1)
                noRecordsError = getString(R.string.no_record, "Non-Admin");
            else if (selectedRole == 2)
                noRecordsError = getString(R.string.no_record, "Admin User");

            tvMessage.setText(noRecordsError);
        } else tvMessage.setVisibility(View.GONE);
        tvMessage.bringToFront();

        loadingDialog.dismissDialog();
        etSearch.setVisibility(View.VISIBLE);

        int userRoleIndex = 0;
        if (currentUser.getRole().isAdmin()) userRoleIndex = 1;
        if (currentUser.getRole().isAdminHead()) userRoleIndex = 2;
        if (currentUser.getRole().isDeveloper()) userRoleIndex = 3;
        if (currentUser.getRole().isLeadDeveloper()) userRoleIndex = 4;

        userAdapter.setUserRoleIndex(userRoleIndex);
        userAdapter.notifyDataSetChanged();

        changeUserRoleDialog.setUserRoleIndex(userRoleIndex);
    }

    private ValueEventListener getUsers() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    users.clear();

                    if (snapshot.exists())
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            User user = dataSnapshot.getValue(User.class);

                            if (user != null) {
                                users.add(user);
                                if (user.getId().equals(firebaseUser.getUid()))
                                    currentUser = user;
                            }
                        }

                    usersCopy = new ArrayList<>(users);

                    filterUsers();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "onCancelled", error.toException());
                loadingDialog.dismissDialog();

                messageDialog.setMessage(getString(R.string.fd_on_cancelled, "Users"));
                messageDialog.setMessageType(Enums.ERROR_MESSAGE);
                messageDialog.showDialog();
            }
        };
    }

    private void updateUser() {
        loadingDialog.showDialog();

        usersQuery.getRef().child(selectedUser.getId())
                .setValue(selectedUser).addOnCompleteListener(task -> {
                    loadingDialog.dismissDialog();

                    if (task.isSuccessful()) {
                        messageDialog.setMessageType(Enums.SUCCESS_MESSAGE);
                        messageDialog.setMessage(getString(R.string.update_record_success_msg, "the user's role"));
                    } else if (task.getException() != null) {
                        messageDialog.setMessageType(Enums.ERROR_MESSAGE);
                        messageDialog.setMessage(task.getException().toString());
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Enums.VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            assert data != null;
            etSearch.setText(data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0));

            filterUsers();
        }
    }

    @Override
    public void onResume() {
        isListening = true;
        usersQuery.addListenerForSingleValueEvent(getUsers());
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