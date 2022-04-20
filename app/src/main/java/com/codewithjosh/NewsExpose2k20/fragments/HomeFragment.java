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

    RecyclerView recycler_updates;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseRef;
    private UpdateAdapter updateAdapter;
    private List<UpdateModel> updateList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recycler_updates = view.findViewById(R.id.recycler_updates);
        recycler_updates.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recycler_updates.setLayoutManager(linearLayoutManager);
        updateList = new ArrayList<>();
        updateAdapter = new UpdateAdapter(getContext(), updateList);
        recycler_updates.setAdapter(updateAdapter);

        firebaseDatabase = FirebaseDatabase.getInstance();

        getUpdates();

        return view;

    }

    private void getUpdates() {

        databaseRef = firebaseDatabase.getReference("Updates");

//        TODO: USE GET METHOD ONCE IT IS AVAILABLE
        databaseRef.addValueEventListener(new ValueEventListener() {

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