package com.matrix_maeny.stories.javaclasses;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class EmailVerification {

    private Context context;
    private ExposeDialogs exposeDialogs;

    public EmailVerification(Context context) {
        this.context = context;
        exposeDialogs = new ExposeDialogs(context);
    }


    public void sendEmailVerification(@NonNull FirebaseAuth auth, FragmentManager manager){

        ExposeDialogs dialogs = new ExposeDialogs(context);
        dialogs.showProgressDialog("Verifying","sending link");
        FirebaseUser user = auth.getCurrentUser();
        if(user != null){

            user.sendEmailVerification().addOnCompleteListener(task2 -> {

                exposeDialogs.dismissProgressDialog();

                if(task2.isSuccessful()){

                    dialogs.dismissProgressDialog();

                    ExposeDialogs.showVerificationDialog(manager,"Verification dialog",false,user.getEmail());

                }else{
                    Toast.makeText(context, Objects.requireNonNull(task2.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                dialogs.dismissProgressDialog();
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }

    }

}
