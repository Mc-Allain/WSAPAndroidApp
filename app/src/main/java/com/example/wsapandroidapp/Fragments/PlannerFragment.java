package com.example.wsapandroidapp.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.wsapandroidapp.Adapters.MenuCategoryImageAdapter;
import com.example.wsapandroidapp.AlcoholCalculatorActivity;
import com.example.wsapandroidapp.BudgetAllocationPlanActivity;
import com.example.wsapandroidapp.Classes.Enums;
import com.example.wsapandroidapp.DataModel.MenuCategoryImage;
import com.example.wsapandroidapp.DialogClasses.ConfirmationDialog;
import com.example.wsapandroidapp.DialogClasses.MessageDialog;
import com.example.wsapandroidapp.NotepadActivity;
import com.example.wsapandroidapp.R;
import com.example.wsapandroidapp.SuppliersChecklistActivity;
import com.example.wsapandroidapp.SuppliersComparativeSheetActivity;
import com.example.wsapandroidapp.TodoChecklistActivity;
import com.example.wsapandroidapp.WeddingDressPickerActivity;
import com.example.wsapandroidapp.WeddingTimelineActivity;
import com.example.wsapandroidapp.WeddingTipsActivity;
import com.example.wsapandroidapp.WelcomeActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Arrays;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class PlannerFragment extends Fragment {

    RecyclerView recyclerView;

    Context context;

    MessageDialog messageDialog;
    ConfirmationDialog confirmationDialog;

    FirebaseAuth firebaseAuth;

    List<MenuCategoryImage> plannerCategories;

    MenuCategoryImageAdapter menuCategoryImageAdapter;

    boolean isAnonymous;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_planner, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);

        context = requireContext();

        getSharedPreference();

        messageDialog = new MessageDialog(context);
        confirmationDialog = new ConfirmationDialog(context);

        confirmationDialog.setDialogListener(this::signOut);

        firebaseAuth = FirebaseAuth.getInstance();

        plannerCategories = Arrays.asList(
                new MenuCategoryImage(getString(R.string.wedding_tips), R.drawable.topics, new Intent(context, WeddingTipsActivity.class)),
                new MenuCategoryImage(getString(R.string.alcohol_calculator), R.drawable.alcohol, new Intent(context, AlcoholCalculatorActivity.class)),
                new MenuCategoryImage(getString(R.string.wedding_dress_picker), R.drawable.wedding_dress, new Intent(context, WeddingDressPickerActivity.class)),
                new MenuCategoryImage(getString(R.string.wedding_timeline), R.drawable.wedding_timeline, new Intent(context, WeddingTimelineActivity.class)),
                new MenuCategoryImage(getString(R.string.suppliers_checklist), R.drawable.suppliers, new Intent(context, SuppliersChecklistActivity.class)),
                new MenuCategoryImage(getString(R.string.suppliers_comparative_sheet), R.drawable.suppliers, new Intent(context, SuppliersComparativeSheetActivity.class)),
                new MenuCategoryImage(getString(R.string.todo_checklist), R.drawable.todo_checklist, new Intent(context, TodoChecklistActivity.class)),
                new MenuCategoryImage(getString(R.string.budget_allocation_plan), R.drawable.budget_plan, new Intent(context, BudgetAllocationPlanActivity.class)),
                new MenuCategoryImage(getString(R.string.notepad), R.drawable.notepad, new Intent(context, NotepadActivity.class))
        );

        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false);
        menuCategoryImageAdapter = new MenuCategoryImageAdapter(context, plannerCategories);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(menuCategoryImageAdapter);

        menuCategoryImageAdapter.setAdapterListener(menuCategoryImage -> {
            if (isAnonymous && !menuCategoryImage.getCategory().equals(getString(R.string.wedding_tips))) {
                confirmationDialog.setMessage(getString(R.string.non_anonymous_sign_in_prompt));
                confirmationDialog.showDialog();
            } else if (isComingSoon(menuCategoryImage)) {
                messageDialog.setMessage(getString(R.string.coming_soon));
                messageDialog.setMessageType(Enums.INFO_MESSAGE);
                messageDialog.showDialog();
            } else startActivity(menuCategoryImage.getIntent());
        });

        return view;
    }

    private boolean isComingSoon(MenuCategoryImage menuCategoryImage) {
        return menuCategoryImage.getCategory().equals(getString(R.string.wedding_tips)) ||
                menuCategoryImage.getCategory().equals(getString(R.string.todo_checklist)) ||
                menuCategoryImage.getCategory().equals(getString(R.string.notepad)) ||
                menuCategoryImage.getCategory().equals(getString(R.string.alcohol_calculator)) ||
                menuCategoryImage.getCategory().equals(getString(R.string.wedding_dress_picker));
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

    private void getSharedPreference() {
        String group = getResources().getStringArray(R.array.shared_preferences)[0];
        String[] fields = getResources().getStringArray(R.array.auth_data);

        SharedPreferences sharedPreferences = context.getSharedPreferences(group, Context.MODE_PRIVATE);
        isAnonymous = sharedPreferences.getBoolean(fields[2], false);
    }
}