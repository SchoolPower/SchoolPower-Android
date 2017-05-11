/**
 * Copyright (C) 2017 Gustav Wang
 */

package com.carbonylgroup.schoolpower.fragments;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import java.util.ArrayList;

import com.carbonylgroup.schoolpower.R;
import com.carbonylgroup.schoolpower.activities.MainActivity;
import com.carbonylgroup.schoolpower.classes.Adapter.CourseDetailAdapter;
import com.carbonylgroup.schoolpower.classes.ListItems.MainListItem;
import com.carbonylgroup.schoolpower.classes.Transition.TransitionHelper;
import com.carbonylgroup.schoolpower.classes.Utils.Utils;


public class CourseDetailFragment extends TransitionHelper.BaseFragment {

    private View view;
    private Utils utils;
    private MainListItem itemToPresent;
    private Animation offset_up_from_bottom;
    private RecyclerView course_detail_recycler;
    private ArrayList<MainListItem> dataList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.course_detail_view_content, container, false);

        initValue();
        initAnim();

        return view;
    }

    private void initValue() {

        utils = new Utils(getActivity());

        MainActivity.of(getActivity()).setPresentFragment(1);
        MainActivity.of(getActivity()).setToolBarTitle("");
        MainActivity.of(getActivity()).expandToolBar(true, true);

        Bundle bundle = this.getArguments();
        int transformedPosition = bundle.getInt("transformedPosition", -1);

        if (transformedPosition != -1) {

            itemToPresent = MainActivity.of(getActivity()).getMainListItemTransporter();
            view.findViewById(R.id.detail_view_header).setBackgroundColor(utils.getColorByLetterGrade(getActivity(), itemToPresent.getLetterGrade()));
            ((TextView) view.findViewById(R.id.detail_subject_title_tv)).setText(itemToPresent.getSubjectTitle());
            MainActivity.of(getActivity()).setToolBarColor(utils.getColorByLetterGrade(getActivity(), itemToPresent.getLetterGrade()), true);

            course_detail_recycler = (RecyclerView) view.findViewById(R.id.course_detail_recycler);
            try {
                if (utils.readDataArrayList() != null) dataList = utils.readDataArrayList();
            } catch (Exception e){
                e.printStackTrace();
            }
            LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
            CourseDetailAdapter adapter = new CourseDetailAdapter(getActivity(), dataList.get(transformedPosition));
            course_detail_recycler.setLayoutManager(layoutManager);
            course_detail_recycler.setAdapter(adapter);
        }
    }

    private void initAnim() {

        offset_up_from_bottom = AnimationUtils.loadAnimation(getActivity(), R.anim.offset_up_from_bottom);
    }
}
