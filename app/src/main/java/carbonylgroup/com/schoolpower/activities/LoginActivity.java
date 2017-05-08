/**
 * Copyright (C) 2017 Gustav Wang
 */

package carbonylgroup.com.schoolpower.activities;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import carbonylgroup.com.schoolpower.R;
import carbonylgroup.com.schoolpower.classes.Utils.Utils;
import carbonylgroup.com.schoolpower.classes.Utils.postData;

public class LoginActivity extends Activity {

    private Utils utils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setTheme(R.style.Theme_Panzer);
        super.onCreate(savedInstanceState);
        checkIfLoggedIn();
        setContentView(R.layout.login_content);

        initValue();
    }

    private void checkIfLoggedIn() {

        SharedPreferences sharedPreferences = getSharedPreferences("accountData", Activity.MODE_PRIVATE);
        if (sharedPreferences.getBoolean("loggedIn", false))
            startMainActivity();
    }

    private void initValue() {

        utils = new Utils(this);

        final EditText input_username = (EditText) findViewById(R.id.input_username);
        final EditText input_password = (EditText) findViewById(R.id.input_password);

        findViewById(R.id.login_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginAction(input_username.getText().toString(), input_password.getText().toString());
            }
        });
    }

    public void loginAction(final String username, final String password) {

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage(getString(R.string.authenticating));
        progressDialog.show();

        new Thread(new postData(
                getString(R.string.postURL),
                "username=" + username + "&password=" + password,
                new Handler() {
                    @Override
                    public void handleMessage(Message msg) {

                        progressDialog.dismiss();
                        String message = msg.obj.toString();

                        if (message.contains("{\"error\":1,\"")) showSnackBar(getString(R.string.wrong_password), true);

                        else if (message.contains("[{\"")) {
                            SharedPreferences.Editor spEditor = getSharedPreferences("accountData", Activity.MODE_PRIVATE).edit();
                            spEditor.putString("username", username);
                            spEditor.putString("password", password);
                            spEditor.putBoolean("loggedIn", true);
                            spEditor.apply();
                            try {
                                utils.outputDataJson(message);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            startMainActivity();

                        } else showSnackBar(getString(R.string.no_connection), true);
                    }
                })).start();
    }

    private void startMainActivity() {

        startActivity(new Intent(getApplication(), MainActivity.class));
        LoginActivity.this.finish();
    }

    private void showSnackBar(String msg, boolean colorRed) {

        Snackbar snackbar = Snackbar.make(findViewById(R.id.login_coordinate_layout), msg, Snackbar.LENGTH_SHORT);
        if(colorRed) snackbar.getView().setBackgroundColor(getResources().getColor(R.color.Cm_score_red_dark));
        else snackbar.getView().setBackgroundColor(getResources().getColor(R.color.accent));
        snackbar.show();
    }
}
