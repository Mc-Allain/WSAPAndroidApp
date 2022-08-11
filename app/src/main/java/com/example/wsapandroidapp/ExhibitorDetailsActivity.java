package com.example.wsapandroidapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.wsapandroidapp.Classes.Enums;
import com.example.wsapandroidapp.DataModel.CategoryImage;
import com.example.wsapandroidapp.DataModel.Exhibitor;
import com.example.wsapandroidapp.DataModel.ContactInfo;
import com.example.wsapandroidapp.DataModel.SocialMedia;
import com.example.wsapandroidapp.DialogClasses.LoadingDialog;
import com.example.wsapandroidapp.DialogClasses.MessageDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class ExhibitorDetailsActivity extends AppCompatActivity {

    ConstraintLayout contactInfoLayout, socialMediaLayout, descriptionLayout;
    ImageView imgPoster;
    TextView tvExhibitor;
    TextView tvCategory;
    ImageView imgPhoneNumber, imgEmailAddress, imgLocationAddress;
    TextView tvPhoneNumber, tvEmailAddress, tvLocationAddress;
    ImageView imgFacebook, imgInstagram, imgTwitter, imgYouTube, imgWeb;
    TextView tvDescription;
    Button btnVisitWSAP;

    Context context;

    LoadingDialog loadingDialog;
    MessageDialog messageDialog;

    FirebaseDatabase firebaseDatabase;

    Query exhibitorCategoriesQuery, exhibitorQuery;

    boolean isListening;

    Exhibitor exhibitor;
    ContactInfo contactInfo;
    SocialMedia socialMedia;

    CategoryImage category;

    String selectedExhibitorId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exhibitor_details);

        contactInfoLayout = findViewById(R.id.contactInfoLayout);
        socialMediaLayout = findViewById(R.id.socialMediaLayout);
        descriptionLayout = findViewById(R.id.descriptionLayout);
        imgPoster = findViewById(R.id.imgPoster);
        tvExhibitor = findViewById(R.id.tvExhibitor);
        tvCategory = findViewById(R.id.tvCategory);
        imgPhoneNumber = findViewById(R.id.imgPhoneNumber);
        imgEmailAddress = findViewById(R.id.imgEmailAddress);
        imgLocationAddress = findViewById(R.id.imgLocationAddress);
        tvPhoneNumber = findViewById(R.id.tvPhoneNumber);
        tvEmailAddress = findViewById(R.id.tvEmailAddress);
        tvLocationAddress = findViewById(R.id.tvLocationAddress);
        imgFacebook = findViewById(R.id.imgFacebook);
        imgInstagram = findViewById(R.id.imgInstagram);
        imgTwitter = findViewById(R.id.imgTwitter);
        imgYouTube = findViewById(R.id.imgYouTube);
        imgWeb = findViewById(R.id.imgWeb);
        tvDescription = findViewById(R.id.tvDescription);
        btnVisitWSAP = findViewById(R.id.btnVisitWSAP);

        context = ExhibitorDetailsActivity.this;

        loadingDialog = new LoadingDialog(context);
        messageDialog = new MessageDialog(context);

        selectedExhibitorId = getIntent().getStringExtra("exhibitorId");

        initDatabaseQuery();

        imgPoster.setOnClickListener(view -> {
            Intent intent = new Intent(context, ImageActivity.class);
            intent.putExtra("image", exhibitor.getImage());
            startActivity(intent);
        });

        btnVisitWSAP.setOnClickListener(view -> {
            Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(getString(R.string.expo_exhibitor_url)));
            startActivity(intent);
        });
    }

    private void initDatabaseQuery() {
        firebaseDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_RTDB_url));
        exhibitorQuery = firebaseDatabase.getReference("exhibitors").orderByChild("id").equalTo(selectedExhibitorId);
        exhibitorCategoriesQuery = firebaseDatabase.getReference("exhibitorCategories").orderByChild("category");

        loadingDialog.showDialog();
        isListening = true;
        exhibitorQuery.addValueEventListener(getSupplier());
    }

    private ValueEventListener getSupplier() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    
                    if (snapshot.exists())
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            exhibitor = dataSnapshot.getValue(Exhibitor.class);
                            contactInfo = dataSnapshot.child("contactInfo").getValue(ContactInfo.class);
                            socialMedia = dataSnapshot.child("socialMedia").getValue(SocialMedia.class);
                            break;
                        }

                    exhibitorCategoriesQuery.addValueEventListener(getSupplierCategories());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "onCancelled", error.toException());
                loadingDialog.dismissDialog();

                messageDialog.setMessage(getString(R.string.fd_on_cancelled, "Exhibitor"));
                messageDialog.setMessageType(Enums.ERROR_MESSAGE);
                messageDialog.showDialog();
            }
        };
    }

    private ValueEventListener getSupplierCategories() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    if (snapshot.exists())
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            CategoryImage exhibitorCategory = dataSnapshot.getValue(CategoryImage.class);

                            if (exhibitorCategory != null && exhibitorCategory.getId().equals(exhibitor.getCategory())) {
                                category = exhibitorCategory;
                                break;
                            }
                        }

                    loadingDialog.dismissDialog();

                    updateUI();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "onCancelled", error.toException());
                loadingDialog.dismissDialog();

                messageDialog.setMessage(getString(R.string.fd_on_cancelled, "Exhibitor Categories"));
                messageDialog.setMessageType(Enums.ERROR_MESSAGE);
                messageDialog.showDialog();
            }
        };
    }

    private void updateUI() {
        Glide.with(context).load(exhibitor.getImage()).centerCrop().placeholder(R.drawable.ic_wsap).
                error(R.drawable.ic_wsap).into(imgPoster);

        imgFacebook.setVisibility(View.INVISIBLE);
        imgInstagram.setVisibility(View.INVISIBLE);
        imgTwitter.setVisibility(View.INVISIBLE);
        imgYouTube.setVisibility(View.INVISIBLE);
        imgWeb.setVisibility(View.INVISIBLE);

        imgFacebook.setOnClickListener(null);
        imgInstagram.setOnClickListener(null);
        imgTwitter.setOnClickListener(null);
        imgYouTube.setOnClickListener(null);
        imgWeb.setOnClickListener(null);

        if (socialMedia != null) {
            socialMediaLayout.setVisibility(View.VISIBLE);

            if (socialMedia.getFacebook().length() > 0) {
                imgFacebook.setVisibility(View.VISIBLE);
                imgFacebook.setOnClickListener(view -> {
                    Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(socialMedia.getFacebook()));
                    startActivity(intent);
                });
            }

            if (socialMedia.getInstagram().length() > 0) {
                imgInstagram.setVisibility(View.VISIBLE);
                imgInstagram.setOnClickListener(view -> {
                    Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(socialMedia.getInstagram()));
                    startActivity(intent);
                });
            }

            if (socialMedia.getTwitter().length() > 0) {
                imgTwitter.setVisibility(View.VISIBLE);
                imgTwitter.setOnClickListener(view -> {
                    Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(socialMedia.getTwitter()));
                    startActivity(intent);
                });
            }

            if (socialMedia.getYoutube().length() > 0) {
                imgYouTube.setVisibility(View.VISIBLE);
                imgYouTube.setOnClickListener(view -> {
                    Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(socialMedia.getYoutube()));
                    startActivity(intent);
                });
            }

            if (socialMedia.getWebsite().length() > 0) {
                imgWeb.setVisibility(View.VISIBLE);
                imgWeb.setOnClickListener(view -> {
                    Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(socialMedia.getWebsite()));
                    startActivity(intent);
                });
            }
        } else socialMediaLayout.setVisibility(View.GONE);

        tvExhibitor.setText(exhibitor.getExhibitor());
        tvCategory.setText(category.getCategory());

        imgPhoneNumber.setVisibility(View.GONE);
        imgEmailAddress.setVisibility(View.GONE);
        imgLocationAddress.setVisibility(View.GONE);

        tvPhoneNumber.setVisibility(View.GONE);
        tvEmailAddress.setVisibility(View.GONE);
        tvLocationAddress.setVisibility(View.GONE);

        if (contactInfo != null) {
            contactInfoLayout.setVisibility(View.VISIBLE);

            if (String.valueOf(contactInfo.getPhoneNumber()).length() > 0 &&
                    contactInfo.getPhoneNumber() != 0) {
                imgPhoneNumber.setVisibility(View.VISIBLE);
                tvPhoneNumber.setVisibility(View.VISIBLE);
                tvPhoneNumber.setText(String.valueOf(contactInfo.getPhoneNumber()));
            }

            if (contactInfo.getEmailAddress().length() > 0) {
                imgEmailAddress.setVisibility(View.VISIBLE);
                tvEmailAddress.setVisibility(View.VISIBLE);
                tvEmailAddress.setText(contactInfo.getEmailAddress());
            }

            if (contactInfo.getLocationAddress().length() > 0) {
                imgLocationAddress.setVisibility(View.VISIBLE);
                tvLocationAddress.setVisibility(View.VISIBLE);
                tvLocationAddress.setText(contactInfo.getLocationAddress());
            }
        } else contactInfoLayout.setVisibility(View.GONE);

        if (tvDescription.getText().toString().length() != 0) {
            descriptionLayout.setVisibility(View.VISIBLE);

            tvDescription.setText(exhibitor.getDescription());
        } else descriptionLayout.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        isListening = true;
        exhibitorQuery.addListenerForSingleValueEvent(getSupplier());

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