package com.matrix_maeny.stories.fragments.profile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.matrix_maeny.stories.R;
import com.matrix_maeny.stories.databinding.ActivityFollowViewerBinding;
import com.matrix_maeny.stories.javaclasses.ExposeDialogs;

import java.util.ArrayList;
import java.util.Objects;

public class FollowViewerActivity extends AppCompatActivity {

    private ActivityFollowViewerBinding binding;

    private FirebaseDatabase database;
    private FirebaseAuth auth;
    private ArrayList<FollowModel> list;
    private FollowAdapter adapter;

    private ExposeDialogs dialogs;
    private String currentUid;

    public static boolean followings = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityFollowViewerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.commentToolbar);

        Objects.requireNonNull(getSupportActionBar()).setTitle("Comments");

        FirebaseApp.initializeApp(FollowViewerActivity.this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                SafetyNetAppCheckProviderFactory.getInstance());

        initialize();

    }


    private void initialize() {

        auth = FirebaseAuth.getInstance();
        currentUid = auth.getUid();
        database = FirebaseDatabase.getInstance();

        list = new ArrayList<>();
        adapter = new FollowAdapter(FollowViewerActivity.this, list);

        binding.commentRecyclerView.setLayoutManager(new LinearLayoutManager(FollowViewerActivity.this));
        binding.commentRecyclerView.setAdapter(adapter);

        dialogs = new ExposeDialogs(FollowViewerActivity.this);
        dialogs.setAllCancellable(false);

        if (followings) {
            fetchFollowingsList();
            Objects.requireNonNull(getSupportActionBar()).setTitle("Following");
        }
        else {
            fetchFollowersList();
            Objects.requireNonNull(getSupportActionBar()).setTitle("Followers");
        }


    }


    private void fetchFollowingsList() {

        dialogs.showProgressDialog("Loading...", "Fetching information");
        database.getReference().child("Followings").child(currentUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot s : snapshot.getChildren()) {

                                FollowModel model = s.getValue(FollowModel.class);

                                if (model != null) list.add(model);

                            }

                            refreshAdapter();
                        } else binding.emptyTv.setVisibility(View.VISIBLE);

                        dialogs.dismissProgressDialog();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void fetchFollowersList() {

        dialogs.showProgressDialog("Loading...", "Fetching information");
        database.getReference().child("Followers").child(currentUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot s : snapshot.getChildren()) {

                                FollowModel model = s.getValue(FollowModel.class);

                                if (model != null) list.add(model);

                            }

                            refreshAdapter();

                        }else binding.emptyTv.setVisibility(View.VISIBLE);

                        dialogs.dismissProgressDialog();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void refreshAdapter() {

        if (list.isEmpty()) {
            binding.emptyTv.setVisibility(View.VISIBLE);
        } else {
            binding.emptyTv.setVisibility(View.GONE);
        }
        new Handler().post(() -> adapter.notifyDataSetChanged());
    }


    @Override
    protected void onStart() {
        super.onStart();

        FirebaseApp.initializeApp(FollowViewerActivity.this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                SafetyNetAppCheckProviderFactory.getInstance());
    }
}