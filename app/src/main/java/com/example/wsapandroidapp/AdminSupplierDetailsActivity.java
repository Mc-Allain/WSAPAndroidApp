package com.example.wsapandroidapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.wsapandroidapp.Classes.Enums;
import com.example.wsapandroidapp.DataModel.CategoryImage;
import com.example.wsapandroidapp.DataModel.Chapter;
import com.example.wsapandroidapp.DataModel.ContactInfo;
import com.example.wsapandroidapp.DataModel.SocialMedia;
import com.example.wsapandroidapp.DataModel.Supplier;
import com.example.wsapandroidapp.DialogClasses.CategoryImageDialog;
import com.example.wsapandroidapp.DialogClasses.ContactInformationFormDialog;
import com.example.wsapandroidapp.DialogClasses.DescriptionFormDialog;
import com.example.wsapandroidapp.DialogClasses.LoadingDialog;
import com.example.wsapandroidapp.DialogClasses.MessageDialog;
import com.example.wsapandroidapp.DialogClasses.SocialMediaFormDialog;
import com.example.wsapandroidapp.DialogClasses.SupplierInformationFormDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

public class AdminSupplierDetailsActivity extends AppCompatActivity {

    ConstraintLayout contactInfoLayout, socialMediaLayout, descriptionLayout;
    ImageView imgPoster;
    TextView tvSupplier;
    ImageView imgChapter, imgCategory;
    TextView tvChapter, tvCategory;
    ImageView imgPhoneNumber, imgEmailAddress, imgLocationAddress;
    TextView tvPhoneNumber, tvEmailAddress, tvLocationAddress;
    ImageView imgFacebook, imgInstagram, imgTwitter, imgYouTube, imgWeb;
    TextView tvFacebook, tvInstagram, tvTwitter, tvYouTube, tvWebsite;
    TextView tvDescription;
    ImageView imgMove, imgUpdateMain, imgUpdateContact, imgUpdateSocial, imgUpdateDescription;

    Context context;

    LoadingDialog loadingDialog;
    MessageDialog messageDialog;
    SupplierInformationFormDialog supplierInformationFormDialog;
    CategoryImageDialog categoryImageDialog;
    ContactInformationFormDialog contactInformationFormDialog;
    DescriptionFormDialog descriptionFormDialog;
    SocialMediaFormDialog socialMediaFormDialog;

    FirebaseDatabase firebaseDatabase;

    Query supplierCategoriesQuery, chaptersQuery, supplierQuery;

    boolean isListening;

    Supplier supplier;
    ContactInfo contactInfo;
    SocialMedia socialMedia;

    CategoryImage category;
    Chapter chapter;

    String selectedSupplierId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_supplier_details);

        contactInfoLayout = findViewById(R.id.contactInfoLayout);
        socialMediaLayout = findViewById(R.id.socialMediaLayout);
        descriptionLayout = findViewById(R.id.descriptionLayout);
        imgPoster = findViewById(R.id.imgPoster);
        tvSupplier = findViewById(R.id.tvSupplier);
        imgChapter = findViewById(R.id.imgChapter);
        imgCategory = findViewById(R.id.imgCategory);
        tvChapter = findViewById(R.id.tvChapter);
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
        tvFacebook = findViewById(R.id.tvFacebook);
        tvInstagram = findViewById(R.id.tvInstagram);
        tvTwitter = findViewById(R.id.tvTwitter);
        tvYouTube = findViewById(R.id.tvYouTube);
        tvWebsite = findViewById(R.id.tvWebsite);
        tvDescription = findViewById(R.id.tvDescription);
        imgMove = findViewById(R.id.imgMove);
        imgUpdateMain = findViewById(R.id.imgUpdateMain);
        imgUpdateContact = findViewById(R.id.imgUpdateContact);
        imgUpdateSocial = findViewById(R.id.imgUpdateSocial);
        imgUpdateDescription = findViewById(R.id.imgUpdateDescription);

        context = AdminSupplierDetailsActivity.this;

        loadingDialog = new LoadingDialog(context);
        messageDialog = new MessageDialog(context);

        supplierInformationFormDialog = new SupplierInformationFormDialog(context);

        supplierInformationFormDialog.setDirectory("suppliers");
        supplierInformationFormDialog.setDialogListener(() -> {
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
            supplier.setCategory(categoryImage.getId());
            updateSupplier();
            categoryImageDialog.dismissDialog();
        });

        contactInformationFormDialog = new ContactInformationFormDialog(context);

        contactInformationFormDialog.setDialogListener(contactInfo -> {
            supplier.setContactInfo(contactInfo);
            updateSupplier();
            contactInformationFormDialog.dismissDialog();
        });

        descriptionFormDialog = new DescriptionFormDialog(context);

        socialMediaFormDialog = new SocialMediaFormDialog(context);

        socialMediaFormDialog.setDialogListener(socialMedia -> {
            supplier.setSocialMedia(socialMedia);
            updateSupplier();
            socialMediaFormDialog.dismissDialog();
        });

        selectedSupplierId = getIntent().getStringExtra("supplierId");

        initDatabaseQuery();

        imgPoster.setOnClickListener(view -> {
            Intent intent = new Intent(context, ImageActivity.class);
            intent.putExtra("image", supplier.getImage());
            startActivity(intent);
        });

        imgMove.setOnClickListener(view -> categoryImageDialog.showDialog());

        imgUpdateMain.setOnClickListener(view -> {
            supplierInformationFormDialog.setSupplier(supplier, chapter);
            supplierInformationFormDialog.showDialog();
        });

        imgUpdateContact.setOnClickListener(view -> {
            contactInformationFormDialog.setContactInfo(contactInfo);
            contactInformationFormDialog.showDialog();
        });

        imgUpdateDescription.setOnClickListener(view -> {
            descriptionFormDialog.setIdAndDescription(supplier.getId(), supplier.getDescription());
            descriptionFormDialog.showDialog();
        });

        imgUpdateSocial.setOnClickListener(view -> {
            socialMediaFormDialog.setSocialMedia(socialMedia);
            socialMediaFormDialog.showDialog();
        });
    }

    private void initDatabaseQuery() {
        firebaseDatabase = FirebaseDatabase.getInstance(getString(R.string.firebase_RTDB_url));
        supplierQuery = firebaseDatabase.getReference("suppliers").orderByChild("id").equalTo(selectedSupplierId);
        supplierCategoriesQuery = firebaseDatabase.getReference("supplierCategories").orderByChild("category");
        chaptersQuery = firebaseDatabase.getReference("chapters").orderByChild("chapter");

        loadingDialog.showDialog();
        isListening = true;
        supplierQuery.addValueEventListener(getSupplier());

        supplierInformationFormDialog.setDatabaseReference(supplierQuery.getRef());
        categoryImageDialog.setQuery(supplierCategoriesQuery);
        descriptionFormDialog.setDatabaseReference(supplierQuery.getRef());
    }

    private ValueEventListener getSupplier() {
        return new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isListening) {
                    if (snapshot.exists())
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            supplier = dataSnapshot.getValue(Supplier.class);
                            contactInfo = dataSnapshot.child("contactInfo").getValue(ContactInfo.class);
                            socialMedia = dataSnapshot.child("socialMedia").getValue(SocialMedia.class);
                            break;
                        }

                    supplierCategoriesQuery.addValueEventListener(getSupplierCategories());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "onCancelled", error.toException());
                loadingDialog.dismissDialog();

                messageDialog.setMessage(getString(R.string.fd_on_cancelled, "Supplier"));
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
                            CategoryImage categoryImage = dataSnapshot.getValue(CategoryImage.class);

                            if (categoryImage != null && categoryImage.getId().equals(supplier.getCategory())) {
                                category = categoryImage;
                                break;
                            }
                        }

                    chaptersQuery.addValueEventListener(getChapters());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG: " + context.getClass(), "onCancelled", error.toException());
                loadingDialog.dismissDialog();

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
                    if (snapshot.exists())
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Chapter supplierChapter = dataSnapshot.getValue(Chapter.class);

                            if (supplierChapter != null && supplierChapter.getId().equals(supplier.getChapter())) {
                                chapter = supplierChapter;
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

                messageDialog.setMessage(getString(R.string.fd_on_cancelled, "Chapters"));
                messageDialog.setMessageType(Enums.ERROR_MESSAGE);
                messageDialog.showDialog();
            }
        };
    }

    private void updateUI() {
        Glide.with(context).load(supplier.getImage()).centerCrop().placeholder(R.drawable.ic_wsap).
                error(R.drawable.ic_wsap).into(imgPoster);

        imgFacebook.setVisibility(View.GONE);
        imgInstagram.setVisibility(View.GONE);
        imgTwitter.setVisibility(View.GONE);
        imgYouTube.setVisibility(View.GONE);
        imgWeb.setVisibility(View.GONE);

        tvFacebook.setVisibility(View.GONE);
        tvInstagram.setVisibility(View.GONE);
        tvTwitter.setVisibility(View.GONE);
        tvYouTube.setVisibility(View.GONE);
        tvWebsite.setVisibility(View.GONE);

        imgFacebook.setOnClickListener(null);
        imgInstagram.setOnClickListener(null);
        imgTwitter.setOnClickListener(null);
        imgYouTube.setOnClickListener(null);
        imgWeb.setOnClickListener(null);

        tvFacebook.setOnClickListener(null);
        tvInstagram.setOnClickListener(null);
        tvTwitter.setOnClickListener(null);
        tvYouTube.setOnClickListener(null);
        tvWebsite.setOnClickListener(null);

        if (socialMedia != null) {
            socialMediaLayout.setVisibility(View.VISIBLE);

            if (socialMedia.getFacebook().length() > 0) {
                tvFacebook.setVisibility(View.VISIBLE);
                imgFacebook.setVisibility(View.VISIBLE);

                tvFacebook.setText(socialMedia.getFacebook());
                tvFacebook.setOnClickListener(view -> {
                    Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(socialMedia.getFacebook()));
                    startActivity(intent);
                });
                imgFacebook.setOnClickListener(view -> {
                    Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(socialMedia.getFacebook()));
                    startActivity(intent);
                });
            }

            if (socialMedia.getInstagram().length() > 0) {
                tvInstagram.setVisibility(View.VISIBLE);
                imgInstagram.setVisibility(View.VISIBLE);

                tvInstagram.setText(socialMedia.getInstagram());
                tvInstagram.setOnClickListener(view -> {
                    Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(socialMedia.getInstagram()));
                    startActivity(intent);
                });
                imgInstagram.setOnClickListener(view -> {
                    Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(socialMedia.getInstagram()));
                    startActivity(intent);
                });
            }

            if (socialMedia.getTwitter().length() > 0) {
                tvTwitter.setVisibility(View.VISIBLE);
                imgTwitter.setVisibility(View.VISIBLE);

                tvTwitter.setText(socialMedia.getTwitter());
                tvTwitter.setOnClickListener(view -> {
                    Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(socialMedia.getTwitter()));
                    startActivity(intent);
                });
                imgTwitter.setOnClickListener(view -> {
                    Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(socialMedia.getTwitter()));
                    startActivity(intent);
                });
            }

            if (socialMedia.getYoutube().length() > 0) {
                tvYouTube.setVisibility(View.VISIBLE);
                imgYouTube.setVisibility(View.VISIBLE);

                tvYouTube.setText(socialMedia.getYoutube());
                tvYouTube.setOnClickListener(view -> {
                    Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(socialMedia.getYoutube()));
                    startActivity(intent);
                });
                imgYouTube.setOnClickListener(view -> {
                    Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(socialMedia.getYoutube()));
                    startActivity(intent);
                });
            }

            if (socialMedia.getWebsite().length() > 0) {
                tvWebsite.setVisibility(View.VISIBLE);
                imgWeb.setVisibility(View.VISIBLE);

                tvWebsite.setText(socialMedia.getWebsite());
                tvWebsite.setOnClickListener(view -> {
                    Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(socialMedia.getWebsite()));
                    startActivity(intent);
                });
                imgWeb.setOnClickListener(view -> {
                    Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(socialMedia.getWebsite()));
                    startActivity(intent);
                });
            }
        }

        tvSupplier.setText(supplier.getSupplier());
        tvChapter.setText(chapter.getChapter());
        tvCategory.setText(category.getCategory());

        imgPhoneNumber.setVisibility(View.GONE);
        imgEmailAddress.setVisibility(View.GONE);
        imgLocationAddress.setVisibility(View.GONE);

        tvPhoneNumber.setVisibility(View.GONE);
        tvEmailAddress.setVisibility(View.GONE);
        tvLocationAddress.setVisibility(View.GONE);

        if (contactInfo != null) {
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
        }

        if (supplier.getDescription().length() > 0) {
            tvDescription.setVisibility(View.VISIBLE);

            tvDescription.setText(supplier.getDescription());
        } else tvDescription.setVisibility(View.GONE);
    }

    private void updateSupplier() {
        loadingDialog.showDialog();

        supplierQuery.getRef().child(selectedSupplierId)
                .setValue(supplier).addOnCompleteListener(task -> {
                    loadingDialog.dismissDialog();

                    if (task.isSuccessful()) {
                        messageDialog.setMessageType(Enums.SUCCESS_MESSAGE);
                        messageDialog.setMessage(getString(R.string.update_record_success_msg, "the supplier details."));
                    } else if (task.getException() != null) {
                        messageDialog.setMessageType(Enums.ERROR_MESSAGE);
                        messageDialog.setMessage(task.getException().toString());
                    }

                    messageDialog.showDialog();
                });
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
        if (requestCode == Enums.PICK_IMAGE_REQUEST_CODE && resultCode == RESULT_OK &&
                data != null && data.getData() != null) {
            supplierInformationFormDialog.setImageData(data.getData());
        }
    }

    @Override
    public void onResume() {
        isListening = true;
        supplierQuery.addListenerForSingleValueEvent(getSupplier());

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