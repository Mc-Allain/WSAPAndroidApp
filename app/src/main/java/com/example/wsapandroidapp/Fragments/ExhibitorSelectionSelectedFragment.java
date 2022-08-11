package com.example.wsapandroidapp.Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.wsapandroidapp.Adapters.ExhibitorSelection2Adapter;
import com.example.wsapandroidapp.DataModel.Exhibitor;
import com.example.wsapandroidapp.R;

import java.util.ArrayList;
import java.util.List;

public class ExhibitorSelectionSelectedFragment extends Fragment {

    private TextView tvMessage;

    private ExhibitorSelection2Adapter exhibitorSelectionAdapter;

    private List<Exhibitor> exhibitors = new ArrayList<>(),
            selectedExhibitors = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exhibitor_selection_selected, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        tvMessage = view.findViewById(R.id.tvMessage);

        Context context = requireContext();

        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false);
        exhibitorSelectionAdapter = new ExhibitorSelection2Adapter(context, exhibitors, selectedExhibitors);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(exhibitorSelectionAdapter);

        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setExhibitors(List<Exhibitor> exhibitors, List<Exhibitor> selectedExhibitors) {
        this.exhibitors = new ArrayList<>(exhibitors);
        this.selectedExhibitors = new ArrayList<>(selectedExhibitors);
    }

    FragmentListener fragmentListener;

    public interface FragmentListener {
        void sendSelectedExhibitors(List<Exhibitor> selectedExhibitors);
    }

    public void setFragmentListener(FragmentListener fragmentListener) {
        this.fragmentListener = fragmentListener;
    }
}