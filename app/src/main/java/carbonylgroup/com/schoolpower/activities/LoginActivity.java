/**
 * Copyright (C) 2017 Gustav Wang
 */

package carbonylgroup.com.schoolpower.activities;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.view.View;

import carbonylgroup.com.schoolpower.R;
import carbonylgroup.com.schoolpower.classes.Utils.postData;

public class LoginActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setTheme(R.style.Theme_Panzer);
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = getSharedPreferences("accountData", Activity.MODE_PRIVATE);
        if (sharedPreferences.getBoolean("loggedIn", false))
            startMainActivity();

        setContentView(R.layout.login_content);

        findViewById(R.id.login_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSnackBar("pppop");
            }
        });


    }

    public void loginAction(final String username, final String password) {

        new Thread(new postData(
                getString(R.string.postURL),
                "username=" + username + "&password=" + password,
                new Handler() {
                    @Override
                    public void handleMessage(Message msg) {

                        switch (msg.obj.toString().replace("\n", "").charAt(0)) {
                            case '0':
                                showSnackBar("WRONG PASSWORD");
                                break;
                            case '1':
                                SharedPreferences.Editor spEditor = getSharedPreferences("accountData", Activity.MODE_PRIVATE).edit();
                                spEditor.putString("username", username);
                                spEditor.putString("password", password);
                                spEditor.putBoolean("loggedIn", true);
                                spEditor.apply();
                                startMainActivity();
                                break;
                            default:
                                showSnackBar("NO CONNECTION");
                                break;
                        }
                    }
                })).start();
    }

    private void startMainActivity(){

        startActivity(new Intent(getApplication(), MainActivity.class));
        LoginActivity.this.finish();
    }

    private void showSnackBar(String msg) {

        Snackbar snackbar = Snackbar.make(findViewById(R.id.login_coordinate_layout), msg, Snackbar.LENGTH_SHORT);
        snackbar.setActionTextColor(getResources().getColor(R.color.white));
        snackbar.getView().setBackgroundColor(getResources().getColor(R.color.accent));
        snackbar.show();
    }
}
