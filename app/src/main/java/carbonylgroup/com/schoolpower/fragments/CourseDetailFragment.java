/**
 * Copyright (C) 2017 Gustav Wang
 */

package carbonylgroup.com.schoolpower.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import carbonylgroup.com.schoolpower.R;
import carbonylgroup.com.schoolpower.activities.MainActivity;
import carbonylgroup.com.schoolpower.classes.MainListItem;


public class CourseDetailFragment extends Fragment {

    private View view;
    private MainListItem itemToPresent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.course_detail_view_content, container, false);

        initValue();
        initOnClick();
        initAnim();

        return view;
    }

    private void initValue() {

        MainActivity.of(getActivity()).setPresentFragment(1);
        MainActivity.of(getActivity()).setToolBarTitle(getString(R.string.course_detail));

        itemToPresent = MainActivity.of(getActivity()).getMainListItemTransporter();
    }

    private void initOnClick() {

    }

    private void initAnim() {

    }
}
