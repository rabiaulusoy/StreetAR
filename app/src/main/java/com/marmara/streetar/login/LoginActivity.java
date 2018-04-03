package com.marmara.streetar.login;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.marmara.streetar.BuildConfig;
import com.marmara.streetar.main.MainActivity;
import com.marmara.streetar.R;

import com.marmara.streetar.signup.SignupActivity;
import com.marmara.streetar.forgotPassword.ForgotPasswordActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends Activity implements LoginView {
    @BindView(R.id.username) EditText etUsername;
    @BindView(R.id.password) EditText etPassword;
    LoginPresenter loginPresenter;
    ProgressDialog progressDialog;
    FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        if(BuildConfig.DEBUG){
            etUsername.setText("rabiaulusoy123@gmail.com");
            etPassword.setText("123456");
        }
        loginPresenter = new LoginPresenter(LoginActivity.this);

    }

    @OnClick(R.id.login)
    public void loginClicked(View v) {
        progressDialog = progressDialog.show(this, "Authenticating...", null);
        String username = etUsername.getText().toString();
        String password = etPassword.getText().toString();
        auth = FirebaseAuth.getInstance();
        loginPresenter.performLogin(username,password,auth);
    }

    @OnClick(R.id.signup)
    public void signupClicked(View v){
        loginPresenter.navigateToSignUp();
    }

    @OnClick(R.id.forgotPass)
    public void forgotPassClicked(View v){
        loginPresenter.navigateToForgotPass();
    }

    @Override
    public void loginValidations() {
        progressDialog.dismiss();
        Toast.makeText(getApplicationContext(), "Please enter username and password", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void passwordLength() {
        progressDialog.dismiss();
        Toast.makeText(getApplicationContext(), "Password too short!", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void loginSuccess() {
        progressDialog.dismiss();
        Toast.makeText(getApplicationContext(), "Login Success", Toast.LENGTH_SHORT).show();
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }

    @Override
    public void loginError() {
        progressDialog.dismiss();
        Toast.makeText(getApplicationContext(),"Login Failure", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void navigateToSignUpActivity() {
        Intent i = new Intent(this, SignupActivity.class);
        startActivity(i);
    }

    @Override
    public void navigateToForgotPasswordActivity() {
        Intent i = new Intent(this, ForgotPasswordActivity.class);
        startActivity(i);
    }



}
