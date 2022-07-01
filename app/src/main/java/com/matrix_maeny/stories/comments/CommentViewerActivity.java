package com.matrix_maeny.stories.comments;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.matrix_maeny.stories.R;
import com.matrix_maeny.stories.databinding.ActivityCommentViewerBinding;
import com.matrix_maeny.stories.javaclasses.ExposeDialogs;
import com.matrix_maeny.stories.posts.PostModel;

import java.util.ArrayList;
import java.util.Objects;

public class CommentViewerActivity extends AppCompatActivity {

    private ActivityCommentViewerBinding binding;

    private FirebaseDatabase database;
    private FirebaseAuth auth;
    private ArrayList<CommentModel> list;
    private CommentAdapter adapter;

    private ExposeDialogs dialogs;

    public static PostModel model = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCommentViewerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.commentToolbar);

        Objects.requireNonNull(getSupportActionBar()).setTitle("Comments");

        FirebaseApp.initializeApp(CommentViewerActivity.this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                SafetyNetAppCheckProviderFactory.getInstance());

        initialize();

        if (model != null) {
            fetchComments();
        }
    }

    private void initialize() {

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        list = new ArrayList<>();
        adapter = new CommentAdapter(CommentViewerActivity.this, list);

        binding.commentRecyclerView.setLayoutManager(new LinearLayoutManager(CommentViewerActivity.this));
        binding.commentRecyclerView.setAdapter(adapter);

        dialogs = new ExposeDialogs(CommentViewerActivity.this);
        dialogs.setAllCancellable(false);


    }


    private void fetchComments() {

        dialogs.showProgressDialog("Loading comments","Fetching information");
        database.getReference().child("Comments").child(model.getUserUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot s : snapshot.getChildren()) {

                                String title = s.getKey();
                                if (title != null && title.equals(model.getTitle())) {

                                    for (DataSnapshot cm : s.getChildren()) {
                                        CommentModel commentModel = cm.getValue(CommentModel.class);
                                        if (commentModel != null) {
                                            list.add(commentModel);
                                        }
                                    }

                                    break;
                                }


                            }

                            refreshAdapter();
                        }

                        dialogs.dismissProgressDialog();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void refreshAdapter() {
        new Handler().post(() -> adapter.notifyDataSetChanged());
    }


    @Override
    protected void onStart() {
        super.onStart();

        FirebaseApp.initializeApp(CommentViewerActivity.this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                SafetyNetAppCheckProviderFactory.getInstance());
    }
}