/**
 * Copyright (C) 2017 Gustav Wang
 */

package com.carbonylgroup.schoolpower.activities;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.support.v7.widget.Toolbar;

import android.view.Menu;
import android.view.View;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.carbonylgroup.schoolpower.R;
import com.carbonylgroup.schoolpower.classes.ListItems.AssignmentItem;
import com.carbonylgroup.schoolpower.classes.ListItems.MainListItem;
import com.carbonylgroup.schoolpower.classes.ListItems.PeriodGradeItem;
import com.carbonylgroup.schoolpower.classes.Transition.DetailsTransition;
import com.carbonylgroup.schoolpower.classes.Transition.TransitionHelper;
import com.carbonylgroup.schoolpower.classes.Utils.Utils;
import com.carbonylgroup.schoolpower.classes.Utils.postData;
import com.carbonylgroup.schoolpower.fragments.CourseDetailFragment;
import com.carbonylgroup.schoolpower.fragments.HomeFragment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;


public class MainActivity extends TransitionHelper.MainActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private boolean menuOpenDrawer = true;
    private int presentFragment;

    private Utils utils;
    private MainListItem mainListItemTransporter;
    private ArrayList<MainListItem> dataList;
    private Toolbar mainToolBar;
    private AppBarLayout mainAppBar;
    private DrawerLayout drawer;
    private DrawerArrowDrawable toggleIcon;
    private ActionBarDrawerToggle toggle;

    /* Fragments */
    private HomeFragment homeFragment;
    private CourseDetailFragment courseDetailFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_Panzer);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nav_drawer);

        initValue();
        initUI();
        initOnClick();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {

        super.onRestoreInstanceState(savedInstanceState);
        presentFragment = savedInstanceState.getInt("presentFragment");
        int[] fragments = {R.id.nav_dashboard, R.id.course_detail_background};
        gotoFragmentWithMenuItemId(fragments[presentFragment]);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
        outState.putInt("presentFragment", presentFragment);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void onBackPressed() {

        switch (presentFragment) {

            case 1:
                returnToHome();
                break;

            default:
                super.onBackPressed();
                break;
        }
    }

    /* Initializer */
    private void initValue() {

        utils = new Utils(this);

        mainToolBar = (Toolbar) findViewById(R.id.main_toolbar);
        mainAppBar = (AppBarLayout) findViewById(R.id.main_app_bar);
        toggleIcon = new DrawerArrowDrawable(this);
        setSupportActionBar(mainToolBar);
        getSupportActionBar().setTitle("");

        try {
            ArrayList<MainListItem> input = utils.readDataArrayList();
            if (input != null) dataList = input;
        } catch (Exception e) {
            e.printStackTrace();
        }

        initDataJson();
    }

    private void initUI() {

        initDrawer();
        setDefaultFragment();
    }

    private void initOnClick() {

        toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (menuOpenDrawer) drawer.openDrawer(GravityCompat.START);
                else returnToHome();
            }
        });
    }

    private void initDrawer() {

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        toggle = new ActionBarDrawerToggle(
                this, drawer, mainToolBar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.setDrawerIndicatorEnabled(false);
        toggle.setHomeAsUpIndicator(toggleIcon);
        toggle.syncState();

        ((TextView) navigationView.getHeaderView(0).findViewById(R.id.nav_header_username)).setText(getUsername());
        ((TextView) navigationView.getHeaderView(0).findViewById(R.id.nav_header_id)).setText(getUserID());
    }

    /* Fragments Handler */
    public void gotoFragmentWithMenuItemId(int id) {

        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();

        switch (id) {

            case R.id.nav_dashboard:

                homeFragment = new HomeFragment();
                transaction.replace(R.id.content_view, homeFragment);
                presentFragment = 0;
                break;

            case R.id.course_detail_background:

                if (courseDetailFragment == null) courseDetailFragment = new CourseDetailFragment();
                transaction.replace(R.id.content_view, courseDetailFragment);
                presentFragment = 1;
                break;

            case R.id.nav_settings:

//                startActivity(new Intent(getApplication(), SettingsActivity.class));
                break;

            case R.id.nav_sign_out:

                confirmSignOut();
                break;

            case R.id.action_refresh:

                initDataJson();
                break;

            default:
                break;
        }

        transaction.commit();
    }

    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();
        gotoFragmentWithMenuItemId(id);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void returnToHome() {

        expandToolBar(true, true);

        if (homeFragment == null) homeFragment = new HomeFragment();
        homeFragment.setSharedElementEnterTransition(new DetailsTransition());
        homeFragment.setSharedElementReturnTransition(new DetailsTransition());

        getFragmentManager()
                .beginTransaction()
                .addSharedElement(findViewById(R.id.detail_view_header), getString(R.string.shared_element_course_header))
                .addSharedElement(findViewById(R.id.detail_subject_title_tv), getString(R.string.shared_element_course_subject_title))
                .setCustomAnimations(R.animator.do_nothing, R.animator.fade_out)
                .replace(R.id.content_view, homeFragment)
                .addToBackStack(null)
                .commit();

        MainActivity.of(this).setToolBarColor(getResources().getColor(R.color.primary), true);
        animateDrawerToggle(false);
        setToolBarElevation(0);
    }

    public int getPresentFragment() {
        return presentFragment;
    }

    public void setPresentFragment(int input) {
        presentFragment = input;
    }

    private void setDefaultFragment() {

        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        gotoFragmentWithMenuItemId(R.id.nav_dashboard);
        transaction.commit();
    }

    /* Other Method */
    public ArrayList<MainListItem> getDataList() {
        return dataList;
    }

    public void initDataJson() {

        final ArrayList<MainListItem> oldMainItemList = new ArrayList<>();
        oldMainItemList.addAll(dataList);
        String token = getSharedPreferences("accountData", Activity.MODE_PRIVATE).getString("token", "");

        new Thread(new postData(
                getString(R.string.postURL), getString(R.string.token_equals) + token,
                new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        String[] messages = msg.obj.toString().split("\n");

                        SharedPreferences.Editor spEditor = getSharedPreferences(getString(R.string.accountData), Activity.MODE_PRIVATE).edit();
                        spEditor.putString(getString(R.string.student_name), messages[1]);
                        spEditor.apply();

                        if (messages.length == 3 && !messages[2].isEmpty()) {
                            String jsonStr = messages[2];
                            try {
                                utils.saveDataJson(jsonStr);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            dataList = utils.parseJsonResult(jsonStr);

                            if (dataList.size() == oldMainItemList.size()) {
                                for (int i = 0; i < dataList.size(); i++) {
                                    ArrayList<PeriodGradeItem> periods = dataList.get(i).getPeriodGradeItemArrayList();
                                    ArrayList<PeriodGradeItem> oldPeriods = oldMainItemList.get(i).getPeriodGradeItemArrayList();

                                    if (periods.size() != oldPeriods.size()) break;

                                    for (int j = 0; j < periods.size(); j++) {
                                        Collection<AssignmentItem> newAssignmentListCollection = periods.get(j).getAssignmentItemArrayList();
                                        Collection<AssignmentItem> oldAssignmentListCollection = oldPeriods.get(j).getAssignmentItemArrayList();
                                        for (AssignmentItem item : newAssignmentListCollection) {
                                            boolean found = false;
                                            for (AssignmentItem item2 : oldAssignmentListCollection) {
                                                if (Objects.equals(item2.getAssignmentTitle(), item.getAssignmentTitle()) &&
                                                        Objects.equals(item2.getAssignmentDividedScore(), item.getAssignmentDividedScore()) &&
                                                        Objects.equals(item2.getAssignmentDate(), item.getAssignmentDate()))
                                                    found = true;
                                            }
                                            if (!found) item.setAsNewItem(true);
                                        }
                                    }
                                }
                            }

                            homeFragment.refreshAdapter(dataList);
                            if (oldMainItemList.size() == 0) setDefaultFragment();
                        }

                    }
                })).start();
    }

    private String getUsername() {

        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.accountData), Activity.MODE_PRIVATE);
        String name = sharedPreferences.getString(getString(R.string.student_name), "");
        if (!name.equals("")){
            String[] fullName = name.split(" ");
            return fullName[1] + " " + fullName[2];
        }
        return getString(R.string.no_username);
    }

    private String getUserID() {

        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.accountData), Activity.MODE_PRIVATE);
        return getString(R.string.user_id_indicator) + " " + sharedPreferences.getString(getString(R.string.user_id), "");
    }

    private void confirmSignOut() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(getDrawable(R.drawable.ic_exit_dark));
        builder.setTitle("Signing Out");
        builder.setMessage("Are you sure to sign out?");
        builder.setPositiveButton("Sign Out", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                 signOut();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void signOut() {

        SharedPreferences.Editor spEditor = getSharedPreferences(getString(R.string.accountData), Activity.MODE_PRIVATE).edit();
        spEditor.putString(getString(R.string.token), "");
        spEditor.putBoolean(getString(R.string.loggedIn), false);
        spEditor.apply();
        startLoginActivity();
    }

    private void startLoginActivity() {

        startActivity(new Intent(getApplication(), LoginActivity.class));
        MainActivity.this.finish();
    }

    public void animateDrawerToggle(final boolean toArrow) {
        ValueAnimator anim;

        menuOpenDrawer = !toArrow;
        enableDrawer(!toArrow);

        if (toArrow) {

            anim = ValueAnimator.ofFloat(0, 1);
            toggle.setDrawerIndicatorEnabled(false);
        } else anim = ValueAnimator.ofFloat(1, 0);

        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {

                float slideOffset = (Float) valueAnimator.getAnimatedValue();
                toggleIcon.setProgress(slideOffset);
                if (!toArrow && slideOffset == 0) toggle.setDrawerIndicatorEnabled(true);
            }
        });
        anim.setInterpolator(new DecelerateInterpolator());
        anim.setDuration(500);
        anim.start();
    }

    public void enableDrawer(boolean enable) {

        if (enable) drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        else drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    public void expandToolBar(boolean expand, boolean animated) {
        mainAppBar.setExpanded(expand, animated);
    }

    public void setToolBarTitle(String barTitle) {
        ((TextView) findViewById(R.id.toolbar_title_tv)).setText(barTitle);
    }

    public void setToolBarElevation(int toolBarElevation) {
        mainAppBar.setElevation(toolBarElevation);
    }

    public void setToolBarColor(int _actionBarToColor, boolean _animation) {

        final Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        if (_animation) {

            ValueAnimator anim1 = ValueAnimator.ofArgb(((ColorDrawable) mainToolBar.getBackground()).getColor(), _actionBarToColor);
            ValueAnimator anim2 = ValueAnimator.ofArgb(utils.getDarkColorByPrimary(((ColorDrawable) mainToolBar.getBackground()).getColor()), utils.getDarkColorByPrimary(_actionBarToColor));
            anim1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    mainToolBar.setBackgroundColor((Integer) valueAnimator.getAnimatedValue());
                    getWindow().setNavigationBarColor((Integer) valueAnimator.getAnimatedValue());
                }
            });
            anim2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    window.setStatusBarColor((Integer) valueAnimator.getAnimatedValue());
                }
            });
            anim1.setDuration(300);
            anim2.setDuration(300);
            anim1.start();
            anim2.start();

        } else {
            mainToolBar.setBackgroundColor(_actionBarToColor);
            getWindow().setNavigationBarColor(_actionBarToColor);
            window.setStatusBarColor(utils.getDarkColorByPrimary(_actionBarToColor));
        }
    }

    public void setMainListItemTransporter(MainListItem _mainListItem) {
        mainListItemTransporter = _mainListItem;
    }

    public MainListItem getMainListItemTransporter() {
        return mainListItemTransporter;
    }

    public static MainActivity of(Activity activity) {
        return (MainActivity) activity;
    }
}
