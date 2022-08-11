package com.example.wsapandroidapp.Adapters;

import com.example.wsapandroidapp.Fragments.ExhibitorSelectionAllFragment;
import com.example.wsapandroidapp.Fragments.ExhibitorSelectionSelectedFragment;
import com.example.wsapandroidapp.Fragments.ExhibitorSelectionUnselectedFragment;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ExhibitorSelectionFragmentAdapter extends FragmentStateAdapter {


    private final ExhibitorSelectionAllFragment exhibitorSelectionAllFragment;
    private final ExhibitorSelectionUnselectedFragment exhibitorSelectionUnselectedFragment;
    private final ExhibitorSelectionSelectedFragment exhibitorSelectionSelectedFragment;

    public ExhibitorSelectionFragmentAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle,
                                             ExhibitorSelectionAllFragment exhibitorSelectionAllFragment,
                                             ExhibitorSelectionUnselectedFragment exhibitorSelectionUnselectedFragment,
                                             ExhibitorSelectionSelectedFragment exhibitorSelectionSelectedFragment) {
        super(fragmentManager, lifecycle);

        this.exhibitorSelectionAllFragment = exhibitorSelectionAllFragment;
        this.exhibitorSelectionUnselectedFragment = exhibitorSelectionUnselectedFragment;
        this.exhibitorSelectionSelectedFragment = exhibitorSelectionSelectedFragment;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return exhibitorSelectionAllFragment;
            case 1:
                return exhibitorSelectionUnselectedFragment;
        }

        return exhibitorSelectionSelectedFragment;
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
