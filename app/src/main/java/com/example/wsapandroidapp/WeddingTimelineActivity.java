package com.example.wsapandroidapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.wsapandroidapp.Adapters.WeddingTimelineAdapter;
import com.example.wsapandroidapp.Classes.ComponentManager;
import com.example.wsapandroidapp.Classes.DateTime;
import com.example.wsapandroidapp.Classes.Enums;
import com.example.wsapandroidapp.Classes.Units;
import com.example.wsapandroidapp.DataModel.Application;
import com.example.wsapandroidapp.DataModel.UserWeddingTimeline;
import com.example.wsapandroidapp.DataModel.UserWeddingTimelineList;
import com.example.wsapandroidapp.DataModel.UserWeddingTimelineTask;
import com.example.wsapandroidapp.DataModel.WeddingTimeline;
import com.example.wsapandroidapp.DataModel.WeddingTimelineTask;
import com.example.wsapandroidapp.DialogClasses.AppStatusPromptDialog;
import com.example.wsapandroidapp.DialogClasses.LoadingDialog;
import com.example.wsapandroidapp.DialogClasses.MessageDialog;
import com.example.wsapandroidapp.DialogClasses.NewVersionPromptDialog;
import com.example.wsapandroidapp.DialogClasses.TargetWeddingDateFormDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class WeddingTimelineActivity extends AppCompatActivity {

    EditText etSearch;
    ImageView imgUpdate;
    TextView tvMessage, tvTargetWeddingDate, tvTimeLeft;
    RecyclerView recyclerView;

    Context context;

    LoadingDialog loadingDialog;
    MessageDialog messageDialog;
    TargetWeddingDateFormDialog targetWeddingDateFormDialog;

    DateTime dateTime;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;

    Query weddingTimelineQuery, userWeddingTimelineQuery;

    boolean isListening;
    boolean isUpdateMode;

    List<UserWeddingTimeline> userWeddingTimeline = new ArrayList<>(),
            userWeddingTimelineCopy = new ArrayList<>();

    WeddingTimelineAdapter weddingTimelineAdapter;

    UserWeddingTimelineList userWeddingTimelineList;

    ComponentManager componentManager;

    long targetWeddingDate = 0;

    String searchValue = "";

    CountDownTimer countDownTimer;

    AppStatusPromptDialog appStatusPromptDialog;
    NewVersionPromptDialog newVersionPromptDialog;
    Query applicationQuery;
    Application application = new Application();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wedding_timeline);

        etSearch = findViewById(R.id.etSearch);
        imgUpdate = findViewById(R.id.imgUpdate);
        tvMessage = findViewById(R.id.tvMessage);
        tvTargetWeddingDate = findViewById(R.id.tvTargetWeddingDate);
        tvTimeLeft = findViewById(R.id.tvTimeLeft);
        recyclerView = findViewById(R.id.recyclerView);

        context = WeddingTimelineActivity.this;

        loadingDialog = new LoadingDialog(context);
        messageDialog = new MessageDialog(context);
        targetWeddingDateFormDialog = new TargetWeddingDateFormDialog(context);

        targetWeddingDateFormDialog.setDialogListener(new TargetWeddingDateFormDialog.DialogListener() {
            @Override
            public void onSubmit(long targetWeddingDateTime) {
                targetWeddingDate = targetWeddingDateTime;

                if (isUpdateMode) updateTargetWeddingDate();
                else {
                    loadingDialog.showDialog();
                    weddingTimelineQuery.addListenerForSingleValueEvent(getWeddingTimeline());
                }
            }

            @Override
            public void onCancel() {
                targetWeddingDateFormDialog.dismissDialog();
                if (!isUpdateMode) onBackPressed();
            }
        });

        appStatusPromptDialog = new AppStatusPromptDialog(context);
        newVersionPromptDialog = new NewVersionPromptDialog(context);

        dateTime = new DateTime();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        firebaseDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_RTDB_url));
        weddingTimelineQuery = firebaseDatabase.getReference("weddingTimeline");
        userWeddingTimelineQuery = firebaseDatabase.getReference("userWeddingTimeline").orderByChild("id").equalTo(firebaseUser.getUid());
        applicationQuery = firebaseDatabase.getReference("application");

        loadingDialog.showDialog();
        isListening = true;
        isUpdateMode = false;
        userWeddingTimelineQuery.addValueEventListener(getUserWeddingTimeline());
        applicationQuery.addValueEventListener(getApplicationValue());

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        weddingTimelineAdapter = new WeddingTimelineAdapter(context, userWeddingTimeline);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(weddingTimelineAdapter);

        weddingTimelineAdapter.setAdapterListener(weddingTimeline -> {
            Map<String, UserWeddingTimeline> userWeddingTimelineMap = new HashMap<>();

            for (UserWeddingTimeline userWeddingTimeline1 : userWeddingTimeline) {
                if (userWeddingTimeline1.getId().equals(weddingTimeline.getId()))
                    userWeddingTimeline1 = weddingTimeline;

                userWeddingTimelineMap.put(userWeddingTimeline1.getId(), userWeddingTimeline1);
            }

            userWeddingTimelineList.setWeddingTimeline(userWeddingTimelineMap);

            updateWeddingTimeline();
        });

        calculateTargetWeddingDate();

        componentManager = new ComponentManager(context);
//        componentManager.setInputRightDrawable(etSearch, true, Enums.VOICE_RECOGNITION);
//        componentManager.setVoiceRecognitionListener(() -> startActivityForResult(componentManager.voiceRecognitionIntent(), Enums.VOICE_RECOGNITION_REQUEST_CODE));

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

                filterTasks();
            }
        });

        imgUpdate.setOnClickListener(view -> {
            isUpdateMode = true;
            targetWeddingDateFormDialog.setTargetWeddingDate(targetWeddingDate);
            targetWeddingDateFormDialog.showDialog();
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void filterTasks() {
        List<UserWeddingTimeline> userWeddingTimelineTemp = new ArrayList<>(userWeddingTimelineCopy);

        userWeddingTimeline.clear();

        for (int i = 0; i < userWeddingTimelineTemp.size(); i++) {
            UserWeddingTimeline userWeddingTimeline1 = userWeddingTimelineTemp.get(i);

            if (isSearched(userWeddingTimeline1)) userWeddingTimeline.add(userWeddingTimeline1);
        }

        if (userWeddingTimeline.size() == 0) {
            tvMessage.setVisibility(View.VISIBLE);

            String noRecordsError = getString(R.string.no_record, "Record");

            tvMessage.setText(noRecordsError);
        } else tvMessage.setVisibility(View.GONE);
        tvMessage.bringToFront();

        etSearch.setVisibility(View.VISIBLE);

        weddingTimelineAdapter.notifyDataSetChanged();
    }

    private boolean isSearched(UserWeddingTimeline userWeddingTimeline1) {
        boolean isSearched = userWeddingTimeline1.getTitle().toLowerCase().contains(searchValue.toLowerCase());

        for (UserWeddingTimelineTask userWeddingTimelineTask : userWeddingTimeline1.getTasks().values())
            if (userWeddingTimelineTask.getTask().toLowerCase().contains(searchValue.toLowerCase())) {
                isSearched = true;
                break;
            }

        return searchValue.trim().length() == 0 || isSearched;
    }

    private ValueEventListener getUserWeddingTimeline() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    boolean isRead = false;
                    userWeddingTimeline.clear();

                    if (snapshot.exists())
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            userWeddingTimelineList = dataSnapshot.getValue(UserWeddingTimelineList.class);

                            if (userWeddingTimelineList != null) {
                                targetWeddingDate = userWeddingTimelineList.getTargetWeddingDate();
                                userWeddingTimeline.addAll(userWeddingTimelineList.getWeddingTimeline().values());
                                isRead = true;
                                isUpdateMode = true;
                                break;
                            }
                        }
                    else {
                        isUpdateMode = false;
                        loadingDialog.dismissDialog();
                        targetWeddingDateFormDialog.showDialog();
                    }

                    if (isRead) {
                        userWeddingTimeline.sort(Comparator.comparingInt(UserWeddingTimeline::getTimelineOrder));

                        loadingDialog.dismissDialog();

                        userWeddingTimelineCopy = new ArrayList<>(userWeddingTimeline);

                        updateUI();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "onCancelled", error.toException());
                loadingDialog.showDialog();

                messageDialog.setMessage(getString(R.string.fd_on_cancelled, "User Wedding Timeline"));
                messageDialog.setMessageType(Enums.ERROR_MESSAGE);
                messageDialog.showDialog();
            }
        };
    }

    private ValueEventListener getWeddingTimeline() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    List<WeddingTimelineTask> weddingTimelineTasks = new ArrayList<>();
                    Map<String, UserWeddingTimeline> weddingTimelineMap = new HashMap<>();

                    if (snapshot.exists())
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            WeddingTimeline weddingTimeline1 = dataSnapshot.getValue(WeddingTimeline.class);

                            if (weddingTimeline1 != null) {
                                weddingTimelineTasks.clear();
                                weddingTimelineTasks.addAll(weddingTimeline1.getTasks().values());

                                Map<String, UserWeddingTimelineTask> userWeddingTimelineTaskMap = new HashMap<>();

                                for (WeddingTimelineTask weddingTimelineTask : weddingTimelineTasks)
                                    userWeddingTimelineTaskMap.put(
                                            weddingTimelineTask.getId(),
                                            new UserWeddingTimelineTask(
                                                    weddingTimelineTask.getId(),
                                                    weddingTimelineTask.getTask(),
                                                    weddingTimelineTask.getTaskNo(),
                                                    false
                                            )
                                    );

                                UserWeddingTimeline userWeddingTimeline =
                                        new UserWeddingTimeline(
                                                weddingTimeline1.getId(),
                                                weddingTimeline1.getTitle(),
                                                weddingTimeline1.getDuration(),
                                                weddingTimeline1.getTimelineOrder(),
                                                userWeddingTimelineTaskMap
                                        );

                                weddingTimelineMap.put(weddingTimeline1.getId(), userWeddingTimeline);
                            }
                        }

                    userWeddingTimelineList = new UserWeddingTimelineList(firebaseUser.getUid(), targetWeddingDate, weddingTimelineMap);

                    userWeddingTimelineQuery.getRef().child(firebaseUser.getUid()).setValue(userWeddingTimelineList)
                            .addOnCompleteListener(task -> {
                                loadingDialog.dismissDialog();

                                if (task.isSuccessful()) {
                                    targetWeddingDateFormDialog.dismissDialog();

                                    messageDialog.setMessageType(Enums.SUCCESS_MESSAGE);
                                    messageDialog.setMessage("Successfully set the target wedding date.");
                                } else {
                                    messageDialog.setMessageType(Enums.ERROR_MESSAGE);
                                    messageDialog.setMessage("Failed to set the target wedding date.");
                                }

                                messageDialog.showDialog();
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "onCancelled", error.toException());
                loadingDialog.showDialog();

                messageDialog.setMessage(getString(R.string.fd_on_cancelled, "Wedding Timeline"));
                messageDialog.setMessageType(Enums.ERROR_MESSAGE);
                messageDialog.showDialog();
            }
        };
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateUI() {
        dateTime.setDateTime(targetWeddingDate);

        calculateTargetWeddingDate();

        weddingTimelineAdapter.notifyDataSetChanged();
    }

    private void calculateTargetWeddingDate() {
        tvTargetWeddingDate.setText(dateTime.getDateText());

        long duration = dateTime.getDateTimeValue() - new Date().getTime();

        weddingTimelineAdapter.setDuration(duration);

        if (countDownTimer != null) countDownTimer.cancel();

        if (duration > 0) {
            countDownTimer = new CountDownTimer(duration, (long) Units.secToMs(1)) {
                @Override
                public void onTick(long l) {
                    long milliseconds = dateTime.getDateTimeValue() - new Date().getTime();

                    int weeks = (int) Units.msToWeek(milliseconds);
                    milliseconds -= Units.weekToMs(weeks);
                    int days = (int) Units.msToDay(milliseconds);
                    milliseconds -= Units.dayToMs(days);
                    int hours = (int) Units.msToHour(milliseconds);
                    milliseconds -= Units.hourToMs(hours);
                    int minutes = (int) Units.msToMin(milliseconds);
                    milliseconds -= Units.minToMs(minutes);
                    int seconds = (int) Units.msToSec(milliseconds);

                    String string;
                    if (weeks > 0)
                        string = getString(R.string.wedding_date_time_left, weeks, days, hours, minutes, seconds);
                    else if (days > 0)
                        string = getString(R.string.wedding_date_time_left_1, days, hours, minutes, seconds);
                    else if (hours > 0)
                        string = getString(R.string.wedding_date_time_left_2, hours, minutes, seconds);
                    else if (minutes > 0)
                        string = getString(R.string.wedding_date_time_left_3, minutes, seconds);
                    else
                        string = getString(R.string.wedding_date_time_left_4, seconds);

                    tvTimeLeft.setText(string);
                }

                @Override
                public void onFinish() {
                    tvTimeLeft.setText(getString(R.string.wedding_day));
                }
            };
            countDownTimer.start();
        } else if (duration < -Units.dayToMs(1)) tvTimeLeft.setText(getString(R.string.congratulations));
        else tvTimeLeft.setText(getString(R.string.wedding_day));
    }

    private void updateWeddingTimeline() {
        userWeddingTimelineQuery.getRef().child(firebaseUser.getUid()).setValue(userWeddingTimelineList);
    }

    private void updateTargetWeddingDate() {
        loadingDialog.showDialog();

        userWeddingTimelineQuery.getRef().child(firebaseUser.getUid()).child("targetWeddingDate")
                .setValue(targetWeddingDate).addOnCompleteListener(task -> {
                    loadingDialog.dismissDialog();

                    if (task.isSuccessful()) {
                        targetWeddingDateFormDialog.dismissDialog();

                        messageDialog.setMessageType(Enums.SUCCESS_MESSAGE);
                        messageDialog.setMessage("Successfully updated the target wedding date.");
                    } else {
                        messageDialog.setMessageType(Enums.ERROR_MESSAGE);
                        messageDialog.setMessage("Failed to update the target wedding date.");
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
        }
    }

    @Override
    public void onResume() {
        isListening = true;
        userWeddingTimelineQuery.addListenerForSingleValueEvent(getUserWeddingTimeline());
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