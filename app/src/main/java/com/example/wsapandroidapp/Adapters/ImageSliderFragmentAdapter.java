package com.example.wsapandroidapp.Adapters;

import com.example.wsapandroidapp.Fragments.ImageSliderFragment;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ImageSliderFragmentAdapter extends FragmentStateAdapter {

    private final List<ImageSliderFragment> imageSliderFragmentList;

    public ImageSliderFragmentAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle,
                                      List<ImageSliderFragment> imageSliderFragmentList) {
        super(fragmentManager, lifecycle);

        this.imageSliderFragmentList = imageSliderFragmentList;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return imageSliderFragmentList.get(position);
    }

    @Override
    public int getItemCount() {
        return imageSliderFragmentList.size();
    }
}
