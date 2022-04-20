package com.codewithjosh.NewsExpose2k20.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codewithjosh.NewsExpose2k20.R;
import com.codewithjosh.NewsExpose2k20.adapters.UpdateAdapter;
import com.codewithjosh.NewsExpose2k20.models.UpdateModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private UpdateAdapter updateAdapter;
    private List<UpdateModel> updateList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recycler_updates);
        recyclerView.setHasFixedSize(true); // false
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        updateList = new ArrayList<>();
        updateAdapter = new UpdateAdapter(getContext(), updateList);
        recyclerView.setAdapter(updateAdapter);

        readUpdates();

        return view;

    }

    private void readUpdates() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Updates");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                updateList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UpdateModel updateModel = snapshot.getValue(UpdateModel.class);
                    updateList.add(updateModel);
                }

                updateAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}