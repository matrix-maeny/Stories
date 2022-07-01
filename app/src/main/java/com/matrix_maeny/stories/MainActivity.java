package com.matrix_maeny.stories;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.matrix_maeny.stories.databinding.ActivityMainBinding;
import com.matrix_maeny.stories.fragments.home.HomeFragment;
import com.matrix_maeny.stories.fragments.profile.LogoutFragment;
import com.matrix_maeny.stories.fragments.profile.ProfileFragment;
import com.matrix_maeny.stories.fragments.search.SearchFragment;
import com.matrix_maeny.stories.posts.PostActivity;
import com.matrix_maeny.stories.posts.PostAdapter;
import com.matrix_maeny.stories.posts.PostModel;
import com.matrix_maeny.stories.registerActivities.LoginActivity;

import java.util.Objects;

import me.ibrahimsn.lib.OnItemSelectedListener;

public class MainActivity extends AppCompatActivity implements PostAdapter.PostAdapterListener,
        HomeFragment.HomeFragmentListener, SearchFragment.SearchFragmentListener,
        ProfileFragment.ProfileFragmentListener, LogoutFragment.LogoutFragmentListener {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.app_name));


        FirebaseApp.initializeApp(MainActivity.this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                SafetyNetAppCheckProviderFactory.getInstance());

        setHome();
        binding.bottomBar.setOnItemSelectedListener((OnItemSelectedListener) i -> {

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            switch (i) {
                case 0:
                    fragmentTransaction.replace(R.id.containerFrame, new HomeFragment());
                    break;
                case 1:
                    fragmentTransaction.replace(R.id.containerFrame, new SearchFragment());
                    break;
                case 2:
                    fragmentTransaction.replace(R.id.containerFrame, new ProfileFragment());
                    break;

            }
            try {
                fragmentTransaction.commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        });


    }


    private void setHome() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.containerFrame,new HomeFragment());

        transaction.commit();
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        startActivity(new Intent(MainActivity.this, PostActivity.class));

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void showKeyboard(boolean shouldShow, EditText editText) {
        InputMethodManager manager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        if (manager != null) {
            if (shouldShow) {
                manager.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.RESULT_SHOWN);
            } else {

                manager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
            }
        }
    }

    @Override
    public void showDeleteFragment(PostModel model) {
        LogoutFragment.shouldDelete = true;
        LogoutFragment.model = model;
        LogoutFragment logoutFragment = new LogoutFragment();
        logoutFragment.show(getSupportFragmentManager(), logoutFragment.getTag());
    }

    @Override
    public void hideToolbar(boolean shouldHide) {
        if (shouldHide) {
            Objects.requireNonNull(getSupportActionBar()).hide();
        } else {
            Objects.requireNonNull(getSupportActionBar()).show();

        }

    }

    @Override
    public void showLogoutFragment() {
        LogoutFragment.shouldDelete = false;
        LogoutFragment logoutFragment = new LogoutFragment();
        logoutFragment.show(getSupportFragmentManager(), logoutFragment.getTag());
    }

    @Override
    public void logout() {
        FirebaseAuth.getInstance().signOut();
        ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setTitle("Logging out");
        progressDialog.setMessage("Please wait");

        progressDialog.show();

        new Handler().postDelayed(() -> {
            progressDialog.dismiss();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }, 1500);
    }
}