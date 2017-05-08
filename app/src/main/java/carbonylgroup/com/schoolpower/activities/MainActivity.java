/**
 * Copyright (C) 2017 Gustav Wang
 */

package carbonylgroup.com.schoolpower.activities;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
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
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import carbonylgroup.com.schoolpower.R;
import carbonylgroup.com.schoolpower.classes.ListItems.AssignmentItem;
import carbonylgroup.com.schoolpower.classes.Transition.DetailsTransition;
import carbonylgroup.com.schoolpower.classes.ListItems.MainListItem;
import carbonylgroup.com.schoolpower.classes.Transition.TransitionHelper;
import carbonylgroup.com.schoolpower.classes.Utils.Utils;
import carbonylgroup.com.schoolpower.classes.Utils.postData;
import carbonylgroup.com.schoolpower.fragments.CourseDetailFragment;
import carbonylgroup.com.schoolpower.fragments.HomeFragment;


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
        int[] fragments = {R.id.nav_home, R.id.course_detail_background};
        gotoFragmentWithMenuItemId(fragments[presentFragment]);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
        outState.putInt("presentFragment", presentFragment);
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

        try {
            if (utils.inputDataArrayList() != null) dataList = utils.inputDataArrayList();
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
    }

    /* Fragments Handler */
    public void gotoFragmentWithMenuItemId(int id) {

        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();

        switch (id) {

            case R.id.nav_home:

                if (homeFragment == null) homeFragment = new HomeFragment();
                transaction.replace(R.id.content_view, homeFragment);
                presentFragment = 0;
                break;

            case R.id.course_detail_background:

                if (courseDetailFragment == null) courseDetailFragment = new CourseDetailFragment();
                transaction.replace(R.id.content_view, courseDetailFragment);
                presentFragment = 1;
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
        gotoFragmentWithMenuItemId(R.id.nav_home);
        transaction.commit();
    }

    /* Other Method */
    public  ArrayList<MainListItem> getDataList(){
        return dataList;
    }

    private String[] getUsernamePassword() {

        SharedPreferences sharedPreferences = getSharedPreferences("accountData", Activity.MODE_PRIVATE);
        String[] unp = {sharedPreferences.getString("username", ""), sharedPreferences.getString("password", "")};
        return unp;
    }

    private void initDataJson() {

        ArrayList<MainListItem> oldMainItemList = dataList;

        String[] unp = getUsernamePassword();

        new Thread(new postData(
                getString(R.string.postURL),
                "username=" + unp[0] + "&password=" + unp[1],
                new Handler() {
                    @Override
                    public void handleMessage(Message msg) {

                        String jsonStr = msg.obj.toString().isEmpty() ? "" : msg.obj.toString();
                        if (!jsonStr.equals("")) {
                            try {
                                utils.outputDataJson(jsonStr);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            dataList = utils.parseJsonResult(jsonStr);
                            homeFragment.refreshAdapter();
                        }
                    }
                })).start();

        for (int i = 0; i < dataList.size(); i++) {

            Collection<AssignmentItem> oldAssignmentListCollection = oldMainItemList.get(i).getAssignmentItemArrayList();
            Collection<AssignmentItem> newAssignmentListCollection = dataList.get(i).getAssignmentItemArrayList();
            newAssignmentListCollection.removeAll(oldAssignmentListCollection);
            for (AssignmentItem item : newAssignmentListCollection) item.setAsNewItem(true);
            oldAssignmentListCollection.addAll(newAssignmentListCollection);
            ArrayList<AssignmentItem> finalList = new ArrayList<>();
            finalList.addAll(oldAssignmentListCollection);
            dataList.get(i).setAssignmentItemArrayList(finalList);
        }

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
