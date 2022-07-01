package com.matrix_maeny.stories.javaclasses;

import android.app.ProgressDialog;
import android.content.Context;

import androidx.fragment.app.FragmentManager;

import com.matrix_maeny.stories.registerActivities.VerificationDialog;

public class ExposeDialogs {

    private final Context context;

    private ProgressDialog progressDialog;

    public ExposeDialogs(Context context) {
        this.context = context;
        progressDialog = new ProgressDialog(context);

    }

    public void setAllCancellable(boolean yes){
        progressDialog.setCancelable(yes);
        progressDialog.setCanceledOnTouchOutside(yes);
    }


    public void showProgressDialog(String title,String message){

        progressDialog.setTitle(title);
        progressDialog.setMessage(message);

        try {
            progressDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public void dismissProgressDialog(){

        try {
            progressDialog.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public static void showVerificationDialog(FragmentManager manager, String tag, boolean isSent, String email){

        VerificationDialog dialog = new VerificationDialog();
        VerificationDialog.isSent = isSent;
        VerificationDialog.email = email;
        dialog.show(manager, tag);
    }
}
