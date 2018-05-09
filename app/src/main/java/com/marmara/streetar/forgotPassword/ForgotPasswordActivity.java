package com.marmara.streetar.forgotPassword;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.marmara.streetar.R;
import com.marmara.streetar.login.LoginActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by EDA on 12.03.2018.
 */

public class ForgotPasswordActivity extends AppCompatActivity implements ForgotPasswordView {

    @BindView(R.id.newEmail)
    EditText etEmail;
   /* @BindView(R.id.btnCancel)
    Button bCancel;
    @BindView(R.id.btnReset)
    Button bReset;*/

    ForgotPasswordPresenter forgotPasswordPresenter;
    ProgressDialog progressDialog;
    FirebaseAuth auth;
    //final Dialog dialog = new Dialog(ForgotPasswordActivity.this);



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgotpassword);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*if(BuildConfig.DEBUG){
            etUsername.setText("test");
            etPassword.setText("test");
            etEmail.setText("test");
        }*/

        /*dialog.setContentView(R.layout.activity_forgotpassword);
        dialog.setTitle("Reset Password");
        dialog.setCancelable(true);*/
        forgotPasswordPresenter = new ForgotPasswordPresenter(ForgotPasswordActivity.this);
        }


        @OnClick(R.id.btnReset)
        public void btnResetClicked(View v){
            progressDialog = progressDialog.show(this, "Sending the mail...", null);
            String email = etEmail.getText().toString();
            auth = FirebaseAuth.getInstance();
            forgotPasswordPresenter.performForgotPassword(email,auth);
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
        }

        @OnClick(R.id.btnCancel)
        public void btnCancelClicked(View v){
            //dialog.dismiss();
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
        }

    @Override
    public void forgotPasswordValidations() {
        progressDialog.dismiss();
        Toast.makeText(getApplicationContext(), "Please enter your e-mail", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void forgotPasswordError() {
        progressDialog.dismiss();
        Toast.makeText(getApplicationContext(),"Failed to send reset email!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void forgotPasswordSuccess() {
        progressDialog.dismiss();
        Toast.makeText(getApplicationContext(),"We have sent you instructions to reset your password!",Toast.LENGTH_LONG).show();
        //dialog.dismiss();
    }
}





