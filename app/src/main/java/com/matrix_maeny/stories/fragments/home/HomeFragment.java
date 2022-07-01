package com.matrix_maeny.stories.fragments.home;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.matrix_maeny.stories.R;
import com.matrix_maeny.stories.databinding.FragmentHomeBinding;
import com.matrix_maeny.stories.javaclasses.ExposeDialogs;
import com.matrix_maeny.stories.posts.PostAdapter;
import com.matrix_maeny.stories.posts.PostModel;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;


public class HomeFragment extends Fragment {


    private FragmentHomeBinding binding;
    private PostAdapter adapter;
    private ArrayList<PostModel> list;
//    private String image = "https://images.immediate.co.uk/production/volatile/sites/30/2020/08/processed-food700-350-e6d0f0f.jpg?quality=90&resize=556,505";

    //    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;

    private ArrayList<PostModel> listDup;

    private ExposeDialogs dialogs;


    final Handler handler = new Handler();
    final Random random = new Random();


    private ArrayList<String> listF;
    private ArrayList<PostModel> listDayMain;
    private ArrayList<PostModel> listDay1;
    private ArrayList<PostModel> listDay2;
    private ArrayList<PostModel> listDay3;
    private ArrayList<PostModel> listDay4;
    private ArrayList<PostModel> listDay5;

    private String[] dates;
    private boolean added = false;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        try {
            HomeFragmentListener listener = (HomeFragmentListener) requireContext();
            listener.hideToolbar(false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        initialize();

        binding.swipeRefreshLayout.setOnRefreshListener(this::fetchPosts);
        return binding.getRoot();
    }


    private void initialize() {
//        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        list = new ArrayList<>();

        listF = new ArrayList<>();
        listDayMain = new ArrayList<>();
        listDay1 = new ArrayList<>();
        listDay2 = new ArrayList<>();
        listDay3 = new ArrayList<>();
        listDay4 = new ArrayList<>();
        listDay5 = new ArrayList<>();

        listDup = new ArrayList<>();

        setupDates();

        dialogs = new ExposeDialogs(requireContext());
        dialogs.setAllCancellable(false);

        adapter = new PostAdapter(requireContext(), list);


        binding.recyclerViewHome.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewHome.setAdapter(adapter);

        dialogs.showProgressDialog("Loading Posts", "wait few seconds");
        fetchUserFollowers();
    }

    private void fetchUserFollowers() {
        firebaseDatabase.getReference().child("Followings").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        listF.clear();
                        if (snapshot.exists()) {
                            for (DataSnapshot s : snapshot.getChildren()) {
                                listF.add(s.getKey());
                            }
                        }

                        fetchPosts();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void setupDates() {
        dates = new String[5];
        dates[0] = LocalDate.now().toString();
        dates[1] = LocalDate.now().minusDays(1).toString();
        dates[2] = LocalDate.now().minusDays(2).toString();
        dates[3] = LocalDate.now().minusDays(3).toString();
        dates[4] = LocalDate.now().minusDays(4).toString();


    }


    private void fetchPosts() {
        firebaseDatabase.getReference().child("Posts")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // we need to fetch last three days information about recipes
                            // day 1 , day 2, day 3 for each day 100

                            list.clear();

                            int i = 1;

                            whole:
                            for (DataSnapshot s : snapshot.getChildren()) { // for every user

                                for (DataSnapshot sc : s.getChildren()) { // for each post
                                    added = false;
                                    PostModel model = sc.getValue(PostModel.class); // get post

                                    if (model != null) { // checking


                                        if (model.getLocalDate().equals(dates[0])) {
                                            if (listF.contains(model.getUserUid())) {
                                                listDayMain.add(model);
                                            } else
                                                listDay1.add(model);
                                            added = true;
                                        }
                                        if (model.getLocalDate().equals(dates[1])) {
                                            if (listF.contains(model.getUserUid())) {
                                                listDayMain.add(model);
                                            } else listDay2.add(model);
                                            added = true;
                                        }
                                        if (model.getLocalDate().equals(dates[2])) {
                                            if (listF.contains(model.getUserUid())) {
                                                listDayMain.add(model);
                                            } else listDay3.add(model);
                                            added = true;
                                        }
                                        if (model.getLocalDate().equals(dates[3])) {
                                            listDay4.add(model);
                                            added = true;
                                        }
                                        if (model.getLocalDate().equals(dates[4])) {
                                            listDay5.add(model);
                                            added = true;
                                        }


                                        if (!added) {
                                            listDup.add(model);
                                        }
                                        i++;
                                        if (i >= 1000) {
                                            break whole;
                                        }

                                    }


                                }// second loop
                            }// first loop


                            setAdapterList();

                            dialogs.dismissProgressDialog();
                            if (list.isEmpty()) {
                                binding.hmEmptyTv.setVisibility(View.VISIBLE);
                            } else binding.hmEmptyTv.setVisibility(View.GONE);
                        } else {
                            dialogs.dismissProgressDialog();
                            Toast.makeText(requireContext(), "No posts are available", Toast.LENGTH_SHORT).show();
                        }
                        refreshAdapter();

                        binding.swipeRefreshLayout.setRefreshing(false);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void setAdapterList() {

        list.addAll(listDayMain);
        list.addAll(listDay1);
        list.addAll(listDay2);
        list.addAll(listDay3);
        list.addAll(listDay4);
        list.addAll(listDay5);
        list.addAll(listDup);

        listDayMain.clear();
        listF.clear();
        listDay1.clear();
        listDay2.clear();
        listDay3.clear();
        listDay4.clear();
        listDay5.clear();
        listDup.clear();

    }

    @SuppressLint("NotifyDataSetChanged")
    private void refreshAdapter() {
        handler.post(() -> adapter.notifyDataSetChanged());
    }


    public interface HomeFragmentListener {
        void hideToolbar(boolean shouldHide);
    }

    @Override
    public void onStart() {
        super.onStart();
        fetchUserFollowers();
    }
}