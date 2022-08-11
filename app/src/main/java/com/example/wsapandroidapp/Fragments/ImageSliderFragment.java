package com.example.wsapandroidapp.Fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.wsapandroidapp.R;

public class ImageSliderFragment extends Fragment {

    ImageView imgVenuePoster;

    Context context;

    String image;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image_slider, container, false);

        imgVenuePoster = view.findViewById(R.id.imgVenuePoster);

        context = requireContext();

        if (getArguments() != null)
            image = getArguments().getString("image");

        Glide.with(context).load(image).centerCrop().placeholder(R.drawable.ic_wsap).
                error(R.drawable.ic_wsap).into(imgVenuePoster);

        return view;
    }
}