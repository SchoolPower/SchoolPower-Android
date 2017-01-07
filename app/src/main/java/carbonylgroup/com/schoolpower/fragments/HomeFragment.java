/**
 * Copyright (C) 2017 Gustav Wang
 */

package carbonylgroup.com.schoolpower.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.balysv.materialmenu.MaterialMenuDrawable;
import com.ramotion.foldingcell.FoldingCell;

import java.util.ArrayList;

import carbonylgroup.com.schoolpower.R;
import carbonylgroup.com.schoolpower.activities.MainActivity;
import carbonylgroup.com.schoolpower.classes.DetailsTransition;
import carbonylgroup.com.schoolpower.classes.FoldingCellListAdapter;
import carbonylgroup.com.schoolpower.classes.MainListItem;


public class HomeFragment extends Fragment {

    private View view;
    private CourseDetailFragment courseDetailFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.home_view_content, container, false);
        initValue();

        return view;
    }

    private void initValue() {

        MainActivity.of(getActivity()).setPresentFragment(0);
        MainActivity.of(getActivity()).setToolBarElevation(10);
        MainActivity.of(getActivity()).setToolBarTitle(getString(R.string.dashboard));

        final ListView theListView = (ListView) view.findViewById(R.id.mainListView);
        final ArrayList<MainListItem> items = MainListItem.getTestingList();
        final FoldingCellListAdapter adapter = new FoldingCellListAdapter(getActivity(), items);

        adapter.setDefaultRequestBtnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MainActivity.of(getActivity()).setMainListItemTransporter(items.get(theListView.getPositionForView(v)));
                gotoCourseDetail();
            }
        });
        theListView.setAdapter(adapter);
        theListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {

                adapter.registerToggle(pos);
                ((FoldingCell) view).toggle(false);
                adapter.refreshPeriodRecycler((FoldingCell) view);
            }
        });
    }

    private void gotoCourseDetail() {

        if (courseDetailFragment == null)
            courseDetailFragment = new CourseDetailFragment();
        courseDetailFragment.setSharedElementEnterTransition(new DetailsTransition());
        courseDetailFragment.setSharedElementReturnTransition(new DetailsTransition());

        getActivity().getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.animator.slide_from_right_in, R.animator.slide_to_left_out)
                .replace(R.id.content_view, courseDetailFragment)
                .addToBackStack(null)
                .commit();

        MainActivity.of(getActivity()).setPresentFragment(1);
        MainActivity.of(getActivity()).animateHomeIcon(MaterialMenuDrawable.IconState.ARROW, false);
        MainActivity.of(getActivity()).setToolBarElevation(0);
    }
}