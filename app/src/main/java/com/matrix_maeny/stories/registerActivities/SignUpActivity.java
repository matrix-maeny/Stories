package com.matrix_maeny.stories.registerActivities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.matrix_maeny.stories.UserModel;
import com.matrix_maeny.stories.databinding.ActivitySignUpBinding;
import com.matrix_maeny.stories.javaclasses.EmailVerification;
import com.matrix_maeny.stories.javaclasses.ExposeDialogs;

import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;

    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    private ExposeDialogs exposeDialogs;

    private String username = null, email = null, password = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS); // to create a translucent status of status bar

        FirebaseApp.initializeApp(SignUpActivity.this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                SafetyNetAppCheckProviderFactory.getInstance());

        initialize();

    }

    private void initialize() {

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        exposeDialogs = new ExposeDialogs(SignUpActivity.this);

        progressDialog = new ProgressDialog(SignUpActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("Please wait few seconds");
        progressDialog.setTitle("Creating Account...");

        binding.suLoginTv.setOnClickListener(suLoginTvListener);
        binding.suSignUpBtn.setOnClickListener(suSignUpBtnListener);
    }


    View.OnClickListener suSignUpBtnListener = v -> signUp();

    View.OnClickListener suLoginTvListener = v -> {
        startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
        finish();
    };

    private void signUp() {

        if (checkUsername() && checkEmail() && checkPassword()) {

            exposeDialogs.showProgressDialog("Creating Account...","Please wait few seconds");
            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {


                if (task.isSuccessful()) {
                    UserModel model = new UserModel(username, "", "");
                    String uid = Objects.requireNonNull(task.getResult().getUser()).getUid();


                    firebaseDatabase.getReference().child("Users").child(uid).setValue(model).addOnCompleteListener(task1 -> {

                        exposeDialogs.dismissProgressDialog();
                        EmailVerification verification = new EmailVerification(SignUpActivity.this);
                        verification.sendEmailVerification(firebaseAuth,getSupportFragmentManager());

//                        if(user != null){
//
//                            user.sendEmailVerification().addOnCompleteListener(task2 -> {
//
//                                exposeDialogs.dismissProgressDialog();
//
//                                if(task2.isSuccessful()){
//
//                                    ExposeDialogs.showVerificationDialog(getSupportFragmentManager(),"Verification dialog",false);
//
//                                }else{
//                                    Toast.makeText(SignUpActivity.this, Objects.requireNonNull(task2.getException()).getMessage(), Toast.LENGTH_SHORT).show();
//                                }
//                            }).addOnFailureListener(e -> Toast.makeText(SignUpActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
//                        }



                    });
                }

            }).addOnFailureListener(e -> {
                exposeDialogs.dismissProgressDialog();
                Toast.makeText(SignUpActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }




    private boolean checkUsername() {
        try {
            username = Objects.requireNonNull(binding.suUserNameEt.getText()).toString();
            if (!username.equals("")) return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        Toast.makeText(this, "Please enter username", Toast.LENGTH_SHORT).show();

        return false;
    }

    private boolean checkEmail() {
        try {
            email = Objects.requireNonNull(binding.suEmailEt.getText()).toString();
            if (!email.equals("")) return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        Toast.makeText(this, "Please enter Email", Toast.LENGTH_SHORT).show();
        return false;
    }

    private boolean checkPassword() {
        try {
            password = Objects.requireNonNull(binding.suPasswordEt.getText()).toString();
            if (!password.equals("")) return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        Toast.makeText(this, "Please enter Password", Toast.LENGTH_SHORT).show();
        return false;
    }


    @Override
    protected void onStart() {
        super.onStart();

        FirebaseApp.initializeApp(SignUpActivity.this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                SafetyNetAppCheckProviderFactory.getInstance());
    }

}