package com.codewithjosh.NewsExpose2k20.fragments;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codewithjosh.NewsExpose2k20.BuildConfig;
import com.codewithjosh.NewsExpose2k20.R;
import com.codewithjosh.NewsExpose2k20.adapters.UpdateAdapter;
import com.codewithjosh.NewsExpose2k20.models.UpdateModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    LinearLayout is_loading;
    RecyclerView recycler_updates;
    TextView tv_status;

    FirebaseDatabase firebaseDatabase;
    FirebaseFirestore firebaseFirestore;

    Context context;
    private UpdateAdapter updateAdapter;
    private List<UpdateModel> updateList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initViews(view);
        initInstance();
        loadUpdates();

        return view;

    }

    private void initViews(final View view) {

        is_loading = view.findViewById(R.id.is_loading);
        recycler_updates = view.findViewById(R.id.recycler_updates);
        tv_status = view.findViewById(R.id.tv_status);

        recycler_updates.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recycler_updates.setLayoutManager(linearLayoutManager);
        updateList = new ArrayList<>();
        updateAdapter = new UpdateAdapter(getContext(), updateList);
        recycler_updates.setAdapter(updateAdapter);

    }

    private void initInstance() {

        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

    }

    private void loadUpdates() {

        is_loading.setVisibility(View.VISIBLE);

        firebaseFirestore
                .collection("Updates")
                .orderBy("update_timestamp")
                .addSnapshotListener((value, error) -> {

                    if (value != null)

                        if (validate(value)) onLoadUpdates(value);

                        else is_loading.setVisibility(View.GONE);

                });

    }

    private void onLoadUpdates(final QuerySnapshot value) {

        is_loading.setVisibility(View.GONE);
        tv_status.setText("");

        updateList.clear();
        for (QueryDocumentSnapshot snapshot : value) {

            final UpdateModel update = snapshot.toObject(UpdateModel.class);

            updateList.add(update);
        } updateAdapter.notifyDataSetChanged();

    }

    private boolean validate(final QuerySnapshot value) {

        if (!isConnected()) tv_status.setText(R.string.text_status_disconnected);

        else if (value.isEmpty()) tv_status.setText(R.string.text_status_empty);

        else return true;

        return false;

    }

    private boolean isConnected() {

        if (getContext() != null) context = getContext();

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();

    }

}