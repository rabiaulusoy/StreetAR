package com.marmara.streetar.login;

/**
 * Created by r_rab on 2/25/2018.
 */

public interface LoginView {
    void loginValidations();
    void loginSuccess();
    void loginError();
    void navigateToSignUpActivity();
    void navigateToForgotPasswordActivity();
    void passwordLength();
}
