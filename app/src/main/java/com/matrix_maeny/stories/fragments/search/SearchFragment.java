package com.matrix_maeny.stories.fragments.search;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.matrix_maeny.stories.R;
import com.matrix_maeny.stories.databinding.FragmentSearchBinding;
import com.matrix_maeny.stories.javaclasses.ExposeDialogs;
import com.matrix_maeny.stories.posts.PostModel;

import java.util.ArrayList;
import java.util.Random;


public class SearchFragment extends Fragment {


    private FragmentSearchBinding binding;

    private SearchAdapter adapter;
    private ArrayList<PostModel> list;
    private ExposeDialogs dialogs;

    private FirebaseDatabase firebaseDatabase;

    //    private ArrayList<PostModel> listDay1;
//    private ArrayList<PostModel> listDay2;
//    private ArrayList<PostModel> listDay3;
    private ArrayList<PostModel> listDup;
//    private ArrayList<PostModel> listEnds;



    final Handler handler = new Handler();
    private String searchQuery;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSearchBinding.inflate(inflater, container, false);

        try {
            SearchFragmentListener listener = (SearchFragmentListener) requireContext();
            listener.hideToolbar(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        initialize();

        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            binding.searchView.setQuery("",false);
            binding.searchView.clearFocus();
            fetchPosts(false, null);
        });

        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.equals("")) {
                    Toast.makeText(requireContext(), "Please enter food name", Toast.LENGTH_SHORT).show();
                } else {
                    fetchPosts(true, query);
                }
                binding.searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        binding.goBtn.setOnClickListener(v -> {
            if(checkQuery()){
                fetchPosts(true,searchQuery);
            }
        });

        return binding.getRoot();
    }


    private void initialize() {
        firebaseDatabase = FirebaseDatabase.getInstance();

        list = new ArrayList<>();


        listDup = new ArrayList<>();

        adapter = new SearchAdapter(requireContext(), list);

        dialogs = new ExposeDialogs(requireContext());


        binding.searchRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.searchRecyclerView.setAdapter(adapter);

        fetchPosts(false, null);

    }


    private void fetchPosts(boolean matchWithQuery, String searchQuery) {
        dialogs.showProgressDialog("Loading posts","wait few seconds");
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

                                    PostModel model = sc.getValue(PostModel.class); // get post

                                    if (model != null) { // checking

                                        if(matchWithQuery){
                                            if(searchQuery.contains(model.getTitle()) || model.getTitle().contains(searchQuery)
                                                    || model.getTitle().matches(searchQuery)){
                                                listDup.add(model);
                                            }
                                        }else{
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


                        } else {
                            Toast.makeText(requireContext(), "No posts are available", Toast.LENGTH_SHORT).show();
                        }


                        binding.swipeRefreshLayout.setRefreshing(false);
                        refreshAdapter();
                        dialogs.dismissProgressDialog();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    private boolean checkQuery() {
        try {
            searchQuery = binding.searchView.getQuery().toString().trim();
            if(!searchQuery.equals("")) return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        Toast.makeText(requireContext(), "Please enter food name", Toast.LENGTH_SHORT).show();
        return false;
    }

    private void setAdapterList() {


        final Random random = new Random();

        int limit = 1000;

        if (listDup.size() < limit) {
            limit = listDup.size();
        }

        for (int i = 0; i < limit; i++) {
            int rN = random.nextInt(listDup.size());
            if (!list.contains(listDup.get(rN)))
                list.add(listDup.get(rN));
        }

        for (int i = 0; i < limit; i++) {
            if (!list.contains(listDup.get(i))) list.add(listDup.get(i));

        }

        if(list.isEmpty()){
            binding.sfEmptyTv.setVisibility(View.VISIBLE);
        }else{
            binding.sfEmptyTv.setVisibility(View.GONE);

        }

        listDup.clear();

    }

    @SuppressLint("NotifyDataSetChanged")
    private void refreshAdapter() {
        handler.post(() -> adapter.notifyDataSetChanged());
    }


    public interface SearchFragmentListener {
        void hideToolbar(boolean shouldHide);
    }
}