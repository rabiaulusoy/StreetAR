package com.marmara.streetar.signup;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.marmara.streetar.R;
import com.marmara.streetar.login.LoginActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SignupActivity extends Activity implements SignupView {
    @BindView(R.id.email)
    EditText etEmail;
    @BindView(R.id.username)
    EditText etUsername;
    @BindView(R.id.password)
    EditText etPassword;

    SignupPresenter signupPresenter;
    ProgressDialog progressDialog;
    FirebaseAuth auth;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);

        /*if(BuildConfig.DEBUG){
            etUsername.setText("test");
            etPassword.setText("test");
            etEmail.setText("test");
        }*/
        signupPresenter = new SignupPresenter(SignupActivity.this);
    }

    @OnClick(R.id.signup)
    public void signupClicked(View v){
        progressDialog = progressDialog.show(this, "Authenticating...", null);
        String username = etUsername.getText().toString();
        String password = etPassword.getText().toString();
        String email = etEmail.getText().toString();
        auth = FirebaseAuth.getInstance();
        databaseReference=FirebaseDatabase.getInstance().getReference();
        signupPresenter.performSignup(email, username, password,auth,databaseReference);
    }

    @Override
    public void signupValidations() {
        progressDialog.dismiss();
        Toast.makeText(getApplicationContext(), "Please enter email, username and password", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void passwordLength() {
        progressDialog.dismiss();
        Toast.makeText(getApplicationContext(), "Password too short!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void signupSuccess() {
        progressDialog.dismiss();

        Toast.makeText(getApplicationContext(), "Signup Success", Toast.LENGTH_SHORT).show();
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
    }

    @Override
    public void signupError() {
        progressDialog.dismiss();
        Toast.makeText(getApplicationContext(), "Signup Failure", Toast.LENGTH_SHORT).show();
    }
}
