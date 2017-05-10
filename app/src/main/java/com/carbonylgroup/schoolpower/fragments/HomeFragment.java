/**
 * Copyright (C) 2017 Gustav Wang
 */

package com.carbonylgroup.schoolpower.fragments;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.ListView;

import com.ramotion.foldingcell.FoldingCell;

import java.util.ArrayList;
import java.util.HashSet;

import com.carbonylgroup.schoolpower.R;
import com.carbonylgroup.schoolpower.activities.MainActivity;
import com.carbonylgroup.schoolpower.classes.Transition.DetailsTransition;
import com.carbonylgroup.schoolpower.classes.Adapter.FoldingCellListAdapter;
import com.carbonylgroup.schoolpower.classes.ListItems.MainListItem;
import com.carbonylgroup.schoolpower.classes.Transition.TransitionHelper;
import com.carbonylgroup.schoolpower.classes.Utils.Utils;


public class HomeFragment extends TransitionHelper.BaseFragment {

    private int transformedPosition = -1;

    private View view;
    private Animation fab_out;
    private Animation fab_in;
    private CourseDetailFragment courseDetailFragment;
    private SwipeRefreshLayout home_swipe_refresh_layout;
    private HashSet<Integer> unfoldedIndexesBackUp = new HashSet<>();
    private ArrayList<MainListItem> dataList;
    private FoldingCellListAdapter adapter;
    private Utils utils;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.home_view_content, container, false);

        initAnim();
        initValue();

        return view;
    }

    private void initAnim() {

        fab_in = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        fab_in.setDuration(200);
        fab_in.setInterpolator(new DecelerateInterpolator());
        fab_out = new ScaleAnimation(1.0f, 0.0f, 1.0f, 0.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        fab_out.setDuration(200);
        fab_out.setInterpolator(new DecelerateInterpolator());
    }

    private void initValue() {

        utils = new Utils(getActivity());
        dataList = MainActivity.of(getActivity()).getDataList();
        MainActivity.of(getActivity()).setPresentFragment(0);
        MainActivity.of(getActivity()).setToolBarElevation(utils.dpToPx(4));
        MainActivity.of(getActivity()).setToolBarTitle(getString(R.string.dashboard));
        home_swipe_refresh_layout = (SwipeRefreshLayout) view.findViewById(R.id.home_swipe_refresh_layout);
        home_swipe_refresh_layout.setColorSchemeResources(R.color.accent, R.color.A_score_green, R.color.B_score_green,
                R.color.Cp_score_yellow, R.color.C_score_orange, R.color.Cm_score_red, R.color.primary);
        home_swipe_refresh_layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                MainActivity.of(getActivity()).initDataJson();
            }
        });
        initAdapter();
    }

    private void initAdapter() {

        final ListView theListView = (ListView) view.findViewById(R.id.mainListView);

        adapter = new FoldingCellListAdapter(getActivity(), dataList, unfoldedIndexesBackUp, transformedPosition);

        adapter.setDefaultRequestBtnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MainActivity.of(getActivity()).setMainListItemTransporter(dataList.get(theListView.getPositionForView(v)));

                if (transformedPosition != -1) {

                    View itemView = getItemViewByPosition(transformedPosition, theListView);
                    itemView.findViewById(R.id.unfold_header_view).setTransitionName("");
                    itemView.findViewById(R.id.detail_subject_title_tv).setTransitionName("");
                }

                transformedPosition = theListView.getPositionForView(v);
                View itemView = getItemViewByPosition(theListView.getPositionForView(v), theListView);
                itemView.findViewById(R.id.floating_action_button).startAnimation(fab_out);
                itemView.findViewById(R.id.floating_action_button).setVisibility(View.GONE);
                gotoCourseDetail(itemView.findViewById(R.id.unfold_header_view), itemView.findViewById(R.id.detail_subject_title_tv), transformedPosition);
            }
        });

        theListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {

                adapter.registerToggle(pos);
                ((FoldingCell) view).toggle(false);
                adapter.refreshPeriodRecycler((FoldingCell) view, pos);
                unfoldedIndexesBackUp = adapter.getUnfoldedIndexes();
            }
        });

        theListView.setAdapter(adapter);
    }

    public void refreshAdapter() {

        adapter.setMainListItems(dataList);
        adapter.notifyDataSetChanged();
        showSnackBar(getActivity().getString(R.string.data_updated));
        home_swipe_refresh_layout.setRefreshing(false);
    }

    private View getItemViewByPosition(int position, ListView listView) {

        int firstItemPos = listView.getFirstVisiblePosition();
        int lastItemPos = listView.getLastVisiblePosition();

        if (position < firstItemPos || position > lastItemPos)
            return listView.getAdapter().getView(position, null, listView);
        else
            return listView.getChildAt(position - firstItemPos);
    }

    private void gotoCourseDetail(View _header, View _subject_title, int transformedPosition) {

        courseDetailFragment = new CourseDetailFragment();
        courseDetailFragment.setSharedElementEnterTransition(new DetailsTransition());
        courseDetailFragment.setSharedElementReturnTransition(new DetailsTransition());

        _header.setTransitionName(getString(R.string.shared_element_course_header));
        _subject_title.setTransitionName(getString(R.string.shared_element_course_subject_title));

        Bundle bundle = new Bundle();
        bundle.putInt("transformedPosition", transformedPosition);
        courseDetailFragment.setArguments(bundle);

        getActivity().getFragmentManager()
                .beginTransaction()
                .addSharedElement(_header, getString(R.string.shared_element_course_header))
                .addSharedElement(_subject_title, getString(R.string.shared_element_course_subject_title))
                .setCustomAnimations(R.animator.do_nothing, R.animator.fade_out)
                .replace(R.id.content_view, courseDetailFragment)
                .addToBackStack(null)
                .commit();

        MainActivity.of(getActivity()).setPresentFragment(1);
        MainActivity.of(getActivity()).animateDrawerToggle(true);
        MainActivity.of(getActivity()).setToolBarElevation(0);
    }

    private void preRenderUnfoldCells() {

        final ListView theListView = (ListView) view.findViewById(R.id.mainListView);
        for (int i = 0; i < 10; i++) {

            View itemView = getItemViewByPosition(transformedPosition, theListView);
            ((FoldingCell) itemView).toggle(false);
            ((FoldingCell) itemView).toggle(false);
        }
    }

    private void showSnackBar(String msg) {

        Snackbar snackbar = Snackbar.make(getActivity().findViewById(R.id.main_coordinate_layout), msg, Snackbar.LENGTH_SHORT);
        snackbar.getView().setBackgroundColor(getResources().getColor(R.color.accent));
        snackbar.show();
    }

    @Override
    public void onAfterEnter() {
        super.onAfterEnter();
        preRenderUnfoldCells();
    }
}