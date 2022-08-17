package com.example.wsapandroidapp.Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.wsapandroidapp.Adapters.MoreOptionIconAdapter;
import com.example.wsapandroidapp.Classes.Enums;
import com.example.wsapandroidapp.DashboardActivity;
import com.example.wsapandroidapp.DataModel.MoreOptionIcon;
import com.example.wsapandroidapp.DataModel.User;
import com.example.wsapandroidapp.DialogClasses.ConfirmationDialog;
import com.example.wsapandroidapp.DialogClasses.LoadingDialog;
import com.example.wsapandroidapp.DialogClasses.MessageDialog;
import com.example.wsapandroidapp.ProfileActivity;
import com.example.wsapandroidapp.R;
import com.example.wsapandroidapp.WebsitesActivity;
import com.example.wsapandroidapp.WelcomeActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MoreFragment extends Fragment {

    RecyclerView recyclerView;

    Context context;

    LoadingDialog loadingDialog;
    MessageDialog messageDialog;
    ConfirmationDialog confirmationDialog;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;

    Query userQuery;

    boolean isListening;

    List<MoreOptionIcon> moreOptionIconList;

    MoreOptionIconAdapter moreOptionIconAdapter;

    User user;

    boolean isAnonymous, isAdmin;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_more, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);

        context = requireContext();

        getSharedPreference();

        loadingDialog = new LoadingDialog(context);
        messageDialog = new MessageDialog(context);
        confirmationDialog = new ConfirmationDialog(context);

        confirmationDialog.setDialogListener(this::signOut);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_RTDB_url));
        userQuery = firebaseDatabase.getReference("users").orderByChild("id").equalTo(firebaseUser.getUid());

        loadingDialog.showDialog();
        isListening = true;
        userQuery.addValueEventListener(getUser());

        moreOptionIconList = Arrays.asList(
                new MoreOptionIcon(getString(R.string.profile), R.drawable.ic_baseline_person_24, new Intent(context, ProfileActivity.class)),
                new MoreOptionIcon(getString(R.string.dashboard), R.drawable.ic_baseline_dashboard_24, new Intent(context, DashboardActivity.class)),
                new MoreOptionIcon(getString(R.string.about_us), R.drawable.ic_outline_info_24, new Intent("android.intent.action.VIEW", Uri.parse(getString(R.string.about_us_url)))),
                new MoreOptionIcon(getString(R.string.the_founder), R.drawable.ic_baseline_groups_24, new Intent("android.intent.action.VIEW", Uri.parse(getString(R.string.the_founder_url)))),
                new MoreOptionIcon(getString(R.string.contact_us), R.drawable.ic_baseline_contacts_24, new Intent("android.intent.action.VIEW", Uri.parse(getString(R.string.contact_us_url)))),
                new MoreOptionIcon(getString(R.string.faq), R.drawable.ic_outline_question_answer_24, new Intent("android.intent.action.VIEW", Uri.parse(getString(R.string.faq_url)))),
                new MoreOptionIcon(getString(R.string.privacy_policy), R.drawable.ic_outline_privacy_tip_24, new Intent("android.intent.action.VIEW", Uri.parse(getString(R.string.privacy_policy_url)))),
                new MoreOptionIcon(getString(R.string.websites), R.drawable.ic_baseline_public_24, new Intent(context, WebsitesActivity.class)),
                new MoreOptionIcon(getString(R.string.sign_out), R.drawable.ic_baseline_logout_24, null)
        );

        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false);
        moreOptionIconAdapter = new MoreOptionIconAdapter(context, moreOptionIconList);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(moreOptionIconAdapter);

        moreOptionIconAdapter.setAdapterListener(moreOptionIcon -> {
            if (moreOptionIcon.getOption().equals(getString(R.string.sign_out))) {
                confirmationDialog.setMessage(getString(R.string.confirmation_prompt, getString(R.string.sign_out)));
                confirmationDialog.showDialog();
            } else if (isAnonymous && moreOptionIcon.getOption().equals(getString(R.string.profile))) {
                confirmationDialog.setMessage(getString(R.string.non_anonymous_sign_in_prompt));
                confirmationDialog.showDialog();
            } else if (!isAdmin && moreOptionIcon.getOption().equals(getString(R.string.dashboard))) {
                messageDialog.setMessage(getString(R.string.non_admin_dashboard_access));
                messageDialog.setMessageType(Enums.INFO_MESSAGE);
                messageDialog.showDialog();
            } else startActivity(moreOptionIcon.getIntent());
        });

        return view;
    }

    private void signOut() {
        GoogleSignInOptions options = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build();

        GoogleSignInClient client = GoogleSignIn.getClient(context, options);

        client.signOut();

        firebaseAuth.signOut();

        Intent intent = new Intent(context, WelcomeActivity.class);
        startActivity(intent);
        ((Activity) context).finishAffinity();
    }

    private ValueEventListener getUser() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {

                    if (snapshot.exists())
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            user = dataSnapshot.getValue(User.class);
                            break;
                        }

                    if (user != null)
                        isAdmin = user.getRole().isAdmin() || user.getRole().isAdminHead() ||
                                user.getRole().isDeveloper() || user.getRole().isLeadDeveloper();

                    loadingDialog.dismissDialog();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "onCancelled", error.toException());
                loadingDialog.dismissDialog();

                messageDialog.setMessage(getString(R.string.fd_on_cancelled, "User"));
                messageDialog.setMessageType(Enums.ERROR_MESSAGE);
                messageDialog.showDialog();
            }
        };
    }

    private void getSharedPreference() {
        String group = getResources().getStringArray(R.array.shared_preferences)[0];
        String[] fields = getResources().getStringArray(R.array.auth_data);

        SharedPreferences sharedPreferences = context.getSharedPreferences(group, Context.MODE_PRIVATE);
        isAnonymous = sharedPreferences.getBoolean(fields[2], false);
    }

    @Override
    public void onResume() {
        isListening = true;
        userQuery.addListenerForSingleValueEvent(getUser());

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