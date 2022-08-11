package com.example.wsapandroidapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.wsapandroidapp.Adapters.CategoryImageAdapter;
import com.example.wsapandroidapp.Adapters.TopicSearchedItemAdapter;
import com.example.wsapandroidapp.Classes.ComponentManager;
import com.example.wsapandroidapp.Classes.Credentials;
import com.example.wsapandroidapp.Classes.Enums;
import com.example.wsapandroidapp.DataModel.Topic;
import com.example.wsapandroidapp.DataModel.CategoryImage;
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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class TopicsActivity extends AppCompatActivity {

    EditText etSearch;
    TextView tvMessage;
    RecyclerView recyclerView, recyclerView1;
    ConstraintLayout constraintLayout;
    ProgressBar pbLoading2;

    Context context;

    MessageDialog messageDialog;

    FirebaseDatabase firebaseDatabase;

    Query topicCategoriesQuery;

    boolean isListening;

    List<CategoryImage> topicCategories = new ArrayList<>();

    List<Topic> topics = new ArrayList<>(), topicsCopy = new ArrayList<>();

    TopicSearchedItemAdapter topicSearchedItemAdapter;

    CategoryImageAdapter categoryImageAdapter;

    ComponentManager componentManager;

    String searchTopic = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topics);

        etSearch = findViewById(R.id.etSearch);
        tvMessage = findViewById(R.id.tvMessage);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView1 = findViewById(R.id.recyclerView1);
        constraintLayout = findViewById(R.id.constraintLayout);
        pbLoading2 = findViewById(R.id.pbLoading2);

        context = TopicsActivity.this;

        messageDialog = new MessageDialog(context);

        firebaseDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_RTDB_url));
        topicCategoriesQuery = firebaseDatabase.getReference("topicCategories").orderByChild("category");

        pbLoading2.setVisibility(View.VISIBLE);
        isListening = true;
        etSearch.setVisibility(View.INVISIBLE);
        topicCategoriesQuery.addValueEventListener(getTopicCategories());

        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false);
        categoryImageAdapter = new CategoryImageAdapter(context, topicCategories);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(categoryImageAdapter);

        categoryImageAdapter.setAdapterListener(categoryImage -> {
            Intent intent = new Intent(context, PostsActivity.class);
            if (categoryImage.getCategory().equals(getString(R.string.tutorials)))
                intent = new Intent(context, TutorialsActivity.class);
            else if (categoryImage.getCategory().equals(getString(R.string.dos_and_donts)))
                intent = new Intent(context, DosAndDontsActivity.class);
            else if (categoryImage.getCategory().equals(getString(R.string.trivias)))
                intent = new Intent(context, TriviasActivity.class);

            startActivity(intent);
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        topicSearchedItemAdapter = new TopicSearchedItemAdapter(context, topics, topicCategories);
        recyclerView1.setLayoutManager(linearLayoutManager);
        recyclerView1.setAdapter(topicSearchedItemAdapter);

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
                searchTopic = editable != null ? editable.toString() : "";

                if (Credentials.fullTrim(searchTopic).length() > 0)
                    constraintLayout.setVisibility(View.VISIBLE);
                else constraintLayout.setVisibility(View.GONE);

                filterTopics();
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void filterTopics() {
        List<Topic> topicsTemp = new ArrayList<>(topicsCopy);

        topics.clear();

        for (int i = 0; i < topicsTemp.size(); i++) {
            Topic topic = topicsTemp.get(i);

            boolean isSearched = searchTopic.trim().length() == 0 ||
                    topic.getTopic().toLowerCase().contains(searchTopic.toLowerCase());

            if (isSearched) topics.add(topic);
        }

        if (topics.size() == 0) {
            tvMessage.setVisibility(View.VISIBLE);

            tvMessage.setText(getString(R.string.no_record, "Record"));
        } else tvMessage.setVisibility(View.GONE);
        tvMessage.bringToFront();

        pbLoading2.setVisibility(View.GONE);
        etSearch.setVisibility(View.VISIBLE);

        topicSearchedItemAdapter.notifyDataSetChanged();
    }

    private ValueEventListener getTopicCategories() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    topicCategories.clear();

                    if (snapshot.exists())
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            CategoryImage topicCategory = dataSnapshot.getValue(CategoryImage.class);

                            if (topicCategory != null) topicCategories.add(topicCategory);
                        }

                    pbLoading2.setVisibility(View.GONE);
                    etSearch.setVisibility(View.VISIBLE);

                    categoryImageAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "onCancelled", error.toException());
                pbLoading2.setVisibility(View.GONE);

                messageDialog.setMessage(getString(R.string.fd_on_cancelled, "Topic Categories"));
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

            filterTopics();
        }
    }

    @Override
    public void onResume() {
        isListening = true;
        topicCategoriesQuery.addListenerForSingleValueEvent(getTopicCategories());

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