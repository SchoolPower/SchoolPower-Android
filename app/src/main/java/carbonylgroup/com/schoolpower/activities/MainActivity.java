/**
 * Copyright (C) 2017 Gustav Wang
 */

package carbonylgroup.com.schoolpower.activities;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ClipData;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.MenuItem;
import android.widget.TextView;

import com.balysv.materialmenu.MaterialMenuDrawable;
import com.balysv.materialmenu.MaterialMenuView;

import carbonylgroup.com.schoolpower.R;
import carbonylgroup.com.schoolpower.classes.DetailsTransition;
import carbonylgroup.com.schoolpower.classes.MainListItem;
import carbonylgroup.com.schoolpower.classes.TransitionHelper;
import carbonylgroup.com.schoolpower.fragments.CourseDetailFragment;
import carbonylgroup.com.schoolpower.fragments.HomeFragment;


public class MainActivity extends TransitionHelper.MainActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private boolean menuOpenDrawer = true;
    private int presentFragment;

    private MainListItem mainListItemTransporter;
    private Toolbar mainToolBar;
    private DrawerLayout drawer;
    private MaterialMenuView materialMenu;
    private MaterialMenuDrawable.IconState currentIconState;

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

    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (presentFragment) {

            case 1:
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
                    returnToHome();
                    return true;
                }
                break;

            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    /* Initializer */
    private void initValue() {

        mainToolBar = (Toolbar) findViewById(R.id.main_toolbar);
        materialMenu = (MaterialMenuView) findViewById(R.id.material_menu_view);
    }

    private void initUI() {

        initDrawer();
        setDefaultFragment();
    }

    private void initOnClick() {

        materialMenu.setOnClickListener(new View.OnClickListener() {
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

        if (homeFragment == null) homeFragment = new HomeFragment();
        homeFragment.setSharedElementEnterTransition(new DetailsTransition());
        homeFragment.setSharedElementReturnTransition(new DetailsTransition());

        getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.animator.slide_to_right_in, R.animator.slide_from_left_out)
                .replace(R.id.content_view, homeFragment)
                .addToBackStack(null)
                .commit();

        animateHomeIcon(MaterialMenuDrawable.IconState.BURGER, true);
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
    public boolean animateHomeIcon(MaterialMenuDrawable.IconState iconState, boolean openDrawer) {

        menuOpenDrawer = openDrawer;
        enableDrawer(openDrawer);
        if (currentIconState == iconState) return false;
        currentIconState = iconState;
        materialMenu.animateState(currentIconState);
        return true;
    }

    public void enableDrawer(boolean enable) {

        if (enable) drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        else drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    public void setToolBarTitle(String barTitle) {
        ((TextView) findViewById(R.id.toolbar_title_tv)).setText(barTitle);
    }

    public void setToolBarElevation(int toolBarElevation) {
        mainToolBar.setElevation(toolBarElevation);
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
